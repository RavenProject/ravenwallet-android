package com.ravenwallet.presenter.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.platform.assets.AssetsRepository;
import com.platform.assets.filters.AssetNameFilter;
import com.platform.assets.filters.DecimalDigitsInputFilter;
import com.platform.assets.filters.InputFilterMinMax;
import com.platform.assets.filters.QuantityTextWatcher;
import com.platform.assets.filters.UnitTextWatcher;
import com.ravenwallet.R;
import com.ravenwallet.core.BRCoreTransactionAsset;
import com.ravenwallet.core.BRCoreWallet;
import com.ravenwallet.presenter.activities.AddressBookActivity;
import com.ravenwallet.presenter.customviews.BRButton;
import com.ravenwallet.presenter.customviews.BREdit;
import com.ravenwallet.presenter.customviews.BRLinearLayoutWithCaret;
import com.ravenwallet.presenter.customviews.BRText;
import com.ravenwallet.presenter.customviews.ContactButton;
import com.ravenwallet.presenter.customviews.InputTextWatcher;
import com.ravenwallet.presenter.customviews.PasteButton;
import com.ravenwallet.presenter.customviews.ScanButton;
import com.ravenwallet.presenter.interfaces.WalletManagerListener;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.animation.SlideDetector;
import com.ravenwallet.tools.manager.BRSharedPrefs;
import com.ravenwallet.tools.util.BRConstants;
import com.ravenwallet.tools.util.CurrencyUtils;
import com.ravenwallet.tools.util.Utils;
import com.ravenwallet.wallet.WalletsMaster;
import com.ravenwallet.wallet.abstracts.BaseWalletManager;
import com.ravenwallet.wallet.RvnWalletManager;

import java.math.BigDecimal;

import static com.platform.assets.AssetType.NEW_ASSET;
import static com.platform.assets.AssetsValidation.isAssetNameValid;
import static com.ravenwallet.presenter.activities.AddressBookActivity.PICK_ADDRESS_VIEW_EXTRAS_KEY;
import static com.ravenwallet.tools.animation.BRAnimator.animateBackgroundDim;
import static com.ravenwallet.tools.animation.BRAnimator.animateSignalSlide;
import static com.ravenwallet.tools.util.BRConstants.CREATION_FEE;
import static com.ravenwallet.tools.util.BRConstants.MAX_ASSET_NAME_LENGTH;
import static com.ravenwallet.tools.util.BRConstants.MAX_ASSET_QUANTITY;
import static com.ravenwallet.tools.util.BRConstants.SATOSHIS;


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

public class FragmentCreateAsset extends BaseAddressAndIpfsHashValidation implements WalletManagerListener {

    private static final String TAG = FragmentCreateAsset.class.getName();

    private ScrollView backgroundLayout;
    private LinearLayout signalLayout, /*quantityKeyboardLayout, unitsKeyboardLayout, */
            balanceLayout;
    private EditText unitsEditText;
    private EditText quantityEditText;
    //    private StringBuilder quantityBuilder;
    private BRLinearLayoutWithCaret feeLayout;
    //    private BRKeyboard quantityKeyboard, unitsKeyboard;
    private BRButton regular;
    private BRButton economy;
    private TextView balanceText, feeText, creationFeeText;
    private BRText feeDescription;
    private BRText warningText;
    private BREdit assetName;
    private TextInputLayout assetNameLayout, addressInputLayout, quantityInputLayout, unitInputLayout, ipfsHashInputLayout;
    private ImageView imgValid;
    private RelativeLayout reissuableLayout, addressLayout;
    private CheckBox reissuableCheckBox, ipfsHashCheckBox;
    private PasteButton pasteIPFSHashButton;
    private ScanButton scanIPFSHashButton;
    private ContactButton importContactButton;
    private BRButton checkNameAvailableButton, createAssetButton, checkingButton;
    private NameStatus mNameStatus = NameStatus.UNCHECKED;
    private boolean hasCreationFee = false;
    private QuantityTextWatcher quantityTextWatcher;
    private UnitTextWatcher unitTextWatcher;

    public static FragmentCreateAsset newInstance() {
        FragmentCreateAsset fragment = new FragmentCreateAsset();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_asset, container, false);

