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

import com.platform.assets.Asset;
import com.platform.assets.AssetType;
import com.platform.assets.AssetsRepository;
import com.platform.assets.AssetsValidation;
import com.platform.assets.filters.AssetNameFilter;
import com.platform.assets.filters.DecimalDigitsInputFilter;
import com.platform.assets.filters.InputFilterMinMax;
import com.platform.assets.filters.QuantityTextWatcher;
import com.platform.assets.filters.UnitTextWatcher;
import com.ravenwallet.R;
import com.ravenwallet.core.BRCoreAddress;
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

import static com.platform.assets.AssetsValidation.isAssetNameValid;
import static com.ravenwallet.presenter.activities.AddressBookActivity.PICK_ADDRESS_VIEW_EXTRAS_KEY;
import static com.ravenwallet.tools.animation.BRAnimator.animateBackgroundDim;
import static com.ravenwallet.tools.animation.BRAnimator.animateSignalSlide;
import static com.ravenwallet.tools.util.BRConstants.MAX_ASSET_NAME_LENGTH;
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

public class FragmentIssueSubAsset extends BaseAddressAndIpfsHashValidation implements WalletManagerListener {

    private static final String TAG = FragmentIssueSubAsset.class.getName();

    public static final long MAX_ASSET_QUANTITY = 21000000 * 1000L;
    public static final int SUB_ASSET_CREATION_FEE = 100;
    private ScrollView backgroundLayout;
    private LinearLayout signalLayout, /*quantityKeyboardLayout, unitsKeyboardLayout,*/
            balanceLayout;
    private EditText quantityEditText, unitsEditText;
    //    private StringBuilder quantityBuilder;
    private BRLinearLayoutWithCaret feeLayout;
    //    private BRKeyboard quantityKeyboard, unitsKeyboard;
    private BRButton regular;
    private BRButton economy;
    private TextView balanceText, feeText, subAssetFeeText;
    private BRText feeDescription;
    private BRText warningText;
    private BREdit assetName;
    private BRText rootAssetName;
    private ImageView imgValid;
    private RelativeLayout reissuableLayout, addressLayout;
    private CheckBox reissuableCheckBox, ipfsHashCheckBox;
    private PasteButton pasteIPFSHashButton;
    private ScanButton scanIPFSHashButton;
    private Asset mAsset;
    private BRButton checkNameAvailableButton, createSubAssetButton, checkingButton;
    private FragmentCreateAsset.NameStatus mNameStatus = FragmentCreateAsset.NameStatus.UNCHECKED;
    private QuantityTextWatcher quantityTextWatcher;
    private UnitTextWatcher unitTextWatcher;
    private TextInputLayout assetNameLayout, addressInputLayout, quantityInputLayout, unitInputLayout, ipfsHashInputLayout;
    public static FragmentIssueSubAsset newInstance(Asset asset) {
        FragmentIssueSubAsset fragment = new FragmentIssueSubAsset();
        Bundle bundle = new Bundle();
        bundle.putParcelable("asset", asset);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_issue_sub_asset, container, false);

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
        subAssetFeeText = rootView.findViewById(R.id.sub_asset_fee_text);
        reissuableLayout = rootView.findViewById(R.id.reissuable_layout);
        reissuableCheckBox = rootView.findViewById(R.id.reissuable_checkbox);
        ipfsHashEditText = rootView.findViewById(R.id.ipfs_hash_edit_text);
        ipfsHashCheckBox = rootView.findViewById(R.id.ipfs_hash_checkbox);
        pasteIPFSHashButton = rootView.findViewById(R.id.paste_ipfs_hash_button);
        scanIPFSHashButton = rootView.findViewById(R.id.scan_ipfs_hash_button);
        assetName = rootView.findViewById(R.id.asset_name);
        rootAssetName = rootView.findViewById(R.id.root_asset_name);
        assetNameLayout = rootView.findViewById(R.id.asset_name_layout);
        addressInputLayout = rootView.findViewById(R.id.address_input_layout);
        quantityInputLayout = rootView.findViewById(R.id.quantity_input_layout);
        unitInputLayout = rootView.findViewById(R.id.unit_input_layout);
        ipfsHashInputLayout = rootView.findViewById(R.id.ipfs_hash_input_layout);
        imgValid = rootView.findViewById(R.id.img_valid);
        checkNameAvailableButton = rootView.findViewById(R.id.check_name_available_button);
        createSubAssetButton = rootView.findViewById(R.id.create_sub_asset_button);

        if (getArguments() != null) {
            mAsset = getArguments().getParcelable("asset");
            rootAssetName.setText(mAsset.getName() + AssetsValidation.SUB_NAME_DELIMITER);
        }
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
        int maxLength = MAX_ASSET_NAME_LENGTH - rootAssetName.getText().toString().length();
        assetName.setFilters(new InputFilter[]{new AssetNameFilter(),new InputFilter.AllCaps(), new InputFilter.LengthFilter(maxLength)});

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
//                if (!assetName.getText().toString().startsWith(mAsset.getName() + "/")) {
//                    assetName.setText(mAsset.getName() + "/");
//                    assetName.append("");
//                }
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
                String fullName = rootAssetName.getText().toString() + assetName.getText().toString();
                FragmentCreateAsset.NameStatus nameStatus = isNameValid(fullName);
                switch (nameStatus) {
                    case INVALID:
                        assetNameLayout.setError("Name invalid");
                        break;
                    case VALID:
                        checkingButton = checkNameAvailableButton;
                        isNameAvailable(fullName);
                }
            }
        });

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

        quantityEditText.addTextChangedListener(quantityTextWatcher);
        unitsEditText.addTextChangedListener(unitTextWatcher);
        quantityEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(0),
                new InputFilterMinMax(1, MAX_ASSET_QUANTITY)});
//        bindKeyboardViewWithInputField(quantityKeyboardLayout, quantityEditText);
//        bindKeyboardViewWithInputField(unitsKeyboardLayout, unitsEditText);

//        quantityEditText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (quantityKeyboardLayout.getVisibility() == View.VISIBLE) {
//                    quantityKeyboardLayout.setVisibility(View.GONE);
//                }
//            }
//        });
//        unitsEditText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (unitsKeyboardLayout.getVisibility() == View.VISIBLE) {
//                    unitsKeyboardLayout.setVisibility(View.GONE);
//                }
//            }
//        });
//
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

        createSubAssetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = rootAssetName.getText().toString() + assetName.getText().toString();
                switch (mNameStatus) {
                    case INVALID:
                        assetNameLayout.setError("Name invalid");
                        break;
                    case UNCHECKED:
                        FragmentCreateAsset.NameStatus nameStatus = isNameValid(fullName);
                        if (nameStatus == FragmentCreateAsset.NameStatus.INVALID) {
                            assetNameLayout.setError("Name invalid");
                        } else {
                            checkingButton = createSubAssetButton;
                            isNameAvailable(fullName);
                        }
                        break;
                    case VALID:
                        checkingButton = createSubAssetButton;
                        isNameAvailable(fullName);
                        break;
                    case AVAILABLE:
                        issueSubAsset();
                        break;
                    case UNAVAILABLE:
                        assetNameLayout.setError("Name unavailable");
                        break;
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
        imgValid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assetName.setText("");
            }
        });
    }

    private void issueSubAsset() {
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
                return;
            }
        } else {
            address = getAddress();
        }

        double quantity = quantityTextWatcher.getValue();
        if (quantity <= 0) {
            quantityInputLayout.setError("Invalid Quantity");
            return;
        }

        int unit = unitTextWatcher.getValue();
        if (unit > 8) {
            unitInputLayout.setError("invalid unit");
            return;
        }

        if (ipfsHashCheckBox.isChecked() && !isIPFSHashValid(ipfsHashEditText.getText().toString())) {
            ipfsHashInputLayout.setError(getString(R.string.ipfs_hash_invalid));
            return;
        }

        BRCoreTransactionAsset asset = new BRCoreTransactionAsset();
        asset.setType(AssetType.NEW_ASSET.getIndex());
        String fullName = rootAssetName.getText().toString() + assetName.getText().toString();
        asset.setName(fullName);
        asset.setNamelen(fullName.length());
        asset.setAmount((long) (quantity * SATOSHIS));
        asset.setReissuable(reissuableCheckBox.isChecked() ? 1 : 0);
        asset.setHasIPFS(ipfsHashCheckBox.isChecked() ? 1 : 0);
        asset.setIPFSHash(ipfsHashEditText.getText().toString());
        asset.setUnit(unit);
        createSubAssetTx(asset, address);
    }

    private void createSubAssetTx(final BRCoreTransactionAsset asset, String address) {
        final Activity app = getActivity();
        if (app == null) return;
        final BRCoreAddress BrAddress = new BRCoreAddress(address);
        final RvnWalletManager walletManager = RvnWalletManager.getInstance(app);
        final BRCoreTransactionAsset rootAsset = mAsset.getCoreAsset();
        walletManager.requestConfirmation(app, AssetType.SUB, asset, rootAsset, address, false, FragmentIssueSubAsset.this);
    }


    private void closeMe() {
        Activity app = getActivity();
        if (app != null && !app.isDestroyed())
            app.getFragmentManager().popBackStack();
    }

    private FragmentCreateAsset.NameStatus isNameValid(String name) {
        if (TextUtils.isEmpty(name) || !isAssetNameValid(name.toUpperCase()))
            return FragmentCreateAsset.NameStatus.INVALID;
        else {
            AssetsRepository repository = AssetsRepository.getInstance(getContext());
            boolean nameExist = repository.checkNameAssetExist(name.toUpperCase());
            if (nameExist)
                return FragmentCreateAsset.NameStatus.INVALID;
            else return FragmentCreateAsset.NameStatus.VALID;
        }
    }

    private void isNameAvailable(String name) {
        if (checkingButton != null)
            checkingButton.setText(getString(R.string.txt_checking_availability));
        RvnWalletManager walletManager = RvnWalletManager.getInstance(getActivity());
        BRCoreWallet wallet = walletManager.getWallet();
        wallet.isAssetNameValid(walletManager.getPeerManager(), name, name.length(), this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkNameAvailableButton.setEnabled(true);
                checkNameAvailableButton.setText(getString(R.string.txt_btn_check_availability));
                createSubAssetButton.setEnabled(true);
                createSubAssetButton.setText(getString(R.string.txt_btn_create_asset));

            }
        }, 3000);
    }

    private void onValidateName(FragmentCreateAsset.NameStatus nameStatus) {
        mNameStatus = nameStatus;
        checkNameAvailableButton.setText(getString(R.string.txt_btn_check_availability));
        createSubAssetButton.setText(getString(R.string.txt_btn_create_asset));

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
                    } else if (checkingButton == createSubAssetButton) {
                        createSubAssetButton.setText(getString(R.string.txt_btn_create_asset));
                        issueSubAsset();
                    }
                }
                break;
        }
    }

    // called by getAssetData JNI
    public void onCheckNameBack(int isAvailable) {
        Log.d(TAG, "isAvailable " + isAvailable);
        final FragmentCreateAsset.NameStatus nameStatus = isAvailable == 1 ? FragmentCreateAsset.NameStatus.AVAILABLE : FragmentCreateAsset.NameStatus.UNAVAILABLE;
        Activity activity = getActivity();
        if (activity != null)
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onValidateName(nameStatus);
                }
            });
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
//
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
        BigDecimal isoSubAssetFee = new BigDecimal(SUB_ASSET_CREATION_FEE);
        String formattedSubAssetFee = CurrencyUtils.getFormattedAmount(app, wallet.getIso(app), wallet.getSmallestCryptoForCrypto(app, isoSubAssetFee));

        setBalanceTextStyle(isoBalance, isoSubAssetFee.add(isoFee));

        // formattedBalance
        String formattedBalance = CurrencyUtils.getFormattedAmount(app, defaultIso, isIsoCrypto ? wallet.getSmallestCryptoForCrypto(app, isoBalance) : isoBalance);
        String balanceString = String.format(getString(R.string.Send_balance), formattedBalance);
        balanceText.setText(balanceString);
        feeText.setText(String.format(getString(R.string.Send_fee), formattedFee));
        subAssetFeeText.setText(String.format(getString(R.string.sub_asset_creation_fee), formattedSubAssetFee));
        balanceLayout.requestLayout();
    }

    private void setBalanceTextStyle(BigDecimal balance, BigDecimal fees) {
        if (balance.compareTo(fees) > 0) {
            balanceText.setTextColor(getContext().getColor(R.color.light_gray));
            feeText.setTextColor(getContext().getColor(R.color.light_gray));
            subAssetFeeText.setTextColor(getContext().getColor(R.color.light_gray));
        } else {
            balanceText.setTextColor(getContext().getColor(R.color.warning_color));
            feeText.setTextColor(getContext().getColor(R.color.warning_color));
            subAssetFeeText.setTextColor(getContext().getColor(R.color.warning_color));
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