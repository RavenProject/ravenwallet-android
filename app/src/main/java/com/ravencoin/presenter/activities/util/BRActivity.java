package com.ravencoin.presenter.activities.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;

//import com.platform.HTTPServer;
import com.platform.addressBook.AddressBookItem;
import com.ravencoin.RavenApp;
import com.ravencoin.R;
import com.ravencoin.presenter.activities.DisabledActivity;
import com.ravencoin.presenter.activities.WalletActivity;
import com.ravencoin.presenter.activities.intro.IntroActivity;
import com.ravencoin.presenter.activities.intro.RecoverActivity;
import com.ravencoin.presenter.activities.intro.WriteDownActivity;
import com.ravencoin.presenter.fragments.BaseAddressAndIpfsHashValidation;
import com.ravencoin.presenter.fragments.BaseAddressValidation;
import com.ravencoin.tools.animation.BRAnimator;
import com.ravencoin.tools.manager.BRApiManager;
import com.ravencoin.tools.manager.InternetManager;
import com.ravencoin.tools.security.AuthManager;
import com.ravencoin.tools.security.BRKeyStore;
import com.ravencoin.tools.security.PostAuth;
import com.ravencoin.tools.threads.executor.BRExecutor;
import com.ravencoin.tools.util.BRConstants;
import com.ravencoin.tools.util.Utils;
import com.ravencoin.wallet.WalletsMaster;
import com.ravencoin.wallet.wallets.util.CryptoUriParser;

public class BRActivity extends Activity {
    private static final String TAG = BRActivity.class.getName();
    public static final Point screenParametersPoint = new Point();


    static {
        System.loadLibrary(BRConstants.NATIVE_LIB_NAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        RavenApp.activityCounter.decrementAndGet();
        RavenApp.onStop(this);
    }

    @Override
    protected void onResume() {
        init(this);
        super.onResume();
        RavenApp.backgroundedTime = 0;

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        // 123 is the qrCode result
        switch (requestCode) {

            case BRConstants.PAY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            PostAuth.getInstance().onPublishTxAuth(BRActivity.this, true);
                        }
                    });
                }
                break;
//            case BRConstants.REQUEST_PHRASE_BITID:
//                if (resultCode == RESULT_OK) {
//                    BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
//                        @Override
//                        public void run() {
//                            PostAuth.getInstance().onBitIDAuth(BRActivity.this, true);
//                        }
//                    });
//
//                }
//                break;

            case BRConstants.PAYMENT_PROTOCOL_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            PostAuth.getInstance().onPaymentProtocolRequest(BRActivity.this, true);
                        }
                    });

                }
                break;

            case BRConstants.CANARY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            PostAuth.getInstance().onCanaryCheck(BRActivity.this, true);
                        }
                    });
                } else {
                    finish();
                }
                break;

            case BRConstants.SHOW_PHRASE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            PostAuth.getInstance().onPhraseCheckAuth(BRActivity.this, true);
                        }
                    });
                }
                break;
            case BRConstants.PROVE_PHRASE_REQUEST:
                if (resultCode == RESULT_OK) {
                    BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            PostAuth.getInstance().onPhraseProveAuth(BRActivity.this, true);
                        }
                    });
                }
                break;
            case BRConstants.PUT_PHRASE_RECOVERY_WALLET_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            PostAuth.getInstance().onRecoverWalletAuth(BRActivity.this, true);
                        }
                    });
                } else {
                    finish();
                }
                break;

            case BRConstants.SCANNER_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            String result = data.getStringExtra("result");
                            if (CryptoUriParser.isCryptoUrl(BRActivity.this, result))
                                CryptoUriParser.processRequest(BRActivity.this, result,
                                        WalletsMaster.getInstance(BRActivity.this).getCurrentWallet(BRActivity.this));
//                            else if (BRBitId.isBitId(result))
//                                BRBitId.signBitID(BRActivity.this, result, null);
                            else
                                Log.e(TAG, "onActivityResult: not bitcoin address NOR bitID");
                        }
                    });
                }
                break;

            case BRConstants.ADDRESS_SCANNER_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            String result = data.getStringExtra("result");
                            final String address = Utils.retrieveAddressChunk(result);
                            if (address != null) {
                                final BaseAddressValidation fragment = (BaseAddressValidation)
                                        getFragmentManager().
                                                findFragmentById(android.R.id.content);
                                if (!fragment.isAddressValid(address)) {
                                    fragment.sayInvalidClipboardData();
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            fragment.setAddress(address);
                                        }
                                    });
                                }
                            } else {
                                Log.e(TAG, "onActivityResult: not RavenCoin address");
                            }
                        }
                    });
                }
                break;

            case BRConstants.IPFS_HASH_SCANNER_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            final String ipfsHash = data.getStringExtra("result");
                            if (ipfsHash != null) {
                                final BaseAddressAndIpfsHashValidation fragment = (BaseAddressAndIpfsHashValidation)
                                        getFragmentManager().
                                                findFragmentById(android.R.id.content);
                                if (!fragment.isIPFSHashValid(ipfsHash)) {
                                    fragment.sayInvalidIPFSHash();
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            fragment.setIPFSHash(ipfsHash);
                                        }
                                    });
                                }
                            } else {
                                Log.e(TAG, "onActivityResult: not RavenCoin address");
                            }
                        }
                    });
                }
                break;

            case BRConstants.PUT_PHRASE_NEW_WALLET_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            PostAuth.getInstance().onCreateWalletAuth(BRActivity.this, true);
                        }
                    });

                } else {
                    Log.e(TAG, "WARNING: resultCode != RESULT_OK");
                    WalletsMaster m = WalletsMaster.getInstance(BRActivity.this);
                    m.wipeWalletButKeystore(this);
                    finish();
                }
                break;

            case BRConstants.SELECT_FROM_ADDRESS_BOOK_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            final AddressBookItem address = data.getParcelableExtra("result");
                            if (address != null) {
                                final BaseAddressValidation fragment = (BaseAddressValidation)
                                        getFragmentManager().
                                                findFragmentById(android.R.id.content);
                                if (!fragment.isAddressValid(address.getAddress())) {
                                    fragment.sayInvalidClipboardData();
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            fragment.setAddress(address.getAddress());
                                        }
                                    });
                                }
                            } else {
                                Log.e(TAG, "onActivityResult: not RavenCoin address");
                            }
                        }
                    });
                }
                break;

        }
    }

    public void init(Activity app) {
        //set status bar color
//        ActivityUTILS.setStatusBarColor(app, android.R.color.transparent);
        InternetManager.getInstance();
        if (!(app instanceof IntroActivity || app instanceof RecoverActivity || app instanceof WriteDownActivity))
            BRApiManager.getInstance().startTimer(app);
        //show wallet locked if it is
        if (!ActivityUTILS.isAppSafe(app))
            if (AuthManager.getInstance().isWalletDisabled(app))
                AuthManager.getInstance().setWalletDisabled(app);

        RavenApp.activityCounter.incrementAndGet();
        RavenApp.setRvnContext(app);

//        if (!HTTPServer.isStarted())
//            BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
//                @Override
//                public void run() {
//                    HTTPServer.startServer();
//                }
//            });

        lockIfNeeded(this);

    }

    private void lockIfNeeded(Activity app) {
        //lock wallet if 3 minutes passed
        if (RavenApp.backgroundedTime != 0
                && ((System.currentTimeMillis() - RavenApp.backgroundedTime) >= 180 * 1000)
                && !(app instanceof DisabledActivity)) {
            if (!BRKeyStore.getPinCode(app).isEmpty()) {
                Log.e(TAG, "lockIfNeeded: " + RavenApp.backgroundedTime);
                BRAnimator.startRvnActivity(app, true);
            }
        }

    }

    protected void finalizeIntent(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        startActivity(intent);
        if (isDestroyed()) finish();
        Activity app = WalletActivity.getApp();
        if (app != null && !app.isDestroyed()) app.finish();
    }

}