        backgroundLayout = rootView.findViewById(R.id.background_layout);
        signalLayout = rootView.findViewById(R.id.signal_layout);
        addressLayout = rootView.findViewById(R.id.layout_address);
        addressEditText = rootView.findViewById(R.id.address_edit_text);
        quantityEditText = rootView.findViewById(R.id.quantity_edit_text);
        unitsEditText = rootView.findViewById(R.id.units_edit_text);
        feeLayout = rootView.findViewById(R.id.fee_buttons_layout);
//        quantityKeyboardLayout = rootView.findViewById(R.id.quantity_keyboard_layout);
//        unitsKeyboardLayout = rootView.findViewById(R.id.units_keyboard_layout);
//        quantityKeyboard = rootView.findViewById(R.id.quantity_keyboard);
//        unitsKeyboard = rootView.findViewById(R.id.units_keyboard);
        balanceLayout = rootView.findViewById(R.id.balance_layout);
        feeDescription = rootView.findViewById(R.id.fee_description);
        warningText = rootView.findViewById(R.id.warning_text);
        regular = rootView.findViewById(R.id.left_button);
        economy = rootView.findViewById(R.id.right_button);
        balanceText = rootView.findViewById(R.id.balance_text);
        feeText = rootView.findViewById(R.id.fee_text);
        creationFeeText = rootView.findViewById(R.id.asset_creation_fee_text);
        reissuableLayout = rootView.findViewById(R.id.reissuable_layout);
        reissuableCheckBox = rootView.findViewById(R.id.reissuable_checkbox);
        ipfsHashEditText = rootView.findViewById(R.id.ipfs_hash_edit_text);
        ipfsHashCheckBox = rootView.findViewById(R.id.ipfs_hash_checkbox);
        pasteIPFSHashButton = rootView.findViewById(R.id.paste_ipfs_hash_button);
        scanIPFSHashButton = rootView.findViewById(R.id.scan_ipfs_hash_button);
        importContactButton = rootView.findViewById(R.id.import_contact);
        checkNameAvailableButton = rootView.findViewById(R.id.check_name_available_button);
        createAssetButton = rootView.findViewById(R.id.create_asset_button);

        assetName = rootView.findViewById(R.id.asset_name);
        assetNameLayout = rootView.findViewById(R.id.asset_name_layout);
        addressInputLayout = rootView.findViewById(R.id.address_input_layout);
        quantityInputLayout = rootView.findViewById(R.id.quantity_input_layout);
        unitInputLayout = rootView.findViewById(R.id.unit_input_layout);
        ipfsHashInputLayout = rootView.findViewById(R.id.ipfs_hash_input_layout);
        imgValid = rootView.findViewById(R.id.img_valid);

        addressLayout.setVisibility(isShowAddressInput() ? View.VISIBLE : View.GONE);
        quantityTextWatcher = new QuantityTextWatcher(quantityEditText);
        unitTextWatcher = new UnitTextWatcher(unitsEditText, 0);
        setListeners(rootView);

        setButton(true);
        signalLayout.setOnTouchListener(new SlideDetector(getContext(), signalLayout));

