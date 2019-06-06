package com.ravenwallet.presenter.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.util.Log;
import android.widget.EditText;

import com.ravenwallet.BuildConfig;
import com.ravenwallet.R;
import com.ravenwallet.core.BRCoreAddress;
import com.ravenwallet.presenter.customviews.BRDialogView;
import com.ravenwallet.presenter.entities.CryptoRequest;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.animation.BRDialog;
import com.ravenwallet.tools.manager.BRClipboardManager;
import com.ravenwallet.tools.threads.executor.BRExecutor;
import com.ravenwallet.tools.util.Utils;
import com.ravenwallet.wallet.WalletsMaster;
import com.ravenwallet.wallet.abstracts.BaseWalletManager;
import com.ravenwallet.wallet.util.CryptoUriParser;

import static com.ravenwallet.wallet.util.CryptoUriParser.parseRequest;


/**
 * RavenWallet
 * <p>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 6/29/15.
 * Copyright (c) 2016 breadwallet LLC
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class BaseAddressValidation extends Fragment {
    private static final String TAG = BaseAddressValidation.class.getName();

    protected EditText addressEditText;
    protected EditText addressNameEditText;
    CryptoRequest req;
    protected boolean isTransfer;

    public boolean isAddressValid(String address) {
        req = CryptoUriParser.parseRequest(getActivity(), address);
        return req != null && !Utils.isNullOrEmpty(req.address);
    }

    protected void postAddressPasted(final EditText addressInputField) {
        if (!BRAnimator.isClickAllowed()) return;
        String theUrl = BRClipboardManager.getClipboard(getActivity());
        if (Utils.isNullOrEmpty(theUrl)) {
            sayClipboardEmpty();
            return;
        }

        final BaseWalletManager wm = WalletsMaster.getInstance(getActivity()).getCurrentWallet(getActivity());


        if (Utils.isEmulatorOrDebug(getActivity()) && BuildConfig.TESTNET) {
            theUrl = wm.decorateAddress(getActivity(), theUrl);
        }

        CryptoRequest obj = parseRequest(getActivity(), theUrl);

        if (obj == null || Utils.isNullOrEmpty(obj.address)) {
            sayInvalidClipboardData();
            return;
        }

        if (obj.iso != null && !obj.iso.equalsIgnoreCase(wm.getIso(getActivity()))) {
            sayInvalidAddress(); //invalid if the screen is Bitcoin and scanning BitcoinCash for instance
            return;
        }

        final BRCoreAddress address = new BRCoreAddress(obj.address);


        if (address.isValid()) {
            final Activity app = getActivity();
            if (app == null) {
                Log.e(TAG, "paste onClick: app is null");
                return;
            }
            BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    if (isTransfer && wm.getWallet().containsAddress(address)) {
                        app.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                BRDialog.showCustomDialog(getActivity(), "",
                                        getResources().getString(R.string.Send_containsAddress),
                                        getResources().getString(R.string.AccessibilityLabels_close),
                                        null, new BRDialogView.BROnClickListener() {
                                            @Override
                                            public void onClick(BRDialogView brDialogView) {
                                                brDialogView.dismiss();
                                            }
                                        }, null, null, 0);
                                BRClipboardManager.putClipboard(getActivity(), "");
                            }
                        });

                    } else if (wm.getWallet().addressIsUsed(address)) {
                        app.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String walletIso = wm.getIso(getActivity());
                                String firstLine = "";

                                if (walletIso.equalsIgnoreCase("RVN")) {
                                    firstLine = getString(R.string.Sendrvn_UsedAddress_firstLine);
                                } /*else if (walletIso.equalsIgnoreCase("BCH")) {
                                            firstLine = getString(R.string.Sendbch_UsedAddress_firstLine);
                                        }*/
                                BRDialog.showCustomDialog(getActivity(), firstLine, getString(R.string.Send_UsedAddress_secondLIne), "Ignore", "Cancel", new BRDialogView.BROnClickListener() {
                                    @Override
                                    public void onClick(BRDialogView brDialogView) {
                                        brDialogView.dismiss();
                                        addressInputField.setText(wm.decorateAddress(getActivity(), address.stringify()));
                                    }
                                }, new BRDialogView.BROnClickListener() {
                                    @Override
                                    public void onClick(BRDialogView brDialogView) {
                                        brDialogView.dismiss();
                                    }
                                }, null, 0);
                            }
                        });

                    } else {
                        app.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "run: " + wm.getIso(getActivity()));
                                addressInputField.setText(wm.decorateAddress(getActivity(), address.stringify()));

                            }
                        });
                    }
                }
            });

        } else {
            sayInvalidClipboardData();
        }
    }

    protected void sayClipboardEmpty() {
        BRDialog.showCustomDialog(getActivity(), "", getResources().getString(R.string.Send_emptyPasteboard), getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
            @Override
            public void onClick(BRDialogView brDialogView) {
                brDialogView.dismiss();
            }
        }, null, null, 0);
        BRClipboardManager.putClipboard(getActivity(), "");
    }

    public void sayInvalidClipboardData() {
        BRDialog.showCustomDialog(getActivity(), "", getResources().getString(R.string.Send_invalidAddressTitle), getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
            @Override
            public void onClick(BRDialogView brDialogView) {
                brDialogView.dismiss();
            }
        }, null, null, 0);
        BRClipboardManager.putClipboard(getActivity(), "");
    }

    protected void saySomethingWentWrong() {
        BRDialog.showCustomDialog(getActivity(), "", "Something went wrong.", getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
            @Override
            public void onClick(BRDialogView brDialogView) {
                brDialogView.dismiss();
            }
        }, null, null, 0);
        BRClipboardManager.putClipboard(getActivity(), "");
    }

    protected void sayInvalidAddress() {
        BRDialog.showCustomDialog(getActivity(), "",
                getResources().getString(R.string.Send_invalidAddressMessage),
                getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismiss();
                    }
                }, null, null, 0);
        BRClipboardManager.putClipboard(getActivity(), "");
    }

    protected void sayCustomMessage(String message) {
        BRDialog.showCustomDialog(getActivity(), "", message, getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
            @Override
            public void onClick(BRDialogView brDialogView) {
                brDialogView.dismiss();
            }
        }, null, null, 0);
        BRClipboardManager.putClipboard(getActivity(), "");
    }

    public void setAddress(String address) {
        addressEditText.setText(address);
    }

}