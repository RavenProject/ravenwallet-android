package com.ravencoin.presenter.fragments;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.platform.assets.Asset;
import com.ravencoin.R;
import com.ravencoin.core.BRCoreAddress;
import com.ravencoin.core.BRCoreTransaction;
import com.ravencoin.core.BRCoreTransactionAsset;
import com.ravencoin.core.BRCoreWallet;
import com.ravencoin.presenter.activities.AddressBookActivity;
import com.ravencoin.presenter.customviews.BRButton;
import com.ravencoin.presenter.customviews.BRKeyboard;
import com.ravencoin.presenter.customviews.BRLinearLayoutWithCaret;
import com.ravencoin.presenter.customviews.BRText;
import com.ravencoin.presenter.customviews.ContactButton;
import com.ravencoin.presenter.customviews.PasteButton;
import com.ravencoin.presenter.customviews.ScanButton;
import com.ravencoin.presenter.interfaces.BRAuthCompletion;
import com.ravencoin.presenter.interfaces.ConfirmationListener;
import com.ravencoin.presenter.interfaces.WalletManagerListener;
import com.ravencoin.tools.animation.BRAnimator;
import com.ravencoin.tools.animation.SlideDetector;
import com.ravencoin.tools.manager.BRSharedPrefs;
import com.ravencoin.tools.security.BRKeyStore;
import com.ravencoin.tools.util.BRConstants;
import com.ravencoin.tools.util.CurrencyUtils;
import com.ravencoin.tools.util.Utils;
import com.ravencoin.wallet.WalletsMaster;
import com.ravencoin.wallet.abstracts.BaseWalletManager;
import com.ravencoin.wallet.wallets.raven.RvnWalletManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import static com.platform.assets.AssetType.TRANSFER;
import static com.ravencoin.presenter.activities.AddressBookActivity.PICK_ADDRESS_VIEW_EXTRAS_KEY;
import static com.ravencoin.tools.animation.BRAnimator.animateBackgroundDim;
import static com.ravencoin.tools.animation.BRAnimator.animateSignalSlide;
import static com.ravencoin.tools.util.BRConstants.MAX_ASSET_QUANTITY;
import static com.ravencoin.tools.util.BRConstants.ONE_RAVEN;
import static com.ravencoin.tools.util.BRConstants.SATOSHIS;


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

public class FragmentTransferAsset extends BaseAddressValidation implements WalletManagerListener {

    private static final String TAG = FragmentTransferAsset.class.getName();

    private ScrollView backgroundLayout;
    private LinearLayout signalLayout, keyboardLayout, balanceLayout;
    private EditText amountEditText;
    private StringBuilder amountBuilder;
    private BRLinearLayoutWithCaret feeLayout;
    private BRKeyboard keyboard;
    private BRButton regular;
    private BRButton economy;
    private TextView assetBalance, balanceText, feeText;
    private BRText feeDescription;
    private BRText warningText;
    private RelativeLayout transferOwnershipLayout;
    private CheckBox transferOwnershipCheckbox;
    private ContactButton importContactButton;
    private boolean hasTransferFee = false;

    private Asset asset;

    private final static String EXTRAS_ASSET_KEY = "extras.asset.key";

    public static FragmentTransferAsset newInstance(Asset asset) {
        FragmentTransferAsset fragment = new FragmentTransferAsset();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRAS_ASSET_KEY, asset);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_transfer_asset, container, false);

        backgroundLayout = rootView.findViewById(R.id.background_layout);
        signalLayout = rootView.findViewById(R.id.signal_layout);
        addressEditText = rootView.findViewById(R.id.address_edit);
        amountEditText = rootView.findViewById(R.id.asset_amount_edit_text);
        assetBalance = rootView.findViewById(R.id.asset_balance_text);
        feeLayout = rootView.findViewById(R.id.fee_buttons_layout);
        keyboardLayout = rootView.findViewById(R.id.keyboard_layout);
        keyboard = rootView.findViewById(R.id.keyboard);
        balanceLayout = rootView.findViewById(R.id.balance_layout);
        feeDescription = rootView.findViewById(R.id.fee_description);
        warningText = rootView.findViewById(R.id.warning_text);
        regular = rootView.findViewById(R.id.left_button);
        economy = rootView.findViewById(R.id.right_button);
        balanceText = rootView.findViewById(R.id.balance_text);
        feeText = rootView.findViewById(R.id.fee_text);
        transferOwnershipLayout = rootView.findViewById(R.id.transfer_ownership_layout);
        transferOwnershipCheckbox = rootView.findViewById(R.id.transfer_ownership_checkbox);
        importContactButton = rootView.findViewById(R.id.import_contact);

        setListeners(rootView);

        signalLayout.setOnTouchListener(new SlideDetector(getContext(), signalLayout));

        // Set title
        asset = getArguments().getParcelable(EXTRAS_ASSET_KEY);
        TextView title = rootView.findViewById(R.id.title);
        title.setText(asset.getName());

        // Set the texts in the view
        setTexts(rootView);

        // Show/hide ownership transfer layout
        transferOwnershipLayout.setVisibility(asset.getOwnership() == 1 ? View.VISIBLE : View.GONE);

        // Set keyboard background color
        keyboard.setBRButtonBackgroundResId(R.drawable.keyboard_white_button);
        keyboard.setBRKeyboardColor(R.color.white);

        amountBuilder = new StringBuilder(0);

        setBalanceAndTransactionFee();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ViewTreeObserver observer = signalLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (observer.isAlive())
                    observer.removeOnGlobalLayoutListener(this);
                BRAnimator.animateBackgroundDim(backgroundLayout, false);
                BRAnimator.animateSignalSlide(signalLayout, false, new BRAnimator.OnSlideAnimationEnd() {
                    @Override
                    public void onAnimationEnd() {
                    }
                });
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.hideKeyboard(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isRemoving()) {
            animateBackgroundDim(backgroundLayout, true);
            animateSignalSlide(signalLayout, true, new BRAnimator.OnSlideAnimationEnd() {
                @Override
                public void onAnimationEnd() {
                    Activity app = getActivity();
                    if (app != null && !app.isDestroyed())
                        app.getFragmentManager().popBackStack();
                }
            });
        }
    }

    private void setListeners(final View rootView) {
        // Handling the paste button click
        PasteButton paste = rootView.findViewById(R.id.paste_button);
        paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postAddressPasted(addressEditText);
            }
        });

        // Handling the scan button click
        ScanButton scan = rootView.findViewById(R.id.scan_button);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                BRAnimator.openAddressScanner(getActivity(), BRConstants.ADDRESS_SCANNER_REQUEST);

            }
        });

        // Handling the close button click
        ImageButton close = rootView.findViewById(R.id.close_button);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity app = getActivity();
                if (app != null)
                    app.getFragmentManager().popBackStack();
            }
        });

        ImageView edit = rootView.findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (feeLayout.getVisibility() == View.VISIBLE) {
                    feeLayout.setVisibility(View.GONE);
                } else {
                    feeLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    amountEditText.setTextSize(16);
                } else {
                    amountEditText.setTextSize(24);
                }
            }
        });

        amountEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Utils.hideKeyboard(getActivity());
                    keyboardLayout.setVisibility(View.VISIBLE);
                } else {
                    keyboardLayout.setVisibility(View.GONE);
                }
            }
        });
        amountEditText.setShowSoftInputOnFocus(false);

        keyboard.addOnInsertListener(new BRKeyboard.OnInsertListener() {
            @Override
            public void onClick(String key) {
                handleClick(key);
            }
        });

        regular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButton(true);
            }
        });
        economy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButton(false);
            }
        });

        transferOwnershipLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferOwnershipCheckbox.setChecked(!transferOwnershipCheckbox.isChecked());
                amountEditText.setVisibility(transferOwnershipCheckbox.isChecked() ? View.GONE : View.VISIBLE);
            }
        });

        importContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddressBookActivity.class);
                intent.putExtra(PICK_ADDRESS_VIEW_EXTRAS_KEY, true);
                getActivity().startActivityForResult(intent, BRConstants.SELECT_FROM_ADDRESS_BOOK_REQUEST);
                getActivity().overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
            }
        });

        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        BRButton transferAssetButton = rootView.findViewById(R.id.transfer_asset_button);
        transferAssetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasTransferFee) {
                    sayCustomMessage("insufficient Fees");
                    return;
                }
                String address = addressEditText.getText().toString();
                boolean isAddressValid = isAddressValid(address);
                if (!isAddressValid) {
                    sayInvalidAddress();
                    return;
                }
                double quantity;
                if (transferOwnershipCheckbox.isChecked()) {
                    quantity = 1;
                } else if (TextUtils.isEmpty(amountBuilder.toString())) {
                    sayCustomMessage("Invalid Quantity");
                    return;
                } else {
                    quantity = Double.parseDouble(amountBuilder.toString());
                    if (quantity <= 0 || quantity * SATOSHIS > asset.getAmount()) {
                        sayCustomMessage("Invalid Quantity");
                        return;
                    }
                }
                boolean transferOwnerShip = transferOwnershipCheckbox.isChecked();
                BRCoreTransactionAsset BRAsset = new BRCoreTransactionAsset();
                BRAsset.setType(TRANSFER.getIndex());
                String name = asset.getName() + (transferOwnerShip ? "!" : "");
                BRAsset.setName(name);
                BRAsset.setNamelen(name.length());
                BRAsset.setAmount((long) (quantity * SATOSHIS));
                BRAsset.setReissuable(asset.getReissuable());
                BRAsset.setHasIPFS(asset.getHasIpfs());
                BRAsset.setIPFSHash(asset.getIpfsHash());
                BRAsset.setUnit(asset.getUnits());
                transferAsset(BRAsset, address, transferOwnerShip);
            }
        });
    }

    private void transferAsset(final BRCoreTransactionAsset asset, final String address, final boolean transferOwnerShip) {
        final Activity app = getActivity();
        if (app == null) return;
        final RvnWalletManager walletManager = RvnWalletManager.getInstance(app);
        walletManager.requestConfirmation(app, TRANSFER, asset, null, address,
                transferOwnerShip, FragmentTransferAsset.this);
    }

    private void closeMe() {
        Activity app = getActivity();
        if (app != null && !app.isDestroyed())
            app.getFragmentManager().popBackStack();
    }

    private void setTexts(View rootView) {
        // Set the Asset's Balance
        RvnWalletManager walletManager = RvnWalletManager.getInstance(getActivity());
        double assetAmount = walletManager.getCryptoForSmallestCrypto(getContext(), new BigDecimal(asset.getAmount())).doubleValue();
        String formattedAmount = com.platform.assets.Utils.formatAssetAmount(assetAmount, asset.getUnits());
        assetBalance.setText(String.format(getString(R.string.asset_balance), formattedAmount));
    }

    private void handleClick(String key) {
        if (key == null) {
            Log.e(TAG, "handleClick: key is null! ");
            return;
        }

        if (key.isEmpty()) {
            handleDeleteClick();
        } else if (Character.isDigit(key.charAt(0))) {
            handleDigitClick(Integer.parseInt(key.substring(0, 1)));
        } else if (key.charAt(0) == '.') {
            handleSeparatorClick();
        }
    }

    private void handleDigitClick(Integer digit) {
        String currentAmount = amountBuilder.toString();
        if (currentAmount.contains(".")) {
            String decimals = currentAmount.substring(currentAmount.indexOf("."));
            if (decimals.length() - 1 < asset.getUnits()) {
                // minus 1 is for the "." character that already exist within the string to be checked
                checkNewAmountValidity(currentAmount, digit);
            }
        } else {
            checkNewAmountValidity(currentAmount, digit);
        }
    }

    private void handleSeparatorClick() {
        if (asset.getUnits() == 0 || amountBuilder.toString().contains(".") || Double.parseDouble(amountBuilder.toString()) == MAX_ASSET_QUANTITY) {
            return;
        }
        amountBuilder.append(".");
        setAmount();
    }

    private void handleDeleteClick() {
        String currAmount = amountBuilder.toString();
        if (currAmount.length() > 0) {
            amountBuilder.deleteCharAt(currAmount.length() - 1);
            if (amountBuilder.length() > 0) {
                checkAssetAmountWithBalance(Double.parseDouble(amountBuilder.toString()));
            }
            setAmount();
        }
    }

    private void checkNewAmountValidity(String currentAmount, Integer digit) {
        double newAmount = new BigDecimal(currentAmount.concat(String.valueOf(digit))).doubleValue();
        if (newAmount <= MAX_ASSET_QUANTITY) {
            amountBuilder.append(digit);
            checkAssetAmountWithBalance(newAmount);
            setAmount();
        }
    }

    private void checkAssetAmountWithBalance(double newAmount) {
        if (newAmount*SATOSHIS > asset.getAmount()) {
            amountEditText.setTextColor(getContext().getColor(R.color.warning_color));
            assetBalance.setTextColor(getContext().getColor(R.color.warning_color));
        } else {
            amountEditText.setTextColor(getContext().getColor(R.color.almost_black));
            assetBalance.setTextColor(getContext().getColor(R.color.light_gray));
        }
    }

    public void setAddress(String address) {
        addressEditText.setText(address);
    }

    private void setButton(boolean isRegular) {
        BaseWalletManager wallet = WalletsMaster.getInstance(getActivity()).getCurrentWallet(getActivity());
        String iso = wallet.getIso(getActivity());
        if (isRegular) {
            BRSharedPrefs.putFavorStandardFee(getActivity(), iso, true);
            regular.setTextColor(getContext().getColor(R.color.white));
            regular.setBackground(getContext().getDrawable(R.drawable.b_half_left_blue));
            economy.setTextColor(getContext().getColor(R.color.dark_blue));
            economy.setBackground(getContext().getDrawable(R.drawable.b_half_right_blue_stroke));
            feeDescription.setText(String.format(getString(R.string.FeeSelector_estimatedDeliver), getString(R.string.FeeSelector_regularTime)));
            warningText.getLayoutParams().height = 0;
        } else {
            BRSharedPrefs.putFavorStandardFee(getActivity(), iso, false);
            regular.setTextColor(getContext().getColor(R.color.dark_blue));
            regular.setBackground(getContext().getDrawable(R.drawable.b_half_left_blue_stroke));
            economy.setTextColor(getContext().getColor(R.color.white));
            economy.setBackground(getContext().getDrawable(R.drawable.b_half_right_blue));
            feeDescription.setText(String.format(getString(R.string.FeeSelector_estimatedDeliver), getString(R.string.FeeSelector_economyTime)));
            warningText.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
        }
        warningText.requestLayout();
        setBalanceAndTransactionFee();
    }

    private void setBalanceAndTransactionFee() {
        Activity app = getActivity();
        if (app == null) return;

        BaseWalletManager wallet = WalletsMaster.getInstance(app).getCurrentWallet(app);
        String defaultIso = wallet.getIso(app);
        long curBalance = wallet.getCachedBalance(app);

        //is the chosen ISO a crypto (could be also a fiat currency)
        boolean isIsoCrypto = WalletsMaster.getInstance(getActivity()).isIsoCrypto(getActivity(), wallet.getIso(app));

        //wallet's balance for the selected ISO
        BigDecimal isoBalance = isIsoCrypto ? wallet.getCryptoForSmallestCrypto(app, new BigDecimal(curBalance)) : wallet.getFiatForSmallestCrypto(app, new BigDecimal(curBalance), null);
        if (isoBalance == null) isoBalance = new BigDecimal(0);

        //get the fee for iso (dollars, bits, BTC..)
        BigDecimal isoFee = isIsoCrypto ? wallet.getCryptoForSmallestCrypto(app, new BigDecimal(2300)) : wallet.getFiatForSmallestCrypto(app, new BigDecimal(2300), null);

        //format the fee to the selected ISO
        String formattedFee = CurrencyUtils.getFormattedAmount(app, wallet.getIso(app), isIsoCrypto ? wallet.getSmallestCryptoForCrypto(app, isoFee) : isoFee);
        hasTransferFee = isoBalance.compareTo(isoFee) > 0;
        // formattedBalance
        String formattedBalance = CurrencyUtils.getFormattedAmount(app, defaultIso, isIsoCrypto ? wallet.getSmallestCryptoForCrypto(app, isoBalance) : isoBalance);
        String balanceString = String.format(getString(R.string.Send_balance), formattedBalance);
        balanceText.setText(balanceString);
        feeText.setText(String.format(getString(R.string.Send_fee), formattedFee));
        balanceLayout.requestLayout();
    }

    private void setAmount() {
        String tmpAmount = amountBuilder.toString();
        int divider = tmpAmount.length();
        if (tmpAmount.contains(".")) {
            divider = tmpAmount.indexOf(".");
        }
        StringBuilder newAmount = new StringBuilder();
        for (int i = 0; i < tmpAmount.length(); i++) {
            newAmount.append(tmpAmount.charAt(i));
            if (divider > 3 && divider - 1 != i && divider > i && ((divider - i - 1) % 3 == 0)) {
                newAmount.append(",");
            }
        }
        amountEditText.setText(newAmount.toString());
    }

    @Override
    public void close() {
        closeMe();
    }

    @Override
    public void error(String error) {
        sayCustomMessage(error);
    }
}