        // Set keyboards background color
//        quantityKeyboard.setBRButtonBackgroundResId(R.drawable.keyboard_white_button);
//        quantityKeyboard.setBRKeyboardColor(R.color.white);
//        unitsKeyboard.setBRButtonBackgroundResId(R.drawable.keyboard_white_button);
//        unitsKeyboard.setBRKeyboardColor(R.color.white);
        assetName.setFilters(new InputFilter[]{new AssetNameFilter(),
                new InputFilter.AllCaps(), new InputFilter.LengthFilter(MAX_ASSET_NAME_LENGTH)});
//        quantityBuilder = new StringBuilder(0);
        setBalanceAndTransactionFee();
        reissuableCheckBox.setChecked(true);
        unitsEditText.setText("0");
        isTransfer = false;
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
        assetName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                imgValid.setVisibility(View.GONE);
                assetName.setTextColor(getResources().getColor(R.color.black, getActivity().getTheme()));
                mNameStatus = NameStatus.UNCHECKED;
            }
        });
        assetName.addTextChangedListener(new InputTextWatcher(assetNameLayout));
        addressEditText.addTextChangedListener(new InputTextWatcher(addressInputLayout));
        quantityEditText.addTextChangedListener(new InputTextWatcher(quantityInputLayout));
        unitsEditText.addTextChangedListener(new InputTextWatcher(unitInputLayout));
        ipfsHashEditText.addTextChangedListener(new InputTextWatcher(ipfsHashInputLayout));

        // check name available button click
        checkNameAvailableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = assetName.getText().toString();
                NameStatus nameStatus = isNameValid(name);
                switch (nameStatus) {
                    case INVALID:
                        assetNameLayout.setError("Name invalid");
//                        sayCustomMessage("Name invalid");
                        break;
                    case VALID:
                        checkingButton = checkNameAvailableButton;
                        isNameAvailable(name);
                }
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

        // Handling the paste IPFS Hash button click
        pasteIPFSHashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postIPFSHashPasted(ipfsHashEditText);
            }
        });

        // Handling create asset button click
        createAssetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!hasCreationFee) {
                    sayCustomMessage("insufficient Fees");
                    return;
                }
                switch (mNameStatus) {
                    case INVALID:
                        assetNameLayout.setError("Name invalid");
//                        sayCustomMessage("Name invalid");
                        break;
                    case UNCHECKED:
                        NameStatus nameStatus = isNameValid(assetName.getText().toString());
                        if (nameStatus == NameStatus.INVALID) {
                            assetNameLayout.setError("Name invalid");
                        } else {
                            checkingButton = createAssetButton;
                            isNameAvailable(assetName.getText().toString());
                        }
                        break;
                    case VALID:
                        checkingButton = createAssetButton;
                        isNameAvailable(assetName.getText().toString());
                        break;
                    case AVAILABLE:
                        createAsset();
                        break;
                    case UNAVAILABLE:
//                        sayCustomMessage("Name unavailable");
                        assetNameLayout.setError("Name unavailable");

                        break;
                }

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

        quantityEditText.addTextChangedListener(quantityTextWatcher);
        unitsEditText.addTextChangedListener(unitTextWatcher);
//        unitsEditText.setFilters(new InputFilter[]{new InputFilterMinMax(0,8)});
        quantityEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(0),
                new InputFilterMinMax(1, MAX_ASSET_QUANTITY)});
//        bindKeyboardViewWithInputField(quantityKeyboardLayout, quantityEditText);
//        bindKeyboardViewWithInputField(unitsKeyboardLayout, unitsEditText);
//        quantityEditText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                boolean visible = quantityKeyboardLayout.getVisibility() == View.VISIBLE;
//                quantityKeyboardLayout.setVisibility(visible ? View.GONE : View.VISIBLE);
//            }
//        });
//        unitsEditText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                boolean visible = unitsKeyboardLayout.getVisibility() == View.VISIBLE;
//                unitsKeyboardLayout.setVisibility(visible ? View.GONE : View.VISIBLE);
//            }
//        });
//        quantityKeyboard.addOnInsertListener(new BRKeyboard.OnInsertListener() {
//            @Override
//            public void onClick(String key) {
//                handleQuantityKeyboardClick(key);
//            }
//        });
//        unitsKeyboard.addOnInsertListener(new BRKeyboard.OnInsertListener() {
//            @Override
//            public void onClick(String key) {
//                handleUnitsKeyboardClick(key);
//            }
//        });

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

        importContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddressBookActivity.class);
                intent.putExtra(PICK_ADDRESS_VIEW_EXTRAS_KEY, true);
                getActivity().startActivityForResult(intent, BRConstants.SELECT_FROM_ADDRESS_BOOK_REQUEST);
                getActivity().overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
            }
        });

        imgValid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NameStatus.AVAILABLE != mNameStatus)
                    assetName.setText("");
            }
        });
    }

    private void createAsset() {
        String address;
        if (isShowAddressInput()) {
            address = addressEditText.getText().toString();
            if (TextUtils.isEmpty(address)) {
                addressInputLayout.setError(getString(R.string.Send_emptyAddressMessage));
                return;
            }
            boolean isAddressValid = isAddressValid(address);
            if (!isAddressValid) {
                addressInputLayout.setError(getString(R.string.Send_invalidAddressMessage));
//                sayInvalidAddress();
                return;
            }
        } else {
            address = getAddress();
        }

        double quantity = quantityTextWatcher.getValue();
        if (quantity <= 0) {
            quantityInputLayout.setError("Invalid Quantity");
//            sayCustomMessage("Invalid Quantity");
            return;
        }

        int unit = unitTextWatcher.getValue();
        if (unit > 8) {
            unitInputLayout.setError("invalid unit");
//            sayCustomMessage("invalid unit");
            return;
        }

        if (ipfsHashCheckBox.isChecked() && !isIPFSHashValid(ipfsHashEditText.getText().toString())) {
            ipfsHashInputLayout.setError(getString(R.string.ipfs_hash_invalid));
//            sayInvalidIPFSHash();
            return;
        }

        BRCoreTransactionAsset asset = new BRCoreTransactionAsset();
        asset.setType(NEW_ASSET.getIndex());
        String name = assetName.getText().toString();
        asset.setName(name);
        asset.setNamelen(name.length());
        asset.setAmount((long) (quantity * SATOSHIS));
        asset.setReissuable(reissuableCheckBox.isChecked() ? 1 : 0);
        asset.setHasIPFS(ipfsHashCheckBox.isChecked() ? 1 : 0);
        asset.setIPFSHash(ipfsHashEditText.getText().toString());
        asset.setUnit(unit);
        createTransactionAsset(asset, address);
    }

    private void onValidateName(NameStatus nameStatus) {
        mNameStatus = nameStatus;
        checkNameAvailableButton.setText(getString(R.string.txt_btn_check_availability));
        createAssetButton.setText(getString(R.string.txt_btn_create_asset));

        switch (nameStatus) {
            case UNAVAILABLE:
                assetName.setTextColor(getResources().getColor(R.color.red_text, getActivity().getTheme()));
                imgValid.setImageResource(R.drawable.ic_name_invalid);
                imgValid.setVisibility(View.VISIBLE);
                break;
            case AVAILABLE:
                if (checkingButton != null) {
                    if (checkingButton == checkNameAvailableButton) {
                        assetName.setTextColor(getResources().getColor(R.color.green_text, getActivity().getTheme()));
                        imgValid.setImageResource(R.drawable.ic_name_valid);
                        imgValid.setVisibility(View.VISIBLE);
                    } else if (checkingButton == createAssetButton) {
                        createAsset();
                    }
                }
                break;
        }
    }

    private void createTransactionAsset(final BRCoreTransactionAsset asset, String address) {
        final Activity app = getActivity();
        if (app == null) return;
        final RvnWalletManager walletManager = RvnWalletManager.getInstance(app);
        walletManager.requestConfirmation(app, NEW_ASSET, asset, null, address, false, FragmentCreateAsset.this);
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

    private NameStatus isNameValid(String name) {
        if (TextUtils.isEmpty(name) || !isAssetNameValid(name.toUpperCase()))
            return NameStatus.INVALID;
        else {
            AssetsRepository repository = AssetsRepository.getInstance(getContext());
            boolean nameExist = repository.checkNameAssetExist(name.toUpperCase());
            if (nameExist)
                return NameStatus.INVALID;
            else return NameStatus.VALID;
        }
    }

    private void isNameAvailable(String name) {
        if (checkingButton != null)
            checkingButton.setText(getString(R.string.txt_checking_availability));
        checkNameAvailableButton.setEnabled(false);
        createAssetButton.setEnabled(false);
        RvnWalletManager walletManager = RvnWalletManager.getInstance(getActivity());
        BRCoreWallet wallet = walletManager.getWallet();
        wallet.isAssetNameValid(walletManager.getPeerManager(), name, name.length(), this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkNameAvailableButton.setEnabled(true);
                checkNameAvailableButton.setText(getString(R.string.txt_btn_check_availability));
                createAssetButton.setEnabled(true);
                createAssetButton.setText(getString(R.string.txt_btn_create_asset));

            }
        }, 3000);
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
            unitsEditText.setText(String.valueOf(key.charAt(0)));
        }
    }

//    private void handleQuantityKeyboardClick(String key) {
//        if (key == null) {
//            Log.e(TAG, "handleClick: key is null! ");
//            return;
//        }
//        if (key.isEmpty()) {
//            handleDeleteClick();
//        } else if (Character.isDigit(key.charAt(0))) {
//            handleDigitClick(Integer.parseInt(key.substring(0, 1)));
//        }
//    }

//    private void handleDigitClick(Integer digit) {
//        String currentAmount = quantityBuilder.toString();
//        if (new BigDecimal(currentAmount.concat(String.valueOf(digit))).doubleValue() <= MAX_ASSET_QUANTITY) {
//            quantityBuilder.append(digit);
//            setQuantity();
//        }
//    }
//
//    private void handleDeleteClick() {
//        String currentQuantity = quantityBuilder.toString();
//        if (currentQuantity.length() > 0) {
//            quantityBuilder.deleteCharAt(currentQuantity.length() - 1);
//            setQuantity();
//        }
//    }

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
            economy.setTextColor(getContext().getColor(R.color.primaryColor));
            economy.setBackground(getContext().getDrawable(R.drawable.b_half_right_blue_stroke));
            feeDescription.setText(String.format(getString(R.string.FeeSelector_estimatedDeliver), getString(R.string.FeeSelector_regularTime)));
            warningText.getLayoutParams().height = 0;
        } else {
            BRSharedPrefs.putFavorStandardFee(getActivity(), iso, false);
            regular.setTextColor(getContext().getColor(R.color.primaryColor));
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

        //format the asset creation fee to the selected ISO
        BigDecimal isoAssetCreationFee = new BigDecimal(CREATION_FEE);
        String formattedAssetCreationFee = CurrencyUtils.getFormattedAmount(app, wallet.getIso(app), wallet.getSmallestCryptoForCrypto(app, isoAssetCreationFee));

        hasCreationFee = isoBalance.compareTo(isoAssetCreationFee.add(isoFee)) > 0;
        setBalanceTextStyle();
        // formattedBalance
        String formattedBalance = CurrencyUtils.getFormattedAmount(app, defaultIso, isIsoCrypto ? wallet.getSmallestCryptoForCrypto(app, isoBalance) : isoBalance);
        String balanceString = String.format(getString(R.string.Send_balance), formattedBalance);
        balanceText.setText(balanceString);
        feeText.setText(String.format(getString(R.string.Send_fee), formattedFee));
        creationFeeText.setText(String.format(getString(R.string.asset_creation_fee), formattedAssetCreationFee));
        balanceLayout.requestLayout();
    }

    private void setBalanceTextStyle() {
        if (hasCreationFee) {
            balanceText.setTextColor(getContext().getColor(R.color.light_gray));
            feeText.setTextColor(getContext().getColor(R.color.light_gray));
            creationFeeText.setTextColor(getContext().getColor(R.color.light_gray));
        } else {
            balanceText.setTextColor(getContext().getColor(R.color.warning_color));
            feeText.setTextColor(getContext().getColor(R.color.warning_color));
            creationFeeText.setTextColor(getContext().getColor(R.color.warning_color));
        }
    }

//    private void setQuantity() {
//        String tmpAmount = quantityBuilder.toString();
//        int divider = tmpAmount.length();
//        if (tmpAmount.contains(".")) {
//            divider = tmpAmount.indexOf(".");
//        }
//        StringBuilder newAmount = new StringBuilder();
//        for (int i = 0; i < tmpAmount.length(); i++) {
//            newAmount.append(tmpAmount.charAt(i));
//            if (divider > 3 && divider - 1 != i && divider > i && ((divider - i - 1) % 3 == 0)) {
//                newAmount.append(",");
//            }
//        }
//        quantityEditText.setText(newAmount);
//    }

    private void bindKeyboardViewWithInputField(final ViewGroup keyboardLayout, EditText
            inputField) {
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

    // called by getAssetData JNI
    public void onCheckNameBack(int isAvailable) {
        Log.d(TAG, "isAvailable " + isAvailable);
        final NameStatus nameStatus = isAvailable == 1 ? NameStatus.AVAILABLE : NameStatus.UNAVAILABLE;
        Activity activity = getActivity();
        if (activity != null)
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onValidateName(nameStatus);
                }
            });
    }

    @Override
    public void close() {
        closeMe();
    }

    @Override
    public void error(String error) {
        sayCustomMessage(error);
    }

    enum NameStatus {
        UNCHECKED, VALID, INVALID, CHECKING_AVAILABILITY, AVAILABLE, UNAVAILABLE
    }


}