package com.ravencoin.wallet.wallets.raven;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.annotation.NonNull;
import android.support.test.espresso.core.internal.deps.guava.collect.Lists;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.platform.assets.Asset;
import com.platform.assets.AssetType;
import com.platform.assets.AssetsRepository;
import com.platform.entities.TxMetaData;
import com.ravencoin.RavenApp;
import com.ravencoin.BuildConfig;
import com.ravencoin.R;
import com.ravencoin.core.BRCoreAddress;
import com.ravencoin.core.BRCoreChainParams;
import com.ravencoin.core.BRCoreKey;
import com.ravencoin.core.BRCoreMasterPubKey;
import com.ravencoin.core.BRCoreMerkleBlock;
import com.ravencoin.core.BRCorePeer;
import com.ravencoin.core.BRCorePeerManager;
import com.ravencoin.core.BRCoreTransaction;
import com.ravencoin.core.BRCoreTransactionAsset;
import com.ravencoin.core.BRCoreWallet;
import com.ravencoin.core.BRCoreWalletManager;
import com.ravencoin.core.MyTransactionAsset;
import com.ravencoin.presenter.customviews.BRToast;
import com.ravencoin.presenter.entities.BRMerkleBlockEntity;
import com.ravencoin.presenter.entities.BRPeerEntity;
import com.ravencoin.presenter.entities.BRTransactionEntity;
import com.ravencoin.presenter.entities.BlockEntity;
import com.ravencoin.presenter.entities.CurrencyEntity;
import com.ravencoin.presenter.entities.PeerEntity;
import com.ravencoin.presenter.entities.TxUiHolder;
import com.ravencoin.presenter.fragments.FragmentPin;
import com.ravencoin.presenter.interfaces.BRAuthCompletion;
import com.ravencoin.presenter.interfaces.BROnSignalCompletion;
import com.ravencoin.presenter.interfaces.ConfirmationListener;
import com.ravencoin.presenter.interfaces.WalletManagerListener;
import com.ravencoin.tools.animation.BRAnimator;
import com.ravencoin.tools.animation.BRDialog;
import com.ravencoin.tools.manager.BREventManager;
import com.ravencoin.tools.manager.BRNotificationManager;
import com.ravencoin.tools.manager.BRReportsManager;
import com.ravencoin.tools.manager.BRSharedPrefs;
import com.ravencoin.tools.manager.InternetManager;
import com.ravencoin.tools.security.BRKeyStore;
import com.ravencoin.tools.sqlite.CurrencyDataSource;
import com.ravencoin.tools.sqlite.MerkleBlockDataSource;
import com.ravencoin.tools.sqlite.PeerDataSource;
import com.ravencoin.tools.sqlite.RvnTransactionDataStore;
import com.ravencoin.tools.sqlite.TransactionStorageManager;
import com.ravencoin.tools.threads.executor.BRExecutor;
import com.ravencoin.tools.util.BRConstants;
import com.ravencoin.tools.util.CurrencyUtils;
import com.ravencoin.tools.util.SymbolUtils;
import com.ravencoin.tools.util.TypesConverter;
import com.ravencoin.tools.util.Utils;
import com.ravencoin.wallet.WalletsMaster;
import com.ravencoin.wallet.abstracts.BaseWalletManager;
import com.ravencoin.wallet.abstracts.OnBalanceChangedListener;
import com.ravencoin.wallet.abstracts.OnTxListModified;
import com.ravencoin.wallet.abstracts.OnTxStatusUpdatedListener;
import com.ravencoin.wallet.abstracts.SyncListener;
import com.ravencoin.wallet.wallets.configs.WalletUiConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.platform.assets.AssetType.BURN;
import static com.platform.assets.AssetType.NEW_ASSET;
import static com.platform.assets.AssetType.REISSUE;
import static com.platform.assets.AssetType.TRANSFER;
import static com.platform.assets.AssetsValidation.isAssetNameAnOwner;
import static com.ravencoin.tools.util.BRConstants.CREATION_FEE;
import static com.ravencoin.tools.util.BRConstants.REISSUE_FEE;
import static com.ravencoin.tools.util.BRConstants.ROUNDING_MODE;
import static com.ravencoin.tools.util.BRConstants.SATOSHIS;
import static com.ravencoin.tools.util.BRConstants.SUB_FEE;
import static com.ravencoin.tools.util.BRConstants.UNIQUE_FEE;

//import com.google.firebase.crash.FirebaseCrash;

/**
 * RavenWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 1/22/18.
 * Copyright (c) 2018 breadwallet LLC
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
public class RvnWalletManager extends BRCoreWalletManager implements BaseWalletManager {

    private static final String TAG = RvnWalletManager.class.getName();

    private static String ISO = "RVN";

    private static final String mName = "Ravencoin";
    public static final String RVN_SCHEME = "raven";


    public static final long MAX_RVN = 21000000 * 1000L;

    private static RvnWalletManager instance;
    private WalletUiConfiguration uiConfig;
    private boolean isTransfer = true;
    private int mSyncRetryCount = 0;
    private static final int SYNC_MAX_RETRY = 3;

    private boolean isInitiatingWallet;

    private List<OnBalanceChangedListener> balanceListeners = new ArrayList<>();
    private List<OnTxStatusUpdatedListener> txStatusUpdatedListeners = new ArrayList<>();
    private List<SyncListener> syncListeners = new ArrayList<>();
    private List<OnTxListModified> txModifiedListeners = new ArrayList<>();

    private Executor listenerExecutor = Executors.newSingleThreadExecutor();

    public synchronized static RvnWalletManager getInstance(Context app) {
        if (instance == null) {
            byte[] rawPubKey = BRKeyStore.getMasterPublicKey(app);
            if (Utils.isNullOrEmpty(rawPubKey)) {
                Log.e(TAG, "getInstance: rawPubKey is null");
                return null;
            }
            BRCoreMasterPubKey pubKey = new BRCoreMasterPubKey(rawPubKey, false);
//            long time = BRKeyStore.getWalletCreationTime(app);
//            if (Utils.isEmulatorOrDebug(app)) time = 1517955529;
//            long time = 1519190488;
            long time = 0;
//            long time = (System.currentTimeMillis() / 1000) - 3 * 7 * 24 * 60 * 60; // 3 * 7
            instance = new RvnWalletManager(app, pubKey, BuildConfig.TESTNET ? BRCoreChainParams.testnetChainParams : BRCoreChainParams.mainnetChainParams, time);
        }
        return instance;
    }

    private RvnWalletManager(final Context app, BRCoreMasterPubKey masterPubKey,
                             BRCoreChainParams chainParams,
                             double earliestPeerTime) {
        super(masterPubKey, chainParams, earliestPeerTime);
        if (isInitiatingWallet) return;
        isInitiatingWallet = true;
        try {
            Log.d(TAG, "connectWallet:" + Thread.currentThread().getName());
            if (app == null) {
                Log.e(TAG, "connectWallet: app is null");
                return;
            }
            String firstAddress = masterPubKey.getPubKeyAsCoreKey().address();
            BRSharedPrefs.putFirstAddress(app, firstAddress);
            long fee = BRSharedPrefs.getFeePerKb(app, getIso(app));
            long economyFee = BRSharedPrefs.getEconomyFeePerKb(app, getIso(app));
            if (fee == 0) {
                fee = getWallet().getDefaultFeePerKb();
                BREventManager.getInstance().pushEvent("wallet.didUseDefaultFeePerKB");
            }
            getWallet().setFeePerKb(BRSharedPrefs.getFavorStandardFee(app, getIso(app)) ? fee : economyFee);
            if (BRSharedPrefs.getStartHeight(app, getIso(app)) == 0)
                BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                    @Override
                    public void run() {
                        BRSharedPrefs.putStartHeight(app, getIso(app), getPeerManager().getLastBlockHeight());
                    }
                });

            WalletsMaster.getInstance(app).updateFixedPeer(app, this);
//        balanceListeners = new ArrayList<>();

            uiConfig = new WalletUiConfiguration("#2e3e80", true, true);

        } finally {
            isInitiatingWallet = false;
        }

    }

    @Override
    protected BRCoreWallet.Listener createWalletListener() {
        return new BRCoreWalletManager.WrappedExecutorWalletListener(
                super.createWalletListener(),
                listenerExecutor);
    }

    @Override
    protected BRCorePeerManager.Listener createPeerManagerListener() {
        return new BRCoreWalletManager.WrappedExecutorPeerManagerListener(
                super.createPeerManagerListener(),
                listenerExecutor);
    }

    @Override
    public BRCoreTransaction[] getTransactions() {
        return getWallet().getTransactions();
    }

    @Override
    public void updateFee(Context app) {
        if (app == null) {
            app = RavenApp.getRvnContext();
            if (app == null) {
                Log.e(TAG, "updateFee: FAILED, app is null");
                return;
            }
        }
//        String jsonString = /*BRApiManager.urlGET(app, "https://" + RavenApp.HOST + "/fee-per-kb?currency=" + getIso(app));*/
        String jsonString = "{fee_per_kb : 10000,fee_per_kb_economy : 10000}";
        if (jsonString == null || jsonString.isEmpty()) {
            Log.e(TAG, "updateFeePerKb: failed to update fee, response string: " + jsonString);
            return;
        }
        long fee;
        long economyFee;
        try {
            JSONObject obj = new JSONObject(jsonString);
            fee = obj.getLong("fee_per_kb");
            economyFee = obj.getLong("fee_per_kb_economy");

            if (fee != 0 && fee < getWallet().getMaxFeePerKb()) {
                BRSharedPrefs.putFeePerKb(app, getIso(app), fee);
                getWallet().setFeePerKb(BRSharedPrefs.getFavorStandardFee(app, getIso(app)) ? fee : economyFee);
                BRSharedPrefs.putFeeTime(app, getIso(app), System.currentTimeMillis()); //store the time of the last successful fee fetch
            } else {
                Crashlytics.logException(new NullPointerException("Fee is weird:" + fee));
            }
            if (economyFee != 0 && economyFee < getWallet().getMaxFeePerKb()) {
                BRSharedPrefs.putEconomyFeePerKb(app, getIso(app), economyFee);
            } else {
                Crashlytics.logException(new NullPointerException("Economy fee is weird:" + economyFee));
            }
        } catch (JSONException e) {
            Log.e(TAG, "updateFeePerKb: FAILED: " + jsonString, e);
            BRReportsManager.reportBug(e);
            BRReportsManager.reportBug(new IllegalArgumentException("JSON ERR: " + jsonString));
        }
    }

    @Override
    public List<TxUiHolder> getTxUiHolders() {
        BRCoreTransaction txs[] = getWallet().getTransactions();
        if (txs == null || txs.length <= 0) return null;
        List<TxUiHolder> uiTxs = new ArrayList<>();
        for (int i = txs.length - 1; i >= 0; i--) { //reverse order
            BRCoreTransaction tx = txs[i];
            if (tx.hasAsset()) {
                ArrayList<BRCoreTransaction> decomposedTxs = getDecomposedTransactions(tx);
                if (decomposedTxs != null)
                    for (BRCoreTransaction dtx : decomposedTxs)
                        uiTxs.add(getTxHolders(dtx));
            } else {
                uiTxs.add(getTxHolders(tx));
            }
        }

        return uiTxs;
    }

    @NonNull
    private TxUiHolder getTxHolders(BRCoreTransaction tx) {
        return new TxUiHolder(tx.getTimestamp(), (int) tx.getBlockHeight(), tx.getHash(),
                tx.getReverseHash(), getWallet().getTransactionAmountSent(tx),
                getWallet().getTransactionAmountReceived(tx), getWallet().getTransactionFee(tx),
                tx.getOutputAddresses(), tx.getInputAddresses(),
                getWallet().getBalanceAfterTransaction(tx), (int) tx.getSize(),
                getWallet().getTransactionAmount(tx), getWallet().transactionIsValid(tx),
                tx.hasAsset() ? tx.getAsset() : null);
    }

    @Override
    public boolean generateWallet(Context app) {
        //no need, one key for all wallets so far
        return true;
    }

    @Override
    public boolean connectWallet(final Context app) {
        BRExecutor.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                getPeerManager().connect();
            }
        });

        return true;
    }


    @Override
    public String getSymbol(Context app) {

        SymbolUtils symbolUtils = new SymbolUtils();
        String currencySymbolString = BRConstants.symbolRotunda;
        if (app != null) {
            int unit = BRSharedPrefs.getCryptoDenomination(app, getIso(app));
            switch (unit) {
                case BRConstants.CURRENT_UNIT_URVN:
                    if (symbolUtils.doesDeviceSupportSymbol(BRConstants.symbolRavenPrimary)) {
                        currencySymbolString = "µ" + BRConstants.symbolRavenPrimary;
                    } else {
                        currencySymbolString = "µ" + BRConstants.symbolRavenSecondary;
                    }
                    break;
                case BRConstants.CURRENT_UNIT_MRVN:
                    if (symbolUtils.doesDeviceSupportSymbol(BRConstants.symbolRavenPrimary)) {
                        currencySymbolString = "m" + BRConstants.symbolRavenPrimary;
                    } else {
                        currencySymbolString = "m" + BRConstants.symbolRavenSecondary;
                    }
                    break;
                case BRConstants.CURRENT_UNIT_RAVENS:

                    if (symbolUtils.doesDeviceSupportSymbol(BRConstants.symbolRavenPrimary)) {
                        currencySymbolString = BRConstants.symbolRavenPrimary;

                    } else {
                        currencySymbolString = BRConstants.symbolRavenPrimary;

                    }
                    break;
            }
        }
        return currencySymbolString;
    }

    @Override
    public String getIso(Context app) {
        return ISO;
    }

    @Override
    public String getScheme(Context app) {
        return RVN_SCHEME;
    }

    @Override
    public String getName(Context app) {
        return mName;
    }

    @Override
    public String getDenomination(Context app) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public BRCoreAddress getReceiveAddress(Context app) {
        return getWallet().getReceiveAddress();
    }

    @Override
    public String decorateAddress(Context app, String addr) {
        return addr; // no need to decorate
    }

    @Override
    public String undecorateAddress(Context app, String addr) {
        return addr; //no need to undecorate
    }

    @Override
    public int getMaxDecimalPlaces(Context app) {
        int unit = BRSharedPrefs.getCryptoDenomination(app, getIso(app));
        switch (unit) {
            case BRConstants.CURRENT_UNIT_URVN:
                return 5;
            case BRConstants.CURRENT_UNIT_MRVN:
                return 8;
            default:
                return 8;
        }
    }

    @Override
    public long getCachedBalance(Context app) {
        return BRSharedPrefs.getCachedBalance(app, getIso(app));
    }

    @Override
    public long getTotalSent(Context app) {
        return getWallet().getTotalSent();
    }

    @Override
    public void wipeData(Context app) {
        RvnTransactionDataStore.getInstance(app).deleteAllTransactions(app, getIso(app));
        MerkleBlockDataSource.getInstance(app).deleteAllBlocks(app, getIso(app));
        PeerDataSource.getInstance(app).deleteAllPeers(app, getIso(app));
        AssetsRepository.getInstance(app).deleteAllAssets();
        BRSharedPrefs.clearAllPrefs(app);
    }

    @Override
    public void setCashedBalance(Context app, long balance) {
        BRSharedPrefs.putCachedBalance(app, getIso(app), balance);
        refreshAddress(app);
        for (OnBalanceChangedListener listener : balanceListeners) {
            if (listener != null) listener.onBalanceChanged(getIso(app), balance);
        }

    }

    @Override
    public void refreshAddress(Context app) {
        BRCoreAddress address = getReceiveAddress(app);
        if (Utils.isNullOrEmpty(address.stringify())) {
            Log.e(TAG, "refreshAddress: WARNING, retrieved address:" + address);
        }
        BRSharedPrefs.putReceiveAddress(app, address.stringify(), getIso(app));

    }

    @Override
    public BigDecimal getMaxAmount(Context app) {
        //return max raven
        return new BigDecimal(MAX_RVN);
    }

    @Override
    public WalletUiConfiguration getUiConfiguration() {
        return uiConfig;
    }

    @Override
    public BigDecimal getFiatExchangeRate(Context app) {
        CurrencyEntity ent = CurrencyDataSource.getInstance(app).getCurrencyByCode(app, getIso(app), BRSharedPrefs.getPreferredFiatIso(app));
        return new BigDecimal(ent == null ? 0 : ent.rate); //dollars
    }

    @Override
    public BigDecimal getFiatBalance(Context app) {
        if (app == null) return null;
        BigDecimal bal = getFiatForSmallestCrypto(app, new BigDecimal(getCachedBalance(app)), null);
        return new BigDecimal(bal == null ? 0 : bal.doubleValue());
    }

    @Override
    public BigDecimal getFiatForSmallestCrypto(Context app, BigDecimal amount, CurrencyEntity ent) {
        if (amount.doubleValue() == 0) return amount;
        String iso = BRSharedPrefs.getPreferredFiatIso(app);
        if (ent == null)
            ent = CurrencyDataSource.getInstance(app).getCurrencyByCode(app, getIso(app), iso);
        if (ent == null) {
            return null;
        }
        double rate = ent.rate;
        //get crypto amount
        BigDecimal cryptoAmount = amount.divide(new BigDecimal(100000000), 8, BRConstants.ROUNDING_MODE);
        return cryptoAmount.multiply(new BigDecimal(rate));
    }

    @Override
    public BigDecimal getCryptoForFiat(Context app, BigDecimal fiatAmount) {
        if (fiatAmount.doubleValue() == 0) return fiatAmount;
        String iso = BRSharedPrefs.getPreferredFiatIso(app);
        CurrencyEntity ent = CurrencyDataSource.getInstance(app).getCurrencyByCode(app, getIso(app), iso);
        if (ent == null) return null;
        double rate = ent.rate;
        //convert c to $.
        int unit = BRSharedPrefs.getCryptoDenomination(app, getIso(app));
        BigDecimal result = new BigDecimal(0);
        switch (unit) {
            case BRConstants.CURRENT_UNIT_URVN:
                result = fiatAmount.divide(new BigDecimal(rate), 2, ROUNDING_MODE).multiply(new BigDecimal("1000000"));
                break;
            case BRConstants.CURRENT_UNIT_MRVN:
                result = fiatAmount.divide(new BigDecimal(rate), 5, ROUNDING_MODE).multiply(new BigDecimal("100000"));
                break;
            case BRConstants.CURRENT_UNIT_RAVENS:
                result = fiatAmount.divide(new BigDecimal(rate), 8, ROUNDING_MODE);
                break;
        }
        return result;

    }

    @Override
    public BigDecimal getCryptoForSmallestCrypto(Context app, BigDecimal amount) {
        if (amount.doubleValue() == 0) return amount;
        BigDecimal result = new BigDecimal(0);
        int unit = BRSharedPrefs.getCryptoDenomination(app, getIso(app));
        switch (unit) {
            case BRConstants.CURRENT_UNIT_URVN:
                result = amount.divide(new BigDecimal("100"), 2, ROUNDING_MODE);
                break;
            case BRConstants.CURRENT_UNIT_MRVN:
                result = amount.divide(new BigDecimal("100000"), 5, ROUNDING_MODE);
                break;
            case BRConstants.CURRENT_UNIT_RAVENS:
                result = amount.divide(new BigDecimal("100000000"), 8, ROUNDING_MODE);
                break;
        }
        return result;
    }

    @Override
    public BigDecimal getSmallestCryptoForCrypto(Context app, BigDecimal amount) {
        if (amount.doubleValue() == 0) return amount;
        BigDecimal result = new BigDecimal(0);
        int unit = BRSharedPrefs.getCryptoDenomination(app, getIso(app));
        switch (unit) {
            case BRConstants.CURRENT_UNIT_URVN:
                result = amount.multiply(new BigDecimal("1"));
                break;
            case BRConstants.CURRENT_UNIT_MRVN:
                result = amount.multiply(new BigDecimal("100000"));
                break;
            case BRConstants.CURRENT_UNIT_RAVENS:
                result = amount.multiply(new BigDecimal("100000000"));
                break;
        }
        return result;
    }

    @Override
    public BigDecimal getSmallestCryptoForFiat(Context app, BigDecimal amount) {
        if (amount.doubleValue() == 0) return amount;
        String iso = BRSharedPrefs.getPreferredFiatIso(app);
        CurrencyEntity ent = CurrencyDataSource.getInstance(app).getCurrencyByCode(app, getIso(app), iso);
        if (ent == null) {
            Log.e(TAG, "getSmallestCryptoForFiat: no exchange rate data!");
            return amount;
        }
        double rate = ent.rate;
        //convert c to $.
        return amount.divide(new BigDecimal(rate), 8, ROUNDING_MODE).multiply(new BigDecimal("100000000"));
    }

    @Override
    public int getForkId() {
        return super.getForkId();
    }

    @Override
    public void addBalanceChangedListener(OnBalanceChangedListener listener) {
        if (listener != null && !balanceListeners.contains(listener))
            balanceListeners.add(listener);
    }

    @Override
    public void addTxStatusUpdatedListener(OnTxStatusUpdatedListener list) {
        if (list != null && !txStatusUpdatedListeners.contains(list))
            txStatusUpdatedListeners.add(list);
    }

    @Override
    public void addSyncListeners(SyncListener list) {
        if (list != null && !syncListeners.contains(list))
            syncListeners.add(list);
    }

    @Override
    public void addTxListModifiedListener(OnTxListModified list) {
        if (list != null && !txModifiedListeners.contains(list))
            txModifiedListeners.add(list);
    }


    @Override
    public void txPublished(final String error) {
        super.txPublished(error);
        final Context app = RavenApp.getRvnContext();
      /*  if (Utils.isNullOrEmpty(error)) return;
        BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
            @Override
            public void run() {
                if (app instanceof Activity)
                    BRAnimator.showRvnSignal((Activity) app, app.getString(R.string.Alert_error),
                            "Error: " + error, R.drawable.ic_error_outline_black_24dp, new BROnSignalCompletion() {
                                @Override
                                public void onComplete() {
                                    if (!((Activity) app).isDestroyed())
                                        ((Activity) app).getFragmentManager().popBackStack();
                                }
                            });

            }
        });*/
        if (!isTransfer) return;
        BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
            @Override
            public void run() {
                if (app instanceof Activity) {
                    BRAnimator.showRvnSignal((Activity) app, Utils.isNullOrEmpty(error) ? app.getString(R.string.Alerts_sendSuccess) : app.getString(R.string.Alert_error),
                            Utils.isNullOrEmpty(error) ? app.getString(R.string.Alerts_sendSuccessSubheader) : error, Utils.isNullOrEmpty(error) ? R.drawable.ic_check_mark_white : R.drawable.ic_error_outline_black_24dp, new BROnSignalCompletion() {
                                @Override
                                public void onComplete() {
                                    if (!((Activity) app).isDestroyed()) {
                                        ((Activity) app).getFragmentManager().popBackStack();
                                    }
                                }
                            });
                }

            }
        });
    }

    @Override
    public void balanceChanged(long balance) {
        super.balanceChanged(balance);
        Context app = RavenApp.getRvnContext();
        setCashedBalance(app, balance);
        for (OnTxListModified list : txModifiedListeners)
            if (list != null) list.txListModified(null);

    }

    @Override
    public void txStatusUpdate() {
        super.txStatusUpdate();
        for (OnTxStatusUpdatedListener listener : txStatusUpdatedListeners)
            if (listener != null) listener.onTxStatusUpdated();
        for (OnTxListModified list : txModifiedListeners)
            if (list != null) list.txListModified(null);
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                long blockHeight = getPeerManager().getLastBlockHeight();

                final Context ctx = RavenApp.getRvnContext();
                if (ctx == null) return;
                BRSharedPrefs.putLastBlockHeight(ctx, getIso(ctx), (int) blockHeight);
            }
        });
    }

    @Override
    public void saveBlocks(boolean replace, BRCoreMerkleBlock[] blocks) {
        super.saveBlocks(replace, blocks);

        Context app = RavenApp.getRvnContext();
        if (app == null) return;
        if (replace) MerkleBlockDataSource.getInstance(app).deleteAllBlocks(app, getIso(app));
        BlockEntity[] entities = new BlockEntity[blocks.length];
        for (int i = 0; i < entities.length; i++) {
            entities[i] = new BlockEntity(blocks[i].serialize(), (int) blocks[i].getHeight());
        }

        MerkleBlockDataSource.getInstance(app).putMerkleBlocks(app, getIso(app), entities);
    }

    @Override
    public void savePeers(boolean replace, BRCorePeer[] peers) {
        super.savePeers(replace, peers);
        Context app = RavenApp.getRvnContext();
        if (app == null) return;
        if (replace) PeerDataSource.getInstance(app).deleteAllPeers(app, getIso(app));
        PeerEntity[] entities = new PeerEntity[peers.length];
        for (int i = 0; i < entities.length; i++) {
            entities[i] = new PeerEntity(peers[i].getAddress(), TypesConverter.intToBytes(peers[i].getPort()), TypesConverter.long2byteArray(peers[i].getTimestamp()));
        }
        PeerDataSource.getInstance(app).putPeers(app, getIso(app), entities);

    }

    @Override
    public boolean networkIsReachable() {
        Context app = RavenApp.getRvnContext();
        return InternetManager.getInstance().isConnected(app);
    }


    @Override
    public BRCoreTransaction[] loadTransactions() {
        Context app = RavenApp.getRvnContext();

        List<BRTransactionEntity> txs = RvnTransactionDataStore.getInstance(app).getAllTransactions(app, getIso(app));
        if (txs == null || txs.size() == 0) return new BRCoreTransaction[0];
        BRCoreTransaction arr[] = new BRCoreTransaction[txs.size()];
        for (int i = 0; i < txs.size(); i++) {
            BRTransactionEntity ent = txs.get(i);
            arr[i] = new BRCoreTransaction(ent.getBuff(), ent.getBlockheight(), ent.getTimestamp());
        }
        return arr;
    }

    @Override
    public BRCoreMerkleBlock[] loadBlocks() {
        Context app = RavenApp.getRvnContext();
        List<BRMerkleBlockEntity> blocks = MerkleBlockDataSource.getInstance(app).getAllMerkleBlocks(app, getIso(app));
        if (blocks == null || blocks.size() == 0) return new BRCoreMerkleBlock[0];
        BRCoreMerkleBlock arr[] = new BRCoreMerkleBlock[blocks.size()];
        for (int i = 0; i < blocks.size(); i++) {
            BRMerkleBlockEntity ent = blocks.get(i);
            arr[i] = new BRCoreMerkleBlock(ent.getBuff(), ent.getBlockHeight());
        }
        return arr;
    }

    @Override
    public BRCorePeer[] loadPeers() {
        Context app = RavenApp.getRvnContext();
        List<BRPeerEntity> peers = PeerDataSource.getInstance(app).getAllPeers(app, getIso(app));
        if (peers == null || peers.size() == 0) return new BRCorePeer[0];
        BRCorePeer arr[] = new BRCorePeer[peers.size()];
        for (int i = 0; i < peers.size(); i++) {
            BRPeerEntity ent = peers.get(i);
            arr[i] = new BRCorePeer(ent.getAddress(), TypesConverter.bytesToInt(ent.getPort()), TypesConverter.byteArray2long(ent.getTimeStamp()));
        }
        return arr;
    }

    @Override
    public void syncStarted() {
        super.syncStarted();
        Log.d(TAG, "syncStarted: ");
        final Context app = RavenApp.getRvnContext();
        if (Utils.isEmulatorOrDebug(app))
            BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(app, "syncStarted " + getIso(app), Toast.LENGTH_LONG).show();
                }
            });

        for (SyncListener list : syncListeners)
            if (list != null) list.syncStarted();

    }

    @Override
    public void syncStopped(final String error) {
        super.syncStopped(error);
        Log.d(TAG, "syncStopped: " + error);
        final Context app = RavenApp.getRvnContext();
        if (Utils.isNullOrEmpty(error))
            BRSharedPrefs.putAllowSpend(app, getIso(app), true);
        for (SyncListener list : syncListeners)
            if (list != null) list.syncStopped(error);
        if (Utils.isEmulatorOrDebug(app))
            BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(app, "SyncStopped " + getIso(app) + " err(" + error + ") ", Toast.LENGTH_LONG).show();
                }
            });

        Log.e(TAG, "syncStopped: peerManager:" + getPeerManager().toString());

        if (!Utils.isNullOrEmpty(error)) {
            if (mSyncRetryCount < SYNC_MAX_RETRY) {
                Log.e(TAG, "syncStopped: Retrying: " + mSyncRetryCount);
                //Retry
                mSyncRetryCount++;
                BRExecutor.getInstance().forBackgroundTasks().execute(new Runnable() {
                    @Override
                    public void run() {
                        getPeerManager().connect();
                    }
                });

            } else {
                //Give up
                Log.e(TAG, "syncStopped: Giving up: " + mSyncRetryCount);
                mSyncRetryCount = 0;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(app, "Syncing failed, retried " + SYNC_MAX_RETRY + " times.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

    }

    @Override
    public void onTxAdded(BRCoreTransaction transaction) {
        super.onTxAdded(transaction);
        final Context ctx = RavenApp.getRvnContext();
        final WalletsMaster master = WalletsMaster.getInstance(ctx);
//        TxMetaData metaData = KVStoreManager.getInstance().createMetadata(ctx, this, transaction);
//        KVStoreManager.getInstance().putTxMetaData(ctx, metaData, transaction.getHash());
        final long amount = getWallet().getTransactionAmount(transaction);
        if (amount > 0) {
            BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                @Override
                public void run() {
                    String am = CurrencyUtils.getFormattedAmount(ctx, getIso(ctx), new BigDecimal(amount)/*getCryptoForSmallestCrypto(ctx, new BigDecimal(amount))*/);
                    BigDecimal bigAmount = master.getCurrentWallet(ctx).getFiatForSmallestCrypto(ctx, new BigDecimal(amount), null);
                    String amCur = CurrencyUtils.getFormattedAmount(ctx, BRSharedPrefs.getPreferredFiatIso(ctx), bigAmount == null ? new BigDecimal(0) : bigAmount);
                    String formatted = String.format("%s (%s)", am, amCur);
                    final String strToShow = String.format(ctx.getString(R.string.TransactionDetails_received), formatted);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!BRToast.isToastShown()) {
                                if (Utils.isEmulatorOrDebug(ctx))
                                    BRToast.showCustomToast(ctx, strToShow,
                                            RavenApp.DISPLAY_HEIGHT_PX / 2, Toast.LENGTH_LONG, R.drawable.toast_layout_black);
                                AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
                                if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                                    try {
                                        final MediaPlayer mp = MediaPlayer.create(ctx, R.raw.coinflip);
                                        mp.start();
                                    } catch (Exception ex) {
                                        Log.e(TAG, "run: ", ex);
                                    }
                                }
                                if (ctx instanceof Activity && BRSharedPrefs.getShowNotification(ctx))
                                    BRNotificationManager.sendNotification((Activity) ctx, R.drawable.notification_icon, ctx.getString(R.string.app_name), strToShow, 1);
                                else
                                    Log.e(TAG, "onTxAdded: ctx is not activity");
                            }
                        }
                    }, 1000);


                }
            });
        }

        if (ctx != null) {
            String txHash = BRCoreKey.encodeHex(transaction.getHash());
            TransactionStorageManager.putTransaction(ctx, getIso(ctx),
                    new BRTransactionEntity(transaction.serialize(), transaction.getBlockHeight(),
                            transaction.getTimestamp(), txHash, getIso(ctx)));
            if (transaction.hasAsset() && !transaction.getAsset().getType().equals(NEW_ASSET.name())
                    && !transaction.getAsset().getType().equals(REISSUE.name())) {
                assetAdded(transaction, ctx, txHash);
            }
        } else
            Log.e(TAG, "onTxAdded: ctx is null!");
        for (OnTxListModified list : txModifiedListeners)
            if (list != null) list.txListModified(transaction.getReverseHash());
    }

    private void assetAdded(BRCoreTransaction transaction, Context ctx, String txHash) {
        MyTransactionAsset transactionAsset = transaction.getAsset();
        if (NEW_ASSET.name().equals(transactionAsset.getType()) || AssetType.REISSUE.name().equals(transactionAsset.getType())) {
            //BMEX should dont write asset if not confirmed
            long blockHeight = transaction.getBlockHeight();
            long lastBlockHeight = getPeerManager().getLastBlockHeight();
            long confirmations = blockHeight > lastBlockHeight ? 0 : (lastBlockHeight - blockHeight) + 1;
            if (!getWallet().transactionIsValid(transaction) || confirmations == 0) {
                return;
            }
        }

        ArrayList<BRCoreTransaction> decomposedTransactions = getDecomposedTransactions(transaction);
        if (decomposedTransactions != null)
            for (BRCoreTransaction tx : decomposedTransactions)
                if (tx.hasAsset())
                    addAsset(ctx, tx, txHash);
    }

    private void addAsset(Context ctx, BRCoreTransaction tx, String txHash) {
        long sent = getWallet().getTransactionAmountSent(tx);
        boolean received = sent == 0;
        MyTransactionAsset transactionAsset = tx.getAsset();
        String assetName = transactionAsset.getName();
        boolean isNameOwner = isAssetNameAnOwner(assetName);
        if (isNameOwner) {
            assetName = assetName.substring(0, assetName.length() - 1);
        }
        AssetsRepository repository = AssetsRepository.getInstance(ctx);
        Asset localAsset = repository.getAsset(assetName);
        if (localAsset == null) {
            double mAmount = isNameOwner ? 0 : transactionAsset.getAmount();
            transactionAsset.setAmount(mAmount);
            transactionAsset.setName(assetName);
            repository.insertAsset(new Asset(transactionAsset, txHash, isNameOwner ? 1 : 0, repository.getAssetCount()));
        } else {
            if (isNameOwner) {
                repository.updateOwnerShip(assetName, received ? 1 : 0);
            } else if (!TextUtils.isEmpty(transactionAsset.getType()))
                switch (AssetType.valueOf(transactionAsset.getType())) {
                    case NEW_ASSET:
                        localAsset.setReissuable(transactionAsset.getReissuable());
                        localAsset.setUnits(transactionAsset.getUnit());
                        localAsset.setHasIpfs(transactionAsset.getHasIPFS());
                        localAsset.setAmount(transactionAsset.getAmount());
                        localAsset.setIpfsHash(transactionAsset.getIPFSHash());
                        localAsset.setTxHash(txHash);
                        localAsset.setOwnership(1);
                        repository.updateAsset(localAsset);
                        break;
                    case REISSUE:
                        localAsset.setAmount(localAsset.getAmount() + transactionAsset.getAmount());
                        localAsset.setUnits(transactionAsset.getUnit());
                        localAsset.setReissuable(transactionAsset.getReissuable());
                        localAsset.setHasIpfs(transactionAsset.getHasIPFS());
                        localAsset.setIpfsHash(transactionAsset.getIPFSHash());
                        localAsset.setTxHash(txHash);
                        repository.updateAsset(localAsset);
                        break;
                    case TRANSFER:
                        double newAmount = localAsset.getAmount();
                        if (received) { // increment existing amount
                            newAmount += transactionAsset.getAmount();
                        } else if (transactionAsset.getAmount() < newAmount) {
                            newAmount -= transactionAsset.getAmount();
                        } else { // balence <=0
                            repository.deleteAsset(assetName);
                        }
                        repository.updateAmount(assetName, newAmount);
                        break;
                    case OWNER:
                        repository.updateOwnerShip(assetName, 1);
                        break;
                }
        }
    }

    public ArrayList<BRCoreTransaction> getDecomposedTransactions(BRCoreTransaction tx) {
        ArrayList<BRCoreTransaction> decomposedTransactions = new ArrayList<>();
        if (tx.hasAsset()) {
            if (NEW_ASSET.name().equals(tx.getAsset().getType()) || REISSUE.name().equals(tx.getAsset().getType())) {
                BRCoreTransaction[] txList = getWallet().decomposeTransaction(tx);
                if (txList != null) {
                    decomposedTransactions.addAll(Lists.newArrayList(txList));
                }
            } else {
                decomposedTransactions.add(tx);
            }

        } else {
            decomposedTransactions.add(tx);
        }
        return decomposedTransactions;
    }

    @Override
    public void onTxDeleted(final String hash, int notifyUser, int recommendRescan) {
        super.onTxDeleted(hash, notifyUser, recommendRescan);
        Log.e(TAG, "onTxDeleted: " + String.format("hash: %s, notifyUser: %d, recommendRescan: %d", hash, notifyUser, recommendRescan));
        final Context ctx = RavenApp.getRvnContext();
        if (ctx != null) {
            if (recommendRescan != 0)
                BRSharedPrefs.putScanRecommended(ctx, getIso(ctx), true);
            if (notifyUser != 0)
                BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                    @Override
                    public void run() {
                        BRDialog.showSimpleDialog(ctx, "Transaction failed!", hash);
                    }
                });
            TransactionStorageManager.removeTransaction(ctx, getIso(ctx), hash);
        } else {
            Log.e(TAG, "onTxDeleted: Failed! ctx is null");
        }
        for (OnTxListModified list : txModifiedListeners)
            if (list != null) list.txListModified(hash);
    }

    @Override
    public void onTxUpdated(final String hash, int blockHeight, int timeStamp) {
        super.onTxUpdated(hash, blockHeight, timeStamp);
        Log.d(TAG, "onTxUpdated: " + String.format("hash: %s, blockHeight: %d, timestamp: %d", hash, blockHeight, timeStamp));
        Context ctx = RavenApp.getRvnContext();
        if (ctx != null) {
            TransactionStorageManager.updateTransaction(ctx, getIso(ctx), new BRTransactionEntity(null, blockHeight, timeStamp, hash, getIso(ctx)));
            if (getWallet().getTransactions() != null)
                for (BRCoreTransaction transaction : getWallet().getTransactions()) {
                    String txHash = null;
                    try {
                        txHash = BRCoreKey.encodeHex(transaction.getHash());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!hash.equals(txHash) || !transaction.hasAsset())
                        continue;

                    if (transaction.getAsset().getType().equals(NEW_ASSET.name())
                            || transaction.getAsset().getType().equals(REISSUE.name())) {
                        assetAdded(transaction, ctx, hash);
                    }
                }
        } else {
            Log.e(TAG, "onTxUpdated: Failed, ctx is null");
        }
        BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
            @Override
            public void run() {
                for (OnTxListModified list : txModifiedListeners)
                    if (list != null) list.txListModified(hash);
            }
        });

    }


    @Override
    public long getRelayCount(byte[] txHash) {
        if (Utils.isNullOrEmpty(txHash)) return 0;
        return getPeerManager().getRelayCount(txHash);
    }

    @Override
    public double getSyncProgress(long startHeight) {
        return getPeerManager().getSyncProgress(startHeight);
    }

    @Override
    public double getConnectStatus() {
        BRCorePeer.ConnectStatus status = getPeerManager().getConnectStatus();
        if (status == BRCorePeer.ConnectStatus.Disconnected)
            return 0;
        else if (status == BRCorePeer.ConnectStatus.Connecting)
            return 1;
        else if (status == BRCorePeer.ConnectStatus.Connected)
            return 2;
        else if (status == BRCorePeer.ConnectStatus.Unknown)
            return 3;
        else
            throw new IllegalArgumentException();
    }

    @Override
    public void removeSyncListener(SyncListener listener) {
        if (listener != null && syncListeners.contains(listener)) {
            syncListeners.remove(listener);
        }
    }


    public void requestConfirmation(final Activity app, final AssetType assetType,
                                    final BRCoreTransactionAsset asset, final BRCoreTransactionAsset rootAsset,
                                    final String address, final boolean isTransferOwnership,
                                    final WalletManagerListener listener) {
        boolean isExpertMode = BRSharedPrefs.getExpertMode(app);
        BRAnimator.showConfirmationFragment(app, new Asset(asset), assetType, isExpertMode ? address : "",
                new ConfirmationListener() {
                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onConfirm() {
                        requestPin(app, assetType, getWallet(), address, asset, rootAsset, isTransferOwnership, listener);
                    }
                });
    }


    private void requestPin(final Activity app, final AssetType assetType, final BRCoreWallet wallet,
                            final String address, final BRCoreTransactionAsset asset,
                            final BRCoreTransactionAsset rootAsset, final boolean isTransferOwnership,
                            final WalletManagerListener listener) {
        final FragmentPin breadPin = new FragmentPin();
        Bundle args = new Bundle();
        args.putString("title", "");
        args.putString("message", "");
        breadPin.setArguments(args);
        breadPin.setCompletion(new BRAuthCompletion() {
            @Override
            public void onComplete() {
                BRCoreTransaction transaction = null;
                String errorMsg = "";
                switch (assetType) {
                    case NEW_ASSET: {
                        final long amount = CREATION_FEE * (new BigDecimal(SATOSHIS).longValue());
                        BRCoreAddress brAddress = new BRCoreAddress(address);
                        transaction = wallet.createAssetTransaction(amount, brAddress, asset);
                        errorMsg = "can't create asset";
                        break;
                    }
                    case REISSUE:
                        transaction = wallet.reissueAsset(REISSUE_FEE * SATOSHIS, address, asset);
                        errorMsg = "can't reissue asset";
                        break;
                    case SUB: {
                        final long amount = SUB_FEE * (new BigDecimal(SATOSHIS).longValue());
                        BRCoreAddress brAddress = new BRCoreAddress(address);
                        transaction = wallet.createSubAssetTransaction(amount,
                                brAddress, asset, rootAsset);
                        errorMsg = "can't create sub asset";
                        break;
                    }
                    case UNIQUE: {
                        final long amount = UNIQUE_FEE * (new BigDecimal(SATOSHIS).longValue());
                        BRCoreAddress brAddress = new BRCoreAddress(address);
                        transaction = wallet.createUniqueAssetTransaction(amount,
                                brAddress, asset, rootAsset);
                        errorMsg = "can't create unique asset";
                        break;
                    }
                    case TRANSFER: {
                        if (isTransferOwnership)
                            transaction = wallet.transferOwnerShipAsset(0, address, asset);
                        else
                            transaction = wallet.transferAsset(0, address, asset);
                        errorMsg = "can't transfer asset";
                        break;
                    }
                    case BURN: {
                        asset.setType(TRANSFER.getIndex());
                        transaction = wallet.burnAsset(asset);
                        errorMsg = "can't burn asset";
                        break;
                    }
                }
                if (transaction == null) {
                    //TODO if transaction null
                    return;
                }
                byte[] phrase = getPhrase(app);
                if (Utils.isNullOrEmpty(phrase)) {
                    //TODO if no phrase
                    return;
                }
                isTransfer = assetType == TRANSFER || assetType == BURN;
                byte[] bTxHash = signAndPublishTransaction(transaction, phrase);
                listener.close();
                if (bTxHash != null) {
                    if (assetType != TRANSFER && assetType != BURN) {
                        String txHash = BRCoreKey.encodeHex(transaction.getHash());
                        BRAnimator.showTxCreatedFragment(app, txHash);
                    }

                } else listener.error(errorMsg);
            }

            @Override
            public void onCancel() {
            }
        });
        FragmentTransaction fragmentTransaction = app.getFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(0, 0, 0, R.animator.plain_300);
        fragmentTransaction.add(android.R.id.content, breadPin, breadPin.getClass().getName());
        fragmentTransaction.addToBackStack(null);
        if (!app.isDestroyed()) {
            fragmentTransaction.commit();
        }
    }

    private byte[] getPhrase(Activity app) {
        byte[] phrase = null;
        try {
            phrase = BRKeyStore.getPhrase(app, 22);
        } catch (UserNotAuthenticatedException e) {
            e.printStackTrace();
        }
        return phrase;
    }
}
