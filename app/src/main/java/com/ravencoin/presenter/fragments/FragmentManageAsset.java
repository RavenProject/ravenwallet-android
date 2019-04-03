package com.ravencoin.presenter.fragments;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.platform.assets.Asset;
import com.ravencoin.R;
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
import java.util.Locale;

import static com.platform.assets.AssetType.REISSUE;
import static com.ravencoin.presenter.activities.AddressBookActivity.PICK_ADDRESS_VIEW_EXTRAS_KEY;
import static com.ravencoin.tools.animation.BRAnimator.animateBackgroundDim;
import static com.ravencoin.tools.animation.BRAnimator.animateSignalSlide;
import static com.ravencoin.tools.util.BRConstants.REISSUE_FEE;
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

public class FragmentManageAsset extends BaseAddressAndIpfsHashValidation implements WalletManagerListener {

    private static final String TAG = FragmentManageAsset.class.getName();

    public static final long MAX_ASSET_QUANTITY = 21000000 * 1000L;
    public static final int ASSET_MANAGING_FEE = 100;

    private ScrollView backgroundLayout;
    private LinearLayout signalLayout, quantityKeyboardLayout, unitsKeyboardLayout, balanceLayout;
    private EditText quantityEditText, unitsEditText;
    private StringBuilder quantityBuilder;
    private BRLinearLayoutWithCaret feeLayout;
    private BRKeyboard quantityKeyboard, unitsKeyboard;
    private BRButton regular;
    private BRButton economy;
    private TextView balanceText, feeText, managingFeeText;
    private BRText feeDescription;
    private BRText warningText;
    private RelativeLayout reissuableLayout;
    private CheckBox reissuableCheckBox, ipfsHashCheckBox;
    private PasteButton pasteIPFSHashButton;
    private ScanButton scanIPFSHashButton;
    private BRButton doneButton;
    private boolean hasManageFee = false;
    private RelativeLayout addressLayout;
    private String mAddress;
    private Asset mAsset;
    private BRCoreTransactionAsset reissueAsset;
    private final static String EXTRAS_ASSET_KEY = "extras.asset.key";

    public static FragmentManageAsset newInstance(Asset asset) {
        FragmentManageAsset fragment = new FragmentManageAsset();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRAS_ASSET_KEY, asset);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_manage_asset, container, false);

        backgroundLayout = rootView.findViewById(R.id.background_layout);
        signalLayout = rootView.findViewById(R.id.signal_layout);
        addressLayout = rootView.findViewById(R.id.layout_address);
        addressEditText = rootView.findViewById(R.id.address_edit_text);
        quantityEditText = rootView.findViewById(R.id.quantity_edit_text);
        unitsEditText = rootView.findViewById(R.id.units_edit_text);
        feeLayout = rootView.findViewById(R.id.fee_buttons_layout);
        quantityKeyboardLayout = rootView.findViewById(R.id.quantity_keyboard_layout);
        unitsKeyboardLayout = rootView.findViewById(R.id.units_keyboard_layout);
        quantityKeyboard = rootView.findViewById(R.id.quantity_keyboard);
        unitsKeyboard = rootView.findViewById(R.id.units_keyboard);
        balanceLayout = rootView.findViewById(R.id.balance_layout);
        feeDescription = rootView.findViewById(R.id.fee_description);
        warningText = rootView.findViewById(R.id.warning_text);
        regular = rootView.findViewById(R.id.left_button);
        economy = rootView.findViewById(R.id.right_button);
        balanceText = rootView.findViewById(R.id.balance_text);
        feeText = rootView.findViewById(R.id.fee_text);
        managingFeeText = rootView.findViewById(R.id.asset_managing_fee_text);
        reissuableLayout = rootView.findViewById(R.id.reissuable_layout);
        reissuableCheckBox = rootView.findViewById(R.id.reissuable_checkbox);
        ipfsHashEditText = rootView.findViewById(R.id.ipfs_hash_edit_text);
        ipfsHashCheckBox = rootView.findViewById(R.id.ipfs_hash_checkbox);
        pasteIPFSHashButton = rootView.findViewById(R.id.paste_ipfs_hash_button);
        scanIPFSHashButton = rootView.findViewById(R.id.scan_ipfs_hash_button);
        doneButton = rootView.findViewById(R.id.done_button);

        boolean isExpertMode = BRSharedPrefs.getExpertMode(getActivity());
        addressLayout.setVisibility(isExpertMode ? View.VISIBLE : View.GONE);
        setListeners(rootView);

        signalLayout.setOnTouchListener(new SlideDetector(getContext(), signalLayout));

        // Set title
        mAsset = getArguments().getParcelable(EXTRAS_ASSET_KEY);
        TextView title = rootView.findViewById(R.id.title);
        title.setText(getString(R.string.manage_asset_title, mAsset.getName()));
        unitsEditText.setText(String.valueOf(mAsset.getUnits()));
        // Set keyboards background color
        quantityKeyboard.setBRButtonBackgroundResId(R.drawable.keyboard_white_button);
        quantityKeyboard.setBRKeyboardColor(R.color.white);
        unitsKeyboard.setBRButtonBackgroundResId(R.drawable.keyboard_white_button);
        unitsKeyboard.setBRKeyboardColor(R.color.white);

        quantityBuilder = new StringBuilder(0);

        setBalanceAndTransactionFee();
        reissuableCheckBox.setChecked(true);
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

        // Handling the paste IPFS Hash button click
        pasteIPFSHashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postIPFSHashPasted(ipfsHashEditText);
            }
        });

        // Handling the scan IPFS Hash button click
        scanIPFSHashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                BRAnimator.openIPFSHashScanner(getActivity(), BRConstants.IPFS_HASH_SCANNER_REQUEST);

            }
        });

        ipfsHashEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ipfsHashCheckBox.setChecked(true);
                }
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

        quantityEditText.addTextChangedListener(new NumbersTypingTextWatcher(quantityEditText));
        unitsEditText.addTextChangedListener(new NumbersTypingTextWatcher(unitsEditText));

        bindKeyboardViewWithInputField(quantityKeyboardLayout, quantityEditText);
        bindKeyboardViewWithInputField(unitsKeyboardLayout, unitsEditText);

        quantityEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantityKeyboardLayout.getVisibility() == View.VISIBLE) {
                    quantityKeyboardLayout.setVisibility(View.GONE);
                } else quantityKeyboardLayout.setVisibility(View.VISIBLE);
            }
        });
        unitsEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (unitsKeyboardLayout.getVisibility() == View.VISIBLE) {
                    unitsKeyboardLayout.setVisibility(View.GONE);
                } else unitsKeyboardLayout.setVisibility(View.VISIBLE);
            }
        });

        quantityKeyboard.addOnInsertListener(new BRKeyboard.OnInsertListener() {
            @Override
            public void onClick(String key) {
                handleQuantityKeyboardClick(key);
            }
        });
        unitsKeyboard.addOnInsertListener(new BRKeyboard.OnInsertListener() {
            @Override
            public void onClick(String key) {
                handleUnitsKeyboardClick(key);
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

        reissuableLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reissuableCheckBox.setChecked(!reissuableCheckBox.isChecked());
            }
        });

        ipfsHashCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pasteIPFSHashButton.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                scanIPFSHashButton.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isExpertMode = BRSharedPrefs.getExpertMode(getContext());
                if (isExpertMode) {
                    mAddress = addressEditText.getText().toString();
                    boolean isAddressValid = isAddressValid(mAddress);
                    if (!isAddressValid) {
                        sayInvalidAddress();
                        return;
                    }
                } else {
                    mAddress = getAddress();
                }
                if (mAsset.getOwnership() == 0) {
                    sayCustomMessage("You don't own this asset");
                    return;
                }

                if (!hasManageFee) {
                    sayCustomMessage("insufficient Fees");
                    return;
                }

                if (TextUtils.isEmpty(quantityBuilder.toString())) {
                    sayCustomMessage("Invalid Quantity");
                    return;
                }

                double quantity = Double.parseDouble(quantityBuilder.toString());
                if (quantity <= 0) {
                    sayCustomMessage("Invalid Quantity");
                    return;
                }

                if (TextUtils.isEmpty(unitsEditText.getText().toString())) {
                    sayCustomMessage("invalid unit");
                    return;
                }
                int unit = Integer.parseInt(unitsEditText.getText().toString());
                if (unit > 8) {
                    sayCustomMessage("invalid unit");
                    return;
                }

                if (ipfsHashCheckBox.isChecked() && !isIPFSHashValid(ipfsHashEditText.getText().toString())) {
                    sayInvalidIPFSHash();
                    return;
                }

                reissueAsset = new BRCoreTransactionAsset();
                reissueAsset.setType(REISSUE.getIndex());
                String name = mAsset.getName();
                reissueAsset.setName(name);
                reissueAsset.setNamelen(name.length());
                reissueAsset.setAmount((long) (quantity * SATOSHIS));
                reissueAsset.setReissuable(reissuableCheckBox.isChecked() ? 1 : 0);
                reissueAsset.setHasIPFS(ipfsHashCheckBox.isChecked() ? 1 : 0);
                reissueAsset.setIPFSHash(ipfsHashEditText.getText().toString());
                reissueAsset.setUnit(unit);
                getAssetData();
            }
        });
        // Handling the paste address button click
        PasteButton pasteAddressButton = rootView.findViewById(R.id.paste_address_button);
        pasteAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postAddressPasted(addressEditText);
            }
        });

        // Handling 'generate address button' click
        BRButton generateAddressButton = rootView.findViewById(R.id.generate_address_button);
        generateAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = getAddress();
                addressEditText.setText(address);
            }
        });

        // Handling the scan address button click
        ScanButton scanAddressButton = rootView.findViewById(R.id.scan_address_button);
        scanAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                BRAnimator.openAddressScanner(getActivity(), BRConstants.ADDRESS_SCANNER_REQUEST);

            }
        });
        ContactButton importContactButton = rootView.findViewById(R.id.import_contact);
        importContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddressBookActivity.class);
                intent.putExtra(PICK_ADDRESS_VIEW_EXTRAS_KEY, true);
                getActivity().startActivityForResult(intent, BRConstants.SELECT_FROM_ADDRESS_BOOK_REQUEST);
                getActivity().overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
            }
        });
    }

    private void getAssetData() {
        doneButton.setText("Processing...");
        doneButton.setEnabled(false);
        RvnWalletManager walletManager = RvnWalletManager.getInstance(getActivity());
        BRCoreWallet wallet = walletManager.getWallet();
        wallet.getAssetData(walletManager.getPeerManager(), mAsset.getName(),
                mAsset.getName().length(), this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doneButton.setEnabled(true);
                doneButton.setText("Done");

            }
        }, 3000);
    }

    // callback of wallet.getAssetData from JNI
    public void onGetAssetData(BRCoreTransactionAsset asset) {
        Activity activity = getActivity();
        if (asset != null && activity != null) {
            long networkAmount = asset.getAmount();
            long reissueAmount = reissueAsset.getAmount();
            boolean quantityValid = networkAmount + reissueAmount <= (MAX_ASSET_QUANTITY * SATOSHIS);

            if (quantityValid)
                reissueAsset(reissueAsset);
            else {
                DecimalFormat currencyFormat = (DecimalFormat) DecimalFormat.getInstance();
                currencyFormat.setGroupingUsed(true);
                currencyFormat.setRoundingMode(RoundingMode.DOWN);
                BigDecimal amount = new BigDecimal((MAX_ASSET_QUANTITY - (networkAmount / SATOSHIS)));
                Log.e(TAG, "setTexts: amount:" + amount);
                String formattedAmount = currencyFormat.format(amount.doubleValue());
                sayCustomMessage("Available Amount " + formattedAmount);
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!doneButton.isEnabled()) {
                        doneButton.setEnabled(true);
                        doneButton.setText("Done");
                    }
                }
            });
        }

    }

    private void reissueAsset(final BRCoreTransactionAsset asset) {
        final Activity app = getActivity();
        if (app == null) return;
        final RvnWalletManager walletManager = RvnWalletManager.getInstance(app);
        walletManager.requestConfirmation(app, REISSUE, asset, null, mAddress, false,FragmentManageAsset.this);
    }

    private void closeMe() {
        Activity app = getActivity();
        if (app != null && !app.isDestroyed())
            app.getFragmentManager().popBackStack();
    }

    private String getAddress() {
        Context ctx = getContext();
        WalletsMaster.getInstance(ctx).getCurrentWallet(ctx).refreshAddress(ctx);
        final BaseWalletManager wallet = WalletsMaster.getInstance(ctx).getCurrentWallet(ctx);
        return BRSharedPrefs.getReceiveAddress(ctx, wallet.getIso(ctx));
    }

    private void handleUnitsKeyboardClick(String key) {
        if (key == null) {
            Log.e(TAG, "handleClick: key is null! ");
            return;
        }
        if (key.isEmpty()) {
            if (unitsEditText.getText().length() > 0) {
                unitsEditText.setText("");
            }
        } else if (Character.isDigit(key.charAt(0)) && Character.getNumericValue(key.charAt(0)) < 9) {
            if (Character.getNumericValue(key.charAt(0)) >= mAsset.getUnits())
                unitsEditText.setText(String.valueOf(key.charAt(0)));
        }
    }

    private void handleQuantityKeyboardClick(String key) {
        if (key == null) {
            Log.e(TAG, "handleClick: key is null! ");
            return;
        }
        if (key.isEmpty()) {
            handleDeleteClick();
        } else if (Character.isDigit(key.charAt(0))) {
            handleDigitClick(Integer.parseInt(key.substring(0, 1)));
        }
    }

    private void handleDigitClick(Integer digit) {
        String currentAmount = quantityBuilder.toString();
        if (new BigDecimal(currentAmount.concat(String.valueOf(digit))).doubleValue() <= MAX_ASSET_QUANTITY) {
            quantityBuilder.append(digit);
            setQuantity();
        }
    }

    private void handleDeleteClick() {
        String currentQuantity = quantityBuilder.toString();
        if (currentQuantity.length() > 0) {
            quantityBuilder.deleteCharAt(currentQuantity.length() - 1);
            setQuantity();
        }
    }

    public void setAddress(String address) {
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

        //format the asset managing fee to the selected ISO
        BigDecimal isoAssetManagingFee = new BigDecimal(ASSET_MANAGING_FEE);
        String formattedAssetManagingFee = CurrencyUtils.getFormattedAmount(app, wallet.getIso(app), wallet.getSmallestCryptoForCrypto(app, isoAssetManagingFee));

        hasManageFee = isoBalance.compareTo(isoAssetManagingFee.add(isoFee)) > 0;
        setBalanceTextStyle();

        // formattedBalance
        String formattedBalance = CurrencyUtils.getFormattedAmount(app, defaultIso, isIsoCrypto ? wallet.getSmallestCryptoForCrypto(app, isoBalance) : isoBalance);
        String balanceString = String.format(getString(R.string.Send_balance), formattedBalance);
        balanceText.setText(balanceString);
        feeText.setText(String.format(getString(R.string.Send_fee), formattedFee));
        managingFeeText.setText(String.format(getString(R.string.asset_manage_fee), formattedAssetManagingFee));
        balanceLayout.requestLayout();
    }

    private void setBalanceTextStyle() {
        if (hasManageFee) {
            balanceText.setTextColor(getContext().getColor(R.color.light_gray));
            feeText.setTextColor(getContext().getColor(R.color.light_gray));
            managingFeeText.setTextColor(getContext().getColor(R.color.light_gray));
        } else {
            balanceText.setTextColor(getContext().getColor(R.color.warning_color));
            feeText.setTextColor(getContext().getColor(R.color.warning_color));
            managingFeeText.setTextColor(getContext().getColor(R.color.warning_color));
        }
    }

    private void setQuantity() {
        String tmpAmount = quantityBuilder.toString();
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
        quantityEditText.setText(newAmount);
    }

    private void bindKeyboardViewWithInputField(final ViewGroup keyboardLayout, EditText inputField) {
        inputField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
        inputField.setShowSoftInputOnFocus(false);
    }

    @Override
    public void close() {
        closeMe();
    }

    @Override
    public void error(String error) {
        sayCustomMessage(error);
    }

    private static class NumbersTypingTextWatcher implements TextWatcher {

        private EditText inputField;

        public NumbersTypingTextWatcher(EditText inputField) {
            this.inputField = inputField;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().isEmpty()) {
                inputField.setTextSize(16);
            } else {
                inputField.setTextSize(24);
            }
        }
    }
}