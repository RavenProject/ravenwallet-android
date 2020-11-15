package com.ravenwallet.tools.manager;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.ravenwallet.R;
import com.ravenwallet.core.BRCoreAddress;
import com.ravenwallet.core.BRCoreTransaction;
import com.ravenwallet.presenter.customviews.BRDialogView;
import com.ravenwallet.presenter.entities.CryptoRequest;
import com.ravenwallet.presenter.interfaces.BRAuthCompletion;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.animation.BRDialog;
import com.ravenwallet.tools.security.AuthManager;
import com.ravenwallet.tools.security.BRKeyStore;
import com.ravenwallet.tools.security.PostAuth;
import com.ravenwallet.tools.threads.executor.BRExecutor;
import com.ravenwallet.tools.util.BRConstants;
import com.ravenwallet.tools.util.CurrencyUtils;
import com.ravenwallet.tools.util.Utils;
import com.ravenwallet.wallet.WalletsMaster;
import com.ravenwallet.wallet.abstracts.BaseWalletManager;
import com.ravenwallet.wallet.exceptions.AmountSmallerThanMinException;
import com.ravenwallet.wallet.exceptions.FeeNeedsAdjust;
import com.ravenwallet.wallet.exceptions.FeeOutOfDate;
import com.ravenwallet.wallet.exceptions.InsufficientFundsException;
import com.ravenwallet.wallet.exceptions.SomethingWentWrong;
import com.ravenwallet.wallet.exceptions.SpendingNotAllowed;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * RavenWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 2/20/18.
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
public class SendManager {
    private static final String TAG = SendManager.class.getSimpleName();

    private static boolean timedOut;
    private static boolean sending;
    private final static long FEE_EXPIRATION_MILLIS = 72 * 60 * 60 * 1000L;

    public static boolean sendTransaction(final Context app, final CryptoRequest payment, final BaseWalletManager walletManager) {
        //array in order to be able to modify the first element from an inner block (can't be final)
        final String[] errTitle = {null};
        final String[] errMessage = {null};

        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (sending) {
                        Log.e(TAG, "sendTransaction: already sending..");
                        return;
                    }
                    sending = true;
                    long now = System.currentTimeMillis();
                    //if the fee was updated more than 24 hours ago then try updating the fee
                    if (now - BRSharedPrefs.getFeeTime(app, walletManager.getIso(app)) >= FEE_EXPIRATION_MILLIS) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if (sending) timedOut = true;
                            }
                        }).start();
                        walletManager.updateFee(app);
                        //if the fee is STILL out of date then fail with network problem message
                        long time = BRSharedPrefs.getFeeTime(app, walletManager.getIso(app));
                        if (time <= 0 || now - time >= FEE_EXPIRATION_MILLIS) {
                            Log.e(TAG, "sendTransaction: fee out of date even after fetching...");
                            throw new FeeOutOfDate(BRSharedPrefs.getFeeTime(app, walletManager.getIso(app)), now);
                        }
                    }
                    if (!timedOut)
                        tryPay(app, payment, walletManager);
                    else
                        FirebaseCrashlytics.getInstance().recordException(new NullPointerException("did not send, timedOut!"));
                    return; //return so no error is shown
                } catch (InsufficientFundsException ignored) {
                    errTitle[0] = app.getString(R.string.Alerts_sendFailure);
                    errMessage[0] = "Insufficient Funds";
                } catch (AmountSmallerThanMinException e) {
                    long minAmount = walletManager.getWallet().getMinOutputAmount();
                    errTitle[0] = app.getString(R.string.Alerts_sendFailure);
                    errMessage[0] = String.format(Locale.getDefault(), app.getString(R.string.PaymentProtocol_Errors_smallPayment),
                            "µ"+BRConstants.symbolRavenPrimary + new BigDecimal(minAmount).divide(new BigDecimal(100), BRConstants.ROUNDING_MODE));
                } catch (SpendingNotAllowed spendingNotAllowed) {
                    ((Activity) app).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BRDialog.showCustomDialog(app, app.getString(R.string.Alert_error), app.getString(R.string.Send_isRescanning), app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                                @Override
                                public void onClick(BRDialogView brDialogView) {
                                    brDialogView.dismissWithAnimation();
                                }
                            }, null, null, 0);
                        }
                    });
                    return;
                } catch (FeeNeedsAdjust feeNeedsAdjust) {
                    //offer to change amount, so it would be enough for fee
//                    showFailed(app); //just show failed for now
                    showAdjustFee((Activity) app, payment, walletManager);
                    return;
                } catch (FeeOutOfDate ex) {
                    //Fee is out of date, show not connected error
                    FirebaseCrashlytics.getInstance().recordException(ex);
                    BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            BRDialog.showCustomDialog(app, app.getString(R.string.Alerts_sendFailure), app.getString(R.string.NodeSelector_notConnected), app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                                @Override
                                public void onClick(BRDialogView brDialogView) {
                                    brDialogView.dismiss();
                                }
                            }, null, null, 0);
                        }
                    });
                    return;
                } catch (SomethingWentWrong somethingWentWrong) {
                    somethingWentWrong.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(somethingWentWrong);
                    BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            BRDialog.showCustomDialog(app, app.getString(R.string.Alerts_sendFailure), "Something went wrong", app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                                @Override
                                public void onClick(BRDialogView brDialogView) {
                                    brDialogView.dismiss();
                                }
                            }, null, null, 0);
                        }
                    });
                    return;
                } finally {
                    sending = false;
                    timedOut = false;
                }

                //show the message if we have one to show
                if (errTitle[0] != null && errMessage[0] != null)
                    BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            BRDialog.showCustomDialog(app, errTitle[0], errMessage[0], app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                                @Override
                                public void onClick(BRDialogView brDialogView) {
                                    brDialogView.dismiss();
                                }
                            }, null, null, 0);
                        }
                    });

            }
        });
        return true;
    }


    /**
     * Try transaction and throw appropriate exceptions if something was wrong
     * BLOCKS
     */
    private static void tryPay(final Context app, final CryptoRequest paymentRequest, final BaseWalletManager walletManager) throws InsufficientFundsException,
            AmountSmallerThanMinException, SpendingNotAllowed, FeeNeedsAdjust, SomethingWentWrong {
        if (paymentRequest == null) {
            Log.e(TAG, "tryPay: ERROR: paymentRequest: null");
            String message = "paymentRequest is null";
            BRReportsManager.reportBug(new RuntimeException("paymentRequest is malformed: " + message), true);
            throw new SomethingWentWrong("wrong parameters: paymentRequest");
        }
//        long amount = paymentRequest.amount;
        long balance = walletManager.getCachedBalance(app);
        long minOutputAmount = walletManager.getWallet().getMinOutputAmount();
        final long maxOutputAmount = walletManager.getWallet().getMaxOutputAmount();

        if (paymentRequest.tx == null) {
            //not enough for fee
            if (paymentRequest.notEnoughForFee(app, walletManager)) {
                //weird bug when the core WalletsMaster is NULL
                if (maxOutputAmount == -1) {
                    BRReportsManager.reportBug(new RuntimeException("getMaxOutputAmount is -1, meaning _wallet is NULL"), true);
                    throw new SomethingWentWrong("getMaxOutputAmount is -1, meaning _wallet is NULL");
                }
                // max you can spend is smaller than the min you can spend
                if (maxOutputAmount == 0 || maxOutputAmount < minOutputAmount) {
                    throw new InsufficientFundsException(paymentRequest.amount.longValue(), balance);
                }

                throw new FeeNeedsAdjust(paymentRequest.amount.longValue(), balance, -1);
            } else {
                throw new InsufficientFundsException(walletManager.getCachedBalance(app), -1);
            }
        }

        // check if spending is allowed
        if (!BRSharedPrefs.getAllowSpend(app, walletManager.getIso(app))) {
            throw new SpendingNotAllowed();
        }

        //check if amount isn't smaller than the min amount
        if (paymentRequest.isSmallerThanMin(app, walletManager)) {
            throw new AmountSmallerThanMinException(Math.abs(walletManager.getWallet().getTransactionAmount(paymentRequest.tx)), minOutputAmount);
        }

        //amount is larger than balance
        if (paymentRequest.isLargerThanBalance(app, walletManager)) {
            throw new InsufficientFundsException(Math.abs(walletManager.getWallet().getTransactionAmount(paymentRequest.tx)), balance);
        }

        // payment successful
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                PostAuth.getInstance().setPaymentItem(paymentRequest);
                confirmPay(app, paymentRequest, walletManager);
            }
        });

    }

    private static void showAdjustFee(final Activity app, final CryptoRequest item, final BaseWalletManager walletManager) {
        BaseWalletManager wm = WalletsMaster.getInstance(app).getCurrentWallet(app);
        long maxAmountDouble = walletManager.getWallet().getMaxOutputAmount();
        if (maxAmountDouble == -1) {
            BRReportsManager.reportBug(new RuntimeException("getMaxOutputAmount is -1, meaning _wallet is NULL"));
            return;
        }
        if (maxAmountDouble == 0) {
            BRDialog.showCustomDialog(app, app.getString(R.string.Alerts_sendFailure), "Insufficient amount for transaction fee", app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                @Override
                public void onClick(BRDialogView brDialogView) {
                    brDialogView.dismissWithAnimation();
                }
            }, null, null, 0);
        } else {
            if (Utils.isNullOrEmpty(item.address)) throw new RuntimeException("can't happen");
            final BRCoreTransaction tx = wm.getWallet().createTransaction(maxAmountDouble, new BRCoreAddress(item.address));
            if (tx == null) {
                BRDialog.showCustomDialog(app, app.getString(R.string.Alerts_sendFailure), "Insufficient amount for transaction fee", app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismissWithAnimation();
                    }
                }, null, null, 0);
                return;
            }
            long fee = wm.getWallet().getTransactionFee(tx);
            if (fee <= 0) {
                BRReportsManager.reportBug(new RuntimeException("fee is weird:  " + fee));
                BRDialog.showCustomDialog(app, app.getString(R.string.Alerts_sendFailure), "Insufficient amount for transaction fee.", app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismissWithAnimation();
                    }
                }, null, null, 0);
                return;
            }

            String formattedCrypto = CurrencyUtils.getFormattedAmount(app, wm.getIso(app), new BigDecimal(maxAmountDouble).negate());
            String formattedFiat = CurrencyUtils.getFormattedAmount(app, BRSharedPrefs.getPreferredFiatIso(app), wm.getFiatForSmallestCrypto(app, new BigDecimal(maxAmountDouble), null).negate());

            String posButtonText = String.format("%s (%s)", formattedCrypto, formattedFiat);


            BRDialog.showCustomDialog(app, "Insufficient amount for transaction fee", "Send max?", posButtonText, "No thanks", new BRDialogView.BROnClickListener() {
                @Override
                public void onClick(BRDialogView brDialogView) {
                    brDialogView.dismissWithAnimation();
                    item.tx = tx;
                    PostAuth.getInstance().setPaymentItem(item);
                    confirmPay(app, item, walletManager);

                }
            }, new BRDialogView.BROnClickListener() {
                @Override
                public void onClick(BRDialogView brDialogView) {
                    brDialogView.dismissWithAnimation();
                }
            }, null, 0);
        }

    }

    private static void confirmPay(final Context ctx, final CryptoRequest request, final BaseWalletManager walletManager) {
        if (ctx == null) {
            Log.e(TAG, "confirmPay: context is null");
            return;
        }

        String message = createConfirmation(ctx, request, walletManager);

        double minOutput;
        if (request.isAmountRequested) {
            minOutput = BRCoreTransaction.getMinOutputAmount();
        } else {
            minOutput = walletManager.getWallet().getMinOutputAmount();
        }

        //amount can't be less than the min
        if (Math.abs(walletManager.getWallet().getTransactionAmount(request.tx)) < minOutput) {
            final String bitcoinMinMessage = String.format(Locale.getDefault(), ctx.getString(R.string.PaymentProtocol_Errors_smallTransaction),
                    CurrencyUtils.getFormattedAmount(ctx, walletManager.getIso(ctx), new BigDecimal(minOutput)));

            ((Activity) ctx).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BRDialog.showCustomDialog(ctx, ctx.getString(R.string.Alerts_sendFailure), bitcoinMinMessage, ctx.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            brDialogView.dismiss();
                        }
                    }, null, null, 0);
                }
            });
            return;
        }
        boolean forcePin = false;

        if (Utils.isEmulatorOrDebug(ctx)) {
            Log.e(TAG, "confirmPay: totalSent: " + walletManager.getWallet().getTotalSent());
            Log.e(TAG, "confirmPay: request.amount: " + walletManager.getWallet().getTransactionAmount(request.tx));
            Log.e(TAG, "confirmPay: total limit: " + AuthManager.getInstance().getTotalLimit(ctx));
            Log.e(TAG, "confirmPay: limit: " + BRKeyStore.getSpendLimit(ctx));
        }

        if (walletManager.getWallet().getTotalSent() + Math.abs(walletManager.getWallet().getTransactionAmount(request.tx)) > AuthManager.getInstance().getTotalLimit(ctx)) {
            forcePin = true;
        }

        //successfully created the transaction, authenticate user
        AuthManager.getInstance().authPrompt(ctx, "", message, forcePin, false, new BRAuthCompletion() {
            @Override
            public void onComplete() {
                BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                    @Override
                    public void run() {
                        PostAuth.getInstance().onPublishTxAuth(ctx, false);
                        BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                            @Override
                            public void run() {
                                BRAnimator.killAllFragments((Activity) ctx);
                            }
                        });

                    }
                });

            }

            @Override
            public void onCancel() {
                //nothing
            }
        });

    }

    private static String createConfirmation(Context ctx, CryptoRequest request, final BaseWalletManager walletManager) {

        String receiver;
        boolean certified = false;
        if (request.cn != null && request.cn.length() != 0) {
            certified = true;
        }
        receiver = walletManager.decorateAddress(ctx, request.address);
        if (certified) {
            receiver = "certified: " + request.cn + "\n";
        }

        String iso = BRSharedPrefs.getPreferredFiatIso(ctx);
        BaseWalletManager wallet = WalletsMaster.getInstance(ctx).getCurrentWallet(ctx);
        long size = request.tx.getSize();
        long stdFee = request.tx.getStandardFee();
        long feeForTx = walletManager.getWallet().getTransactionFee(request.tx);
        if (feeForTx <= 0) {
            long maxAmount = walletManager.getWallet().getMaxOutputAmount();
            if (maxAmount == -1) {
                BRReportsManager.reportBug(new RuntimeException("getMaxOutputAmount is -1, meaning _wallet is NULL"), true);
            }
            if (maxAmount == 0) {
                BRDialog.showCustomDialog(ctx, "", ctx.getString(R.string.Alerts_sendFailure), ctx.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismiss();
                    }
                }, null, null, 0);

                return null;
            }
            request.tx = walletManager.getWallet().createTransaction(maxAmount, wallet.getWallet().getTransactionAddress(request.tx));
            feeForTx = walletManager.getWallet().getTransactionFee(request.tx);
            feeForTx += walletManager.getCachedBalance(ctx) - Math.abs(walletManager.getWallet().getTransactionAmount(request.tx));
        }
        long amount = Math.abs(walletManager.getWallet().getTransactionAmount(request.tx));
        final long total = amount + feeForTx;
        String formattedCryptoAmount = CurrencyUtils.getFormattedAmount(ctx, walletManager.getIso(ctx), new BigDecimal(amount));
        String formatterCryptoFee = CurrencyUtils.getFormattedAmount(ctx, walletManager.getIso(ctx), new BigDecimal(feeForTx));
        String formatterCryptoTotal = CurrencyUtils.getFormattedAmount(ctx, walletManager.getIso(ctx), new BigDecimal(total));

        String formattedAmount = CurrencyUtils.getFormattedAmount(ctx, iso, wallet.getFiatForSmallestCrypto(ctx, new BigDecimal(amount), null));
        String formattedFee = CurrencyUtils.getFormattedAmount(ctx, iso, wallet.getFiatForSmallestCrypto(ctx, new BigDecimal(feeForTx), null));
        String formattedTotal = CurrencyUtils.getFormattedAmount(ctx, iso, wallet.getFiatForSmallestCrypto(ctx, new BigDecimal(total), null));

        //formatted text
        return receiver + "\n\n"
                + ctx.getString(R.string.Confirmation_amountLabel) + " " + formattedCryptoAmount + " (" + formattedAmount + ")"
                + "\n" + ctx.getString(R.string.Confirmation_feeLabel) + " " + formatterCryptoFee + " (" + formattedFee + ")"
                + "\n" + ctx.getString(R.string.Confirmation_totalLabel) + " " + formatterCryptoTotal + " (" + formattedTotal + ")"
                + (request.message == null ? "" : "\n\n" + request.message);
    }

}
