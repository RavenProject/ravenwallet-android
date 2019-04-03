package com.ravencoin.presenter.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.annotation.Nullable;
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
import com.platform.assets.AssetsRepository;
import com.ravencoin.R;
import com.ravencoin.core.BRCoreAddress;
import com.ravencoin.core.BRCoreTransactionAsset;
import com.ravencoin.core.BRCoreWallet;
import com.ravencoin.presenter.activities.AddressBookActivity;
import com.ravencoin.presenter.customviews.BRButton;
import com.ravencoin.presenter.customviews.BREdit;
import com.ravencoin.presenter.customviews.BRLinearLayoutWithCaret;
import com.ravencoin.presenter.customviews.BRText;
import com.ravencoin.presenter.customviews.ContactButton;
import com.ravencoin.presenter.customviews.PasteButton;
import com.ravencoin.presenter.customviews.ScanButton;
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

import static com.platform.assets.AssetType.NEW_ASSET;
import static com.platform.assets.AssetType.UNIQUE;
import static com.platform.assets.AssetsValidation.isAssetNameValid;
import static com.ravencoin.presenter.activities.AddressBookActivity.PICK_ADDRESS_VIEW_EXTRAS_KEY;
import static com.ravencoin.tools.animation.BRAnimator.animateBackgroundDim;
import static com.ravencoin.tools.animation.BRAnimator.animateSignalSlide;
import static com.ravencoin.tools.util.BRConstants.MAX_ASSET_NAME_LENGTH;
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

public class FragmentIssueUniqueAsset extends BaseAddressAndIpfsHashValidation implements WalletManagerListener {

    private static final String TAG = FragmentIssueUniqueAsset.class.getName();
    private static final String UNIQUE_SUFFIX = "#";
    public static final int UNIQUE_ASSET_CREATION_FEE = 5;
    private ScrollView backgroundLayout;
    private LinearLayout signalLayout, balanceLayout;
    private BRLinearLayoutWithCaret feeLayout;
    private BRButton regular;
    private BRButton economy;
    private TextView balanceText, feeText, uniqueAssetFeeText;
    private BRText feeDescription;
    private BRText warningText;
    private BREdit assetName;
    private ImageView imgValid;
    private CheckBox ipfsHashCheckBox;
    private PasteButton pasteIPFSHashButton;
    private ScanButton scanIPFSHashButton;
    private BRButton checkingButton, checkNameAvailableButton, createUniqueAssetButton;
    private Asset mAsset;
    private FragmentCreateAsset.NameStatus mNameStatus = FragmentCreateAsset.NameStatus.UNCHECKED;
    private RelativeLayout addressLayout;

    public static FragmentIssueUniqueAsset newInstance(Asset asset) {
        FragmentIssueUniqueAsset fragment = new FragmentIssueUniqueAsset();
        Bundle bundle = new Bundle();
        bundle.putParcelable("asset", asset);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_issue_unique_asset, container, false);

        backgroundLayout = rootView.findViewById(R.id.background_layout);
        signalLayout = rootView.findViewById(R.id.signal_layout);
        addressLayout = rootView.findViewById(R.id.layout_address);
        addressEditText = rootView.findViewById(R.id.address_edit_text);
        feeLayout = rootView.findViewById(R.id.fee_buttons_layout);
        balanceLayout = rootView.findViewById(R.id.balance_layout);
        feeDescription = rootView.findViewById(R.id.fee_description);
        warningText = rootView.findViewById(R.id.warning_text);
        regular = rootView.findViewById(R.id.left_button);
        economy = rootView.findViewById(R.id.right_button);
        balanceText = rootView.findViewById(R.id.balance_text);
        feeText = rootView.findViewById(R.id.fee_text);
        uniqueAssetFeeText = rootView.findViewById(R.id.unique_asset_fee_text);
        ipfsHashEditText = rootView.findViewById(R.id.ipfs_hash_edit_text);
        ipfsHashCheckBox = rootView.findViewById(R.id.ipfs_hash_checkbox);
        pasteIPFSHashButton = rootView.findViewById(R.id.paste_ipfs_hash_button);
        scanIPFSHashButton = rootView.findViewById(R.id.scan_ipfs_hash_button);
        assetName = rootView.findViewById(R.id.asset_name);
        imgValid = rootView.findViewById(R.id.img_valid);
        checkNameAvailableButton = rootView.findViewById(R.id.check_name_available_button);
        createUniqueAssetButton = rootView.findViewById(R.id.create_unique_asset_button);

        if (getArguments() != null) {
            mAsset = getArguments().getParcelable("asset");
            assetName.setText(mAsset.getName() + UNIQUE_SUFFIX);
        }

        boolean isExpertMode = BRSharedPrefs.getExpertMode(getActivity());
        addressLayout.setVisibility(isExpertMode ? View.VISIBLE : View.GONE);
        setListeners(rootView);

        signalLayout.setOnTouchListener(new SlideDetector(getContext(), signalLayout));

        assetName.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(MAX_ASSET_NAME_LENGTH)});
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
                if (!assetName.getText().toString().startsWith(mAsset.getName() + UNIQUE_SUFFIX)) {
                    assetName.setText(mAsset.getName() + UNIQUE_SUFFIX);
                    assetName.append("");
                }
            }
        });
        // check name available button click
        checkNameAvailableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = assetName.getText().toString();
                FragmentCreateAsset.NameStatus nameStatus = isNameValid(name);
                switch (nameStatus) {
                    case INVALID:
                        sayCustomMessage("Name invalid");
                        break;
                    case VALID:
                        checkingButton = checkNameAvailableButton;
                        isNameAvailable(name);
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

        ipfsHashCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pasteIPFSHashButton.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                scanIPFSHashButton.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });

        createUniqueAssetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mNameStatus) {
                    case INVALID:
                        sayCustomMessage("Name invalid");
                        break;
                    case UNCHECKED:
                        FragmentCreateAsset.NameStatus nameStatus = isNameValid(assetName.getText().toString());
                        if (nameStatus == FragmentCreateAsset.NameStatus.INVALID) {
                            sayCustomMessage("Name invalid");
                        } else {
                            checkingButton = createUniqueAssetButton;
                            isNameAvailable(assetName.getText().toString());
                        }
                        break;
                    case VALID:
                        checkingButton = createUniqueAssetButton;
                        isNameAvailable(assetName.getText().toString());
                        break;
                    case AVAILABLE:
                        createUniqueAsset();
                        break;
                    case UNAVAILABLE:
                        sayCustomMessage("Name unavailable");
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


    private void createUniqueAsset() {
        String address;
        boolean isExpertMode = BRSharedPrefs.getExpertMode(getContext());
        if (isExpertMode) {
            address = addressEditText.getText().toString();
            boolean isAddressValid = isAddressValid(address);
            if (!isAddressValid) {
                sayInvalidAddress();
                return;
            }
        } else {
            address = getAddress();
        }

        if (ipfsHashCheckBox.isChecked() && !isIPFSHashValid(ipfsHashEditText.getText().toString())) {
            sayInvalidIPFSHash();
            return;
        }

        BRCoreTransactionAsset asset = new BRCoreTransactionAsset();
        asset.setType(NEW_ASSET.getIndex());
        String name = assetName.getText().toString();
        asset.setName(name);
        asset.setNamelen(name.length());
        asset.setAmount((long) SATOSHIS);
        asset.setReissuable(0);
        asset.setHasIPFS(ipfsHashCheckBox.isChecked() ? 1 : 0);
        asset.setIPFSHash(ipfsHashEditText.getText().toString());
        asset.setUnit(0);
        createUniqueAssetTransaction(asset, address);
    }

    private void onValidateName(FragmentCreateAsset.NameStatus nameStatus) {
        mNameStatus = nameStatus;
        checkNameAvailableButton.setText("Check availability");

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
                    } else if (checkingButton == createUniqueAssetButton) {
                        createUniqueAssetButton.setText("Create asset");
                        createUniqueAsset();
                    }
                }
                break;
        }
    }

    private void createUniqueAssetTransaction(final BRCoreTransactionAsset asset, String address) {
        final Activity app = getActivity();
        if (app == null) return;
        final BRCoreAddress BrAddress = new BRCoreAddress(address);
        final RvnWalletManager walletManager = RvnWalletManager.getInstance(app);
        walletManager.requestConfirmation(app, UNIQUE, asset, mAsset.getCoreAsset(), address,
                false,FragmentIssueUniqueAsset.this);
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
            checkingButton.setText("Checking availability...");
        RvnWalletManager walletManager = RvnWalletManager.getInstance(getActivity());
        BRCoreWallet wallet = walletManager.getWallet();
        wallet.isAssetNameValid(walletManager.getPeerManager(), name, name.length(), this);
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

        //format the asset creation fee to the selected ISO
        BigDecimal isoUniqueAssetFee = new BigDecimal(UNIQUE_ASSET_CREATION_FEE);
        String formattedUniqueAssetFee = CurrencyUtils.getFormattedAmount(app, wallet.getIso(app), wallet.getSmallestCryptoForCrypto(app, isoUniqueAssetFee));

        setBalanceTextStyle(isoBalance, isoUniqueAssetFee.add(isoFee));

        // formattedBalance
        String formattedBalance = CurrencyUtils.getFormattedAmount(app, defaultIso, isIsoCrypto ? wallet.getSmallestCryptoForCrypto(app, isoBalance) : isoBalance);
        String balanceString = String.format(getString(R.string.Send_balance), formattedBalance);
        balanceText.setText(balanceString);
        feeText.setText(String.format(getString(R.string.Send_fee), formattedFee));
        uniqueAssetFeeText.setText(String.format(getString(R.string.unique_asset_creation_fee), formattedUniqueAssetFee));
        balanceLayout.requestLayout();
    }

    private void setBalanceTextStyle(BigDecimal balance, BigDecimal fees) {
        if (balance.compareTo(fees) > 0) {
            balanceText.setTextColor(getContext().getColor(R.color.light_gray));
            feeText.setTextColor(getContext().getColor(R.color.light_gray));
            uniqueAssetFeeText.setTextColor(getContext().getColor(R.color.light_gray));
        } else {
            balanceText.setTextColor(getContext().getColor(R.color.warning_color));
            feeText.setTextColor(getContext().getColor(R.color.warning_color));
            uniqueAssetFeeText.setTextColor(getContext().getColor(R.color.warning_color));
        }
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