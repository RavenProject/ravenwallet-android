package com.ravenwallet.wallet;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.security.keystore.UserNotAuthenticatedException;
import android.text.format.DateUtils;
import android.util.Log;

import com.ravenwallet.RavenApp;
import com.ravenwallet.R;
import com.ravenwallet.core.BRCoreKey;
import com.ravenwallet.core.BRCoreMasterPubKey;
import com.ravenwallet.presenter.customviews.BRDialogView;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.animation.BRDialog;
import com.ravenwallet.tools.manager.BRReportsManager;
import com.ravenwallet.tools.manager.BRSharedPrefs;
import com.ravenwallet.tools.security.BRKeyStore;
import com.ravenwallet.tools.threads.executor.BRExecutor;
import com.ravenwallet.tools.util.BRConstants;
import com.ravenwallet.tools.util.Bip39Wordlist;
import com.ravenwallet.tools.util.TrustedNode;
import com.ravenwallet.tools.util.Utils;
import com.ravenwallet.wallet.abstracts.BaseWalletManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class WalletsMaster {

    private static final String TAG = WalletsMaster.class.getName();

    private static WalletsMaster instance;

    private List<BaseWalletManager> mWallets = new ArrayList<>();

    private WalletsMaster(Context app) {
    }

    public synchronized static WalletsMaster getInstance(Context app) {
        if (instance == null) {
            instance = new WalletsMaster(app);
        }
        return instance;
    }

    public List<BaseWalletManager> getAllWallets() {
        return mWallets;
    }

    //return the needed wallet for the iso
    public BaseWalletManager getWalletByIso(Context app, String iso) {
        if (Utils.isNullOrEmpty(iso))
            throw new RuntimeException("getWalletByIso with iso = null, Cannot happen!");
        if (iso.equalsIgnoreCase("RVN"))
            return RvnWalletManager.getInstance(app);
        return null;
    }

    public BaseWalletManager getCurrentWallet(Context app) {
        return getWalletByIso(app, BRSharedPrefs.getCurrentWalletIso(app));
    }

    //get the total fiat balance held in all the wallets in the smallest unit (e.g. cents)
    public BigDecimal getAggregatedFiatBalance(Context app) {
        BigDecimal totalBalance = new BigDecimal(0);
        for (BaseWalletManager wallet : mWallets) {
            totalBalance = totalBalance.add(wallet.getFiatBalance(app));
        }
        return totalBalance;
    }

    public synchronized boolean generateRandomSeed(final Context ctx) {
        Bip39Wordlist bipWords = Bip39Wordlist.getWordlistForLocale();

        //Generate a random seed to use
        byte[] randomSeed = bipWords.generateRandomSeed();
        //Generate a byte-array String of the paper key created
        byte[] paperKeyBytes = bipWords.generatePaperKeyBytes(ctx, randomSeed);
        //Split that byte[] into an array of each word
        String[] splitPhrase = bipWords.splitPharse(paperKeyBytes);

        //Attempt write paper key
        boolean success = false;
        try {
            success = BRKeyStore.putPhrase(paperKeyBytes, ctx, BRConstants.PUT_PHRASE_NEW_WALLET_REQUEST_CODE);
        } catch (UserNotAuthenticatedException e) {
            return false;
        }
        if (!success) return false;
        //Attempt re-read and verify
        byte[] verifyPaperKeyBytes;
        try {
            verifyPaperKeyBytes = BRKeyStore.getPhrase(ctx, 0);
        } catch (UserNotAuthenticatedException e) {
            throw new RuntimeException("Failed to retrieve the verifyPaperKeyBytes even though at this point the system auth was asked for sure.");
        }

        if (Utils.isNullOrEmpty(verifyPaperKeyBytes))
            throw new NullPointerException("verifyPaperKeyBytes is null!! - Unable to retrieve verifyPaperKeyBytes from BRKeyStore");
        if (verifyPaperKeyBytes.length == 0)
            throw new RuntimeException("verifyPaperKeyBytes is empty");

        //Re-extract the seed from the newly created phrase for checking
        byte[] verifyPaperKeySeed = bipWords.getSeedFromPhrase(verifyPaperKeyBytes);

        //Create an api private key, this function will internally verify the results, and will raise if invalid
        byte[] privateKeyBytes = bipWords.getPrivateKeyForAPI(randomSeed);

        if(paperKeyBytes.length != verifyPaperKeyBytes.length)
            throw new RuntimeException("paperKeyBytes and verifyPaperKeyBytes do not mach in length.");
        for(int i=0;i<paperKeyBytes.length;i++)
            if(paperKeyBytes[i] != verifyPaperKeyBytes[i])
                throw new RuntimeException("paperKeyBytes and verifyPaperKeyBytes do not match. Aborting.");

        int walletCreationTime = (int) (System.currentTimeMillis() / DateUtils.SECOND_IN_MILLIS);
        BRKeyStore.putWalletCreationTime(walletCreationTime, ctx);
//        final WalletInfo info = new WalletInfo();
//        info.creationDate = walletCreationTime;
//        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
//            @Override
//            public void run() {
//                KVStoreManager.getInstance().putWalletInfo(ctx, info); //push the creation time to the kv store
//            }
//        });

        //store the serialized in the BRKeyStore
        byte[] pubKey = bipWords.getMasterPubKey(paperKeyBytes, true).serialize();
        BRKeyStore.putMasterPublicKey(pubKey, ctx);

        return true;

    }

    public boolean isIsoCrypto(Context app, String iso) {
        for (BaseWalletManager w : mWallets) {
            if (w.getIso(app).equalsIgnoreCase(iso)) return true;
        }
        return false;
    }

    public boolean wipeKeyStore(Context context) {
        Log.d(TAG, "wipeKeyStore");
        return BRKeyStore.resetWalletKeyStore(context);
    }

    /**
     * true if keystore is available and we know that no wallet exists on it
     */
    public boolean noWallet(Context ctx) {
        byte[] pubkey = BRKeyStore.getMasterPublicKey(ctx);

        if (pubkey == null || pubkey.length == 0) {
            byte[] phrase;
            try {
                phrase = BRKeyStore.getPhrase(ctx, 0);
                //if not authenticated, an error will be thrown and returned false, so no worry about mistakenly removing the wallet
                if (phrase == null || phrase.length == 0) {
                    return true;
                }
            } catch (UserNotAuthenticatedException e) {
                return false;
            }

        }
        return false;
    }

    public boolean noWalletForPlatform(Context ctx) {
        byte[] pubkey = BRKeyStore.getMasterPublicKey(ctx);
        return pubkey == null || pubkey.length == 0;
    }

    /**
     * true if device passcode is enabled
     */
    public boolean isPasscodeEnabled(Context ctx) {
        KeyguardManager keyguardManager = (KeyguardManager) ctx.getSystemService(Activity.KEYGUARD_SERVICE);
        return keyguardManager.isKeyguardSecure();
    }

    public boolean isNetworkAvailable(Context ctx) {
        if (ctx == null) return false;
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();

    }

    public void wipeWalletButKeystore(final Context ctx) {
        Log.d(TAG, "wipeWalletButKeystore");
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                for (BaseWalletManager wallet : mWallets) {
                    wallet.wipeData(ctx);
                }
//                wipeAll(ctx);
//                ((ActivityManager)ctx.getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
            }
        });
    }

    public void wipeAll(Context app) {
        wipeKeyStore(app);
        wipeWalletButKeystore(app);
    }

    public void refreshBalances(Context app) {
        for (BaseWalletManager wallet : mWallets) {
            long balance = wallet.getWallet().getBalance();
            wallet.setCashedBalance(app, balance);
        }

    }

    public void initWallets(Context app) {
        if (!mWallets.contains(RvnWalletManager.getInstance(app)))
            mWallets.add(RvnWalletManager.getInstance(app));
    }

    public void initLastWallet(Context app) {
        if (app == null) {
            app = RavenApp.getRvnContext();
            if (app == null) {
                Log.e(TAG, "initLastWallet: FAILED, app is null");
                return;
            }
        }
        BaseWalletManager wallet = getWalletByIso(app, BRSharedPrefs.getCurrentWalletIso(app));
        if (wallet == null) wallet = getWalletByIso(app, "RVN");
        wallet.connectWallet(app);
    }

    public void updateFixedPeer(Context app, BaseWalletManager wm) {
        String node = BRSharedPrefs.getTrustNode(app, wm.getIso(app));
        if (!Utils.isNullOrEmpty(node)) {
            String host = TrustedNode.getNodeHost(node);
            int port = TrustedNode.getNodePort(node);
            boolean success = wm.getPeerManager().useFixedPeer(host, port);
            if (!success) {
                Log.e(TAG, "updateFixedPeer: Failed to updateFixedPeer with input: " + node);
            } else {
                Log.d(TAG, "updateFixedPeer: succeeded");
            }
        }
        wm.getPeerManager().connect();

    }

    public void startTheWalletIfExists(final Activity app) {
        final WalletsMaster m = WalletsMaster.getInstance(app);
        if (!m.isPasscodeEnabled(app)) {
            //Device passcode/password should be enabled for the app to work
            BRDialog.showCustomDialog(app, app.getString(R.string.JailbreakWarnings_title), app.getString(R.string.Prompts_NoScreenLock_body_android),
                    app.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            app.finish();
                        }
                    }, null, new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            app.finish();
                        }
                    }, 0);
        } else {
            if (!m.noWallet(app)) {
                BRAnimator.startRvnActivity(app, true);
            }
            //else just sit in the intro screen

        }
    }

}