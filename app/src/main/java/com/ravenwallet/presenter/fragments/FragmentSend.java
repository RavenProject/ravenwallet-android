package com.ravenwallet.presenter.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import com.google.android.material.textfield.TextInputLayout;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.platform.addressBook.AddressBookItem;
import com.platform.addressBook.AddressBookRepository;
import com.platform.assets.filters.AddressLabelFilter;
import com.platform.assets.filters.DecimalDigitsInputFilter;
import com.platform.assets.filters.InputFilterMinMax;
import com.platform.assets.filters.QuantityTextWatcher;
import com.ravenwallet.R;
import com.ravenwallet.core.BRCoreAddress;
import com.ravenwallet.core.BRCoreTransaction;
import com.ravenwallet.presenter.activities.AddressBookActivity;
import com.ravenwallet.presenter.customviews.BRButton;
import com.ravenwallet.presenter.customviews.BRDialogView;
import com.ravenwallet.presenter.customviews.BRLinearLayoutWithCaret;
import com.ravenwallet.presenter.customviews.BRText;
import com.ravenwallet.presenter.customviews.ContactButton;
import com.ravenwallet.presenter.customviews.InputTextWatcher;
import com.ravenwallet.presenter.customviews.PasteButton;
import com.ravenwallet.presenter.customviews.ScanButton;
import com.ravenwallet.presenter.entities.CryptoRequest;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.animation.BRDialog;
import com.ravenwallet.tools.animation.SlideDetector;
import com.ravenwallet.tools.animation.SpringAnimator;
import com.ravenwallet.tools.manager.BRReportsManager;
import com.ravenwallet.tools.manager.BRSharedPrefs;
import com.ravenwallet.tools.manager.SendManager;
import com.ravenwallet.tools.util.BRConstants;
import com.ravenwallet.tools.util.CurrencyUtils;
import com.ravenwallet.tools.util.Utils;
import com.ravenwallet.wallet.RvnWalletManager;
import com.ravenwallet.wallet.WalletsMaster;
import com.ravenwallet.wallet.abstracts.BaseWalletManager;

import java.math.BigDecimal;

//import static com.platform.HTTPServer.URL_SUPPORT;
import static com.ravenwallet.presenter.activities.AddressBookActivity.PICK_ADDRESS_VIEW_EXTRAS_KEY;
import static com.ravenwallet.tools.util.BRConstants.MAX_ADDRESS_NAME_LENGTH;
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

public class FragmentSend extends BaseAddressValidation {
    private static final String TAG = FragmentSend.class.getName();
    public ScrollView backgroundLayout;
    public LinearLayout signalLayout;
    //    private BRKeyboard keyboard;
    private ScanButton scan;
    private PasteButton paste;
    private Button send;
    //    private StringBuilder amountBuilder;
    private TextView isoText;
    private EditText amountEdit;
    private TextView balanceText;
    private TextView feeText;
    private ImageView edit;
    private long curBalance;
    private String selectedIso;
    private Button isoButton;
    //    private int keyboardIndex;
//    private LinearLayout keyboardLayout;
    private ImageButton close;
    private ConstraintLayout amountLayout;
    private BRButton regular;
    private BRButton economy;
    private BRLinearLayoutWithCaret feeLayout;
    private boolean feeButtonsShown = false;
    private BRText feeDescription;
    private BRText warningText;
    private boolean amountLabelOn = true;
    private ContactButton importContactButton;
    private TextInputLayout addressInputLayout;

    private static String savedIso;
//    private static String savedAmount;

    private boolean ignoreCleanup;

    private BRText addressLabel;
    private CheckBox addAddressCheckBox;
    private QuantityTextWatcher mQuantityTextWatcher;
    private final static String OPEN_FROM_ADDRESS_BOOK_EXTRA_KEY = "open.from.address.book.extra.key";
    private final static String ADDRESS_BOOK_EXTRA_KEY = "address.book.extra.key";

    private boolean isFromAddressBook;

    public static final String URL_SUPPORT = "https://ravencoin.org/mobilewallet/support";

    public static FragmentSend newInstance(boolean isFromAddressBook, AddressBookItem address) {
        FragmentSend fragment = new FragmentSend();

        Bundle args = new Bundle();
        args.putBoolean(OPEN_FROM_ADDRESS_BOOK_EXTRA_KEY, isFromAddressBook);
        args.putParcelable(ADDRESS_BOOK_EXTRA_KEY, address);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_send, container, false);
        backgroundLayout = rootView.findViewById(R.id.background_layout);
        signalLayout = rootView.findViewById(R.id.signal_layout);
        addressInputLayout = rootView.findViewById(R.id.address_input_layout);
        isoText = rootView.findViewById(R.id.iso_text);
        addressEditText = rootView.findViewById(R.id.address_edit);
        scan = rootView.findViewById(R.id.scan_button);
        paste = rootView.findViewById(R.id.paste_button);
        send = rootView.findViewById(R.id.send_button);
        amountEdit = rootView.findViewById(R.id.amount_edit);
        balanceText = rootView.findViewById(R.id.balance_text);
        feeText = rootView.findViewById(R.id.fee_text);
        edit = rootView.findViewById(R.id.edit);
        isoButton = rootView.findViewById(R.id.iso_button);
//        keyboardLayout = rootView.findViewById(R.id.keyboard_layout);
        amountLayout = rootView.findViewById(R.id.amount_layout);
        feeLayout = rootView.findViewById(R.id.fee_buttons_layout);
        feeDescription = rootView.findViewById(R.id.fee_description);
        warningText = rootView.findViewById(R.id.warning_text);
        importContactButton = rootView.findViewById(R.id.import_contact);

        isFromAddressBook = getArguments().getBoolean(OPEN_FROM_ADDRESS_BOOK_EXTRA_KEY, false);
        if (isFromAddressBook) {
            // Hiding the Address Book layout section
            rootView.findViewById(R.id.address_book_layout).setVisibility(View.GONE);
            rootView.findViewById(R.id.separator5).setVisibility(View.GONE);

            // Hiding the scan, paste and import contact buttons
            scan.setVisibility(View.GONE);
            paste.setVisibility(View.GONE);
            importContactButton.setVisibility(View.GONE);

            // Get the Address then set it accordingly
            AddressBookItem address = getArguments().getParcelable(ADDRESS_BOOK_EXTRA_KEY);
            if (address != null && address.getAddress() != null) {
                addressEditText.setText(address.getAddress());
                addressEditText.setEnabled(false);
            }
        } else {
            addressLabel = rootView.findViewById(R.id.add_address_label);
            addAddressCheckBox = rootView.findViewById(R.id.add_address_checkbox);
            addAddressCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        createDialogForSettingAddressLabel(getActivity());
                    } else {
                        addressLabel.setText(getString(R.string.add_to_address_book));
                    }
                }
            });
        }

        regular = rootView.findViewById(R.id.left_button);
        economy = rootView.findViewById(R.id.right_button);
        close = rootView.findViewById(R.id.close_button);
        BaseWalletManager wm = WalletsMaster.getInstance(getActivity()).getCurrentWallet(getActivity());
        selectedIso = BRSharedPrefs.isCryptoPreferred(getActivity()) ? wm.getIso(getActivity()) : BRSharedPrefs.getPreferredFiatIso(getContext());

        setListeners();
        isoText.setText(getString(R.string.Send_amountLabel));
        isoText.setTextSize(18);
        isoText.setTextColor(getContext().getColor(R.color.light_gray));
        isoText.requestLayout();
        signalLayout.setOnTouchListener(new SlideDetector(getContext(), signalLayout));
        mQuantityTextWatcher = new QuantityTextWatcher(amountEdit);
        signalLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });


        showFeeSelectionButtons(feeButtonsShown);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feeButtonsShown = !feeButtonsShown;
                showFeeSelectionButtons(feeButtonsShown);
            }
        });
//        keyboardIndex = signalLayout.indexOfChild(keyboardLayout);

        ImageButton faq = rootView.findViewById(R.id.faq_button);

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                Activity app = getActivity();
                if (app == null) {
                    Log.e(TAG, "onClick: app is null, can't start the webview with url: " + URL_SUPPORT);
                    return;
                }
                BRAnimator.showSupportFragment(app, BRConstants.send);
            }
        });

        amountEdit.addTextChangedListener(mQuantityTextWatcher);
        amountEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateText();
            }
        });
        double maxAmount = RvnWalletManager.getInstance(getActivity()).getMaxAmount(getActivity()).doubleValue();
        int maxDecimal = CurrencyUtils.getMaxDecimalPlaces(getActivity(), "RVN");
        amountEdit.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(maxDecimal),
                new InputFilterMinMax(0, maxAmount)});
        setButton(true);

        signalLayout.setLayoutTransition(BRAnimator.getDefaultTransition());
        isTransfer = true;
        return rootView;
    }

    private void createDialogForSettingAddressLabel(Context context) {
        final View addressLabelView = LayoutInflater.from(context).inflate(R.layout.dialog_add_address_label, null);
        final EditText addressLabelEditText = addressLabelView.findViewById(R.id.address_label);
        addressLabelEditText.setFilters(new InputFilter[]{new AddressLabelFilter(), new InputFilter.LengthFilter(MAX_ADDRESS_NAME_LENGTH)});
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Add Address Label")
                .setView(addressLabelView)
                .setCancelable(false)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String text = addressLabelEditText.getText().toString().trim();
                        if (!text.isEmpty()) {
                            addressLabel.setText(text);
                        } else {
                            addAddressCheckBox.setChecked(false);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        addAddressCheckBox.setChecked(false);
                    }
                })
                .create();
        dialog.show();
    }

    private void setListeners() {

        addressEditText.addTextChangedListener(new InputTextWatcher(addressInputLayout));

        amountEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                onFirstFocus();
            }
        });

        paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postAddressPasted(addressEditText);
            }
        });

        isoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedIso.equalsIgnoreCase(BRSharedPrefs.getPreferredFiatIso(getContext()))) {
                    Activity app = getActivity();
                    selectedIso = WalletsMaster.getInstance(app).getCurrentWallet(app).getIso(app);
                } else {
                    selectedIso = BRSharedPrefs.getPreferredFiatIso(getContext());
                }
                updateText();

            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
//                saveMetaData();
                BRAnimator.openAddressScanner(getActivity(), BRConstants.SCANNER_REQUEST);

            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //not allowed now
                if (!BRAnimator.isClickAllowed()) return;
                WalletsMaster master = WalletsMaster.getInstance(getActivity());
                BaseWalletManager wallet = master.getCurrentWallet(getActivity());
                //get the current wallet used
                if (wallet == null) {
                    Log.e(TAG, "onClick: Wallet is null and it can't happen.");
                    BRReportsManager.reportBug(new NullPointerException("Wallet is null and it can't happen."), true);
                    return;
                }
                boolean allFilled = true;
                String rawAddress = addressEditText.getText().toString();
                //inserted amount
                BigDecimal rawAmount = new BigDecimal(mQuantityTextWatcher.getValue());
                //is the chosen ISO a crypto (could be a fiat currency)
                boolean isIsoCrypto = master.isIsoCrypto(getActivity(), selectedIso);

                BigDecimal cryptoAmount = isIsoCrypto ? wallet.getSmallestCryptoForCrypto(getActivity(), rawAmount) : wallet.getSmallestCryptoForFiat(getActivity(), rawAmount);

                // Checking the Address validity
                if (!isAddressValid(rawAddress)) {
                    addressInputLayout.setError(getString(R.string.Send_invalidAddressMessage));
                    return;
                }

                BRCoreAddress address = new BRCoreAddress(req.address);
                Activity app = getActivity();
                if (!address.isValid()) {
                    allFilled = false;

                    BRDialog.showCustomDialog(app, app.getString(R.string.Alert_error), app.getString(R.string.Send_noAddress), app.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            brDialogView.dismissWithAnimation();
                        }
                    }, null, null, 0);
                    return;
                }
                if (cryptoAmount.doubleValue() <= 0) {
                    allFilled = false;
                    SpringAnimator.failShakeAnimation(getActivity(), amountEdit);
                    return;
                }
                if (cryptoAmount.longValue() > wallet.getCachedBalance(getActivity())) {
                    allFilled = false;
                    SpringAnimator.failShakeAnimation(getActivity(), balanceText);
                    SpringAnimator.failShakeAnimation(getActivity(), feeText);
                }
//                Log.e(TAG, "before createTransaction: smallestCryptoAmount.longValue: " + cryptoAmount.longValue() + ", addrs: " + address.stringify());
                BRCoreTransaction tx = wallet.getWallet().createTransaction(cryptoAmount.longValue(), address);
//                if (tx == null) {
//                    BRDialog.showCustomDialog(app, app.getString(R.string.Alert_error), app.getString(R.string.Send_creatTransactionError), app.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
//                        @Override
//                        public void onClick(BRDialogView brDialogView) {
//                            brDialogView.dismissWithAnimation();
//                        }
//                    }, null, null, 0);
//                    return;
//                }

                if (allFilled) {
                    CryptoRequest item = new CryptoRequest(tx, null, false, "", req.address, cryptoAmount);
                    SendManager.sendTransaction(getActivity(), item, wallet);

                    // Check if Address is bookmarked to save it
                    if (!isFromAddressBook && addAddressCheckBox.isChecked()) {
                        AddressBookRepository repository = AddressBookRepository.getInstance(getActivity());
                        repository.insertAddress(new AddressBookItem(addressLabel.getText().toString(), rawAddress));
                    }
                }
            }
        });

        backgroundLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                getActivity().onBackPressed();
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity app = getActivity();
                if (app != null)
                    app.getFragmentManager().popBackStack();
            }
        });


//        addressEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
//                    Utils.hideKeyboard(getActivity());
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            showKeyboard(true);
//                        }
//                    }, 500);
//
//                }
//                return false;
//            }
//        });
//
//        keyboard.addOnInsertListener(new BRKeyboard.OnInsertListener() {
//            @Override
//            public void onClick(String key) {
//                handleClick(key);
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

    private void onFirstFocus() {
        if (amountLabelOn) { //only first time
            amountLabelOn = false;
            amountEdit.setHint("0");
            amountEdit.setTextSize(24);
            balanceText.setVisibility(View.VISIBLE);
            feeText.setVisibility(View.VISIBLE);
            edit.setVisibility(View.VISIBLE);
            isoText.setTextColor(getContext().getColor(R.color.almost_black));
            isoText.setText(CurrencyUtils.getSymbolByIso(getActivity(), selectedIso));
            isoText.setTextSize(28);
            final float scaleX = amountEdit.getScaleX();
            amountEdit.setScaleX(0);

            AutoTransition tr = new AutoTransition();
            tr.setInterpolator(new OvershootInterpolator());
            tr.addListener(new androidx.transition.Transition.TransitionListener() {
                @Override
                public void onTransitionStart(@NonNull androidx.transition.Transition transition) {

                }

                @Override
                public void onTransitionEnd(@NonNull androidx.transition.Transition transition) {
                    amountEdit.animate().setDuration(100).scaleX(scaleX);
                    amountEdit.requestFocus();
                }

                @Override
                public void onTransitionCancel(@NonNull androidx.transition.Transition transition) {

                }

                @Override
                public void onTransitionPause(@NonNull androidx.transition.Transition transition) {

                }

                @Override
                public void onTransitionResume(@NonNull androidx.transition.Transition transition) {

                }
            });

            ConstraintSet set = new ConstraintSet();
            set.clone(amountLayout);
            TransitionManager.beginDelayedTransition(amountLayout, tr);

            int px4 = Utils.getPixelsFromDps(getContext(), 4);
//                    int px8 = Utils.getPixelsFromDps(getContext(), 8);
            set.connect(balanceText.getId(), ConstraintSet.TOP, isoText.getId(), ConstraintSet.BOTTOM, px4);
            set.connect(feeText.getId(), ConstraintSet.TOP, balanceText.getId(), ConstraintSet.BOTTOM, px4);
            set.connect(feeText.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, px4);
            set.connect(isoText.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, px4);
            set.connect(isoText.getId(), ConstraintSet.BOTTOM, -1, ConstraintSet.TOP, -1);
            set.applyTo(amountLayout);
        }
    }

//    private void showKeyboard(boolean b) {
//        int curIndex = keyboardIndex;
//
//        if (!b) {
//            signalLayout.removeView(keyboardLayout);
//
//        } else {
//            Utils.hideKeyboard(getActivity());
//            if (signalLayout.indexOfChild(keyboardLayout) == -1)
//                signalLayout.addView(keyboardLayout, curIndex);
//            else
//                signalLayout.removeView(keyboardLayout);
//
//        }
//    }

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
    public void onStop() {
        super.onStop();
        if (isRemoving()) {
            BRAnimator.animateBackgroundDim(backgroundLayout, true);
            BRAnimator.animateSignalSlide(signalLayout, true, new BRAnimator.OnSlideAnimationEnd() {
                @Override
                public void onAnimationEnd() {
                    if (getActivity() != null) {
                        try {
                            getActivity().getFragmentManager().popBackStack();
                        } catch (Exception ignored) {

                        }
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        loadMetaData();
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.hideKeyboard(getActivity());
        if (!ignoreCleanup) {
            savedIso = null;
//            savedAmount = null;
        }
    }

//    private void handleClick(String key) {
//        if (key == null) {
//            Log.e(TAG, "handleClick: key is null! ");
//            return;
//        }
//
//        if (key.isEmpty()) {
//            handleDeleteClick();
//        } else if (Character.isDigit(key.charAt(0))) {
//            handleDigitClick(Integer.parseInt(key.substring(0, 1)));
//        } else if (key.charAt(0) == '.') {
//            handleSeparatorClick();
//        }
//    }

//    private void handleDigitClick(Integer dig) {
//        String currAmount = amountBuilder.toString();
//        String iso = selectedIso;
//        WalletsMaster master = WalletsMaster.getInstance(getActivity());
//        if (new BigDecimal(currAmount.concat(String.valueOf(dig))).doubleValue()
//                <= master.getCurrentWallet(getActivity()).getMaxAmount(getActivity()).doubleValue()) {
//            //do not insert 0 if the balance is 0 now
//            if (currAmount.equalsIgnoreCase("0")) amountBuilder = new StringBuilder("");
//            if ((currAmount.contains(".") && (currAmount.length() - currAmount.indexOf(".") > CurrencyUtils.getMaxDecimalPlaces(getActivity(), iso))))
//                return;
//            amountBuilder.append(dig);
//            updateText();
//        }
//    }
//
//    private void handleSeparatorClick() {
//        String currAmount = amountBuilder.toString();
//        if (currAmount.contains(".") || CurrencyUtils.getMaxDecimalPlaces(getActivity(), selectedIso) == 0)
//            return;
//        amountBuilder.append(".");
//        updateText();
//    }
//
//    private void handleDeleteClick() {
//        String currAmount = amountBuilder.toString();
//        if (currAmount.length() > 0) {
//            amountBuilder.deleteCharAt(currAmount.length() - 1);
//            updateText();
//        }

//    }

    private void updateText() {
        Activity app = getActivity();
        if (app == null) return;

//        String stringAmount = amountBuilder.toString();
//        setAmount();
        BaseWalletManager wallet = WalletsMaster.getInstance(app).getCurrentWallet(app);
        String balanceString;
        if (selectedIso == null)
            selectedIso = wallet.getIso(app);
        //String iso = selectedIso;
        curBalance = wallet.getCachedBalance(app);
        if (!amountLabelOn)
            isoText.setText(CurrencyUtils.getSymbolByIso(app, selectedIso));
        isoButton.setText(String.format("%s(%s)", selectedIso, CurrencyUtils.getSymbolByIso(app, selectedIso)));

        //is the chosen ISO a crypto (could be also a fiat currency)
        boolean isIsoCrypto = WalletsMaster.getInstance(getActivity()).isIsoCrypto(getActivity(), selectedIso);

        BigDecimal inputAmount = new BigDecimal(mQuantityTextWatcher.getValue());

        //smallest crypto e.g. satoshis
        BigDecimal cryptoAmount = isIsoCrypto ? wallet.getSmallestCryptoForCrypto(app, inputAmount) : wallet.getSmallestCryptoForFiat(app, inputAmount);

        //wallet's balance for the selected ISO
        BigDecimal isoBalance = isIsoCrypto ? wallet.getCryptoForSmallestCrypto(app, new BigDecimal(curBalance)) : wallet.getFiatForSmallestCrypto(app, new BigDecimal(curBalance), null);
        if (isoBalance == null) isoBalance = new BigDecimal(0);

        long fee;
        if (cryptoAmount.longValue() <= 0) {
            fee = 0;
        } else {
            String addrString = addressEditText.getText().toString();
            BRCoreAddress coreAddress = null;
            if (!Utils.isNullOrEmpty(addrString)) {
                coreAddress = new BRCoreAddress(addrString);
            }
            BRCoreTransaction tx = null;
            if (coreAddress != null && coreAddress.isValid()) {
                tx = wallet.getWallet().createTransaction(cryptoAmount.longValue(), coreAddress);
            }

            if (tx == null) {
                fee = wallet.getWallet().getFeeForTransactionAmount(cryptoAmount.longValue());
            } else {
                fee = wallet.getWallet().getTransactionFee(tx);
                if (fee <= 0)
                    fee = wallet.getWallet().getFeeForTransactionAmount(cryptoAmount.longValue());
            }
        }

        //get the fee for iso (dollars, bits, BTC..)
        BigDecimal isoFee = isIsoCrypto ? wallet.getCryptoForSmallestCrypto(app, new BigDecimal(fee)) : wallet.getFiatForSmallestCrypto(app, new BigDecimal(fee), null);

        //format the fee to the selected ISO
        String formattedFee = CurrencyUtils.getFormattedAmount(app, selectedIso, isIsoCrypto ? wallet.getSmallestCryptoForCrypto(app, isoFee) : isoFee);
//        Log.e(TAG, "updateText: aproxFee:" + aproxFee);

        boolean isOverTheBalance = inputAmount.doubleValue() > isoBalance.doubleValue();
        if (isOverTheBalance) {
            balanceText.setTextColor(getContext().getColor(R.color.warning_color));
            feeText.setTextColor(getContext().getColor(R.color.warning_color));
            amountEdit.setTextColor(getContext().getColor(R.color.warning_color));
            if (!amountLabelOn)
                isoText.setTextColor(getContext().getColor(R.color.warning_color));
        } else {
            balanceText.setTextColor(getContext().getColor(R.color.light_gray));
            feeText.setTextColor(getContext().getColor(R.color.light_gray));
            amountEdit.setTextColor(getContext().getColor(R.color.almost_black));
            if (!amountLabelOn)
                isoText.setTextColor(getContext().getColor(R.color.almost_black));
        }
        //formattedBalance
        String formattedBalance = CurrencyUtils.getFormattedAmount(app, selectedIso, isIsoCrypto ? wallet.getSmallestCryptoForCrypto(app, isoBalance) : isoBalance);
        balanceString = String.format(getString(R.string.Send_balance), formattedBalance);
        balanceText.setText(balanceString);
        feeText.setText(String.format(getString(R.string.Send_fee), formattedFee));
        amountLayout.requestLayout();
    }

    public void setCryptoObject(final CryptoRequest obj) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (obj == null) {
                    Log.e(TAG, "setCryptoObject: obj is null");
                    return;
                }
                Activity app = getActivity();
                if (app == null) {
                    Log.e(TAG, "setCryptoObject: app is null");
                    return;
                }
                BaseWalletManager wm = WalletsMaster.getInstance(app).getCurrentWallet(app);
                if (obj.address != null && addressEditText != null) {
                    addressEditText.setText(wm.decorateAddress(getActivity(), obj.address.trim()));
                }

                if (obj.amount != null) {
                    BigDecimal satoshiAmount = obj.amount.multiply(new BigDecimal(SATOSHIS));
//                    amountBuilder = new StringBuilder(wm.getFiatForSmallestCrypto(getActivity(), satoshiAmount, null).toPlainString());
                    amountEdit.setText(wm.getFiatForSmallestCrypto(getActivity(), satoshiAmount, null).toPlainString());
                    updateText();
                }
            }
        }, 1000);

    }

    private void showFeeSelectionButtons(boolean b) {
        if (!b) {
            signalLayout.removeView(feeLayout);
        } else {
            signalLayout.addView(feeLayout, signalLayout.indexOfChild(amountLayout) + 1);

        }
    }

//    private void setAmount() {
//        String tmpAmount = amountBuilder.toString();
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
//        amountEdit.setText(newAmount.toString());
//    }

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
        updateText();
    }


//    private void saveMetaData() {
//            savedAmount = String.valueOf(mQuantityTextWatcher.getValue());
//        savedIso = selectedIso;
//        ignoreCleanup = true;
//    }
//
//    private void loadMetaData() {
//        ignoreCleanup = false;
//        if (!Utils.isNullOrEmpty(savedIso))
//            selectedIso = savedIso;
//        if (!Utils.isNullOrEmpty(savedAmount)) {
////            amountBuilder = new StringBuilder(savedAmount);
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    amountEdit.performClick();
//                    updateText();
//                }
//            }, 500);
//
//        }
//    }

}