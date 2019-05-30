package com.ravenwallet.presenter.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ravenwallet.R;
import com.ravenwallet.presenter.activities.intro.IntroActivity;
import com.ravenwallet.presenter.activities.settings.ImportActivity;
import com.ravenwallet.presenter.activities.util.BRActivity;
import com.ravenwallet.presenter.customviews.BRDialogView;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.animation.BRDialog;
import com.ravenwallet.tools.animation.SpringAnimator;
import com.ravenwallet.tools.manager.BRReportsManager;
import com.ravenwallet.tools.manager.BRSharedPrefs;
import com.ravenwallet.tools.security.AuthManager;
import com.ravenwallet.tools.security.PostAuth;
import com.ravenwallet.tools.security.SmartValidator;
import com.ravenwallet.tools.util.BRConstants;
import com.ravenwallet.tools.util.Utils;
import com.ravenwallet.wallet.WalletsMaster;

public class InputWordsActivity extends BRActivity {
    private static final String TAG = InputWordsActivity.class.getName();
    private Button nextButton;

    private EditText word1;
    private EditText word2;
    private EditText word3;
    private EditText word4;
    private EditText word5;
    private EditText word6;
    private EditText word7;
    private EditText word8;
    private EditText word9;
    private EditText word10;
    private EditText word11;
    private EditText word12;
    private String debugPhrase;

    private TextView title;
    private TextView description;
    private LinearLayout layoutBips;
    private RadioGroup rgBips;
    public static boolean appVisible = false;
    private static InputWordsActivity app;

    public static InputWordsActivity getApp() {
        return app;
    }

    //will be true if this screen was called from the restore screen
    private boolean restore = false;
    private boolean resetPin = false;
    private boolean sweepWords = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_words);

        nextButton = findViewById(R.id.send_button);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        layoutBips = findViewById(R.id.layout_rg_bips);
        rgBips = findViewById(R.id.rg_bips);

        word1 = (EditText) findViewById(R.id.word1);
        word2 = (EditText) findViewById(R.id.word2);
        word3 = (EditText) findViewById(R.id.word3);
        word4 = (EditText) findViewById(R.id.word4);
        word5 = (EditText) findViewById(R.id.word5);
        word6 = (EditText) findViewById(R.id.word6);
        word7 = (EditText) findViewById(R.id.word7);
        word8 = (EditText) findViewById(R.id.word8);
        word9 = (EditText) findViewById(R.id.word9);
        word10 = (EditText) findViewById(R.id.word10);
        word11 = (EditText) findViewById(R.id.word11);
        word12 = (EditText) findViewById(R.id.word12);
        ImageButton faq = (ImageButton) findViewById(R.id.faq_button);

        if (Utils.isUsingCustomInputMethod(this)) {
            BRDialog.showCustomDialog(this, getString(R.string.JailbreakWarnings_title), getString(R.string.Alert_customKeyboard_android),
                    getString(R.string.Button_ok), getString(R.string.JailbreakWarnings_close), new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
                            imeManager.showInputMethodPicker();
                            brDialogView.dismissWithAnimation();
                        }
                    }, new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            brDialogView.dismissWithAnimation();
                        }
                    }, null, 0);
        }

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                BRAnimator.showSupportFragment(app, BRConstants.paperKey);
            }
        });

        FocusListener focusListener = new FocusListener();
        word1.setOnFocusChangeListener(focusListener);
        word2.setOnFocusChangeListener(focusListener);
        word3.setOnFocusChangeListener(focusListener);
        word4.setOnFocusChangeListener(focusListener);
        word5.setOnFocusChangeListener(focusListener);
        word6.setOnFocusChangeListener(focusListener);
        word7.setOnFocusChangeListener(focusListener);
        word8.setOnFocusChangeListener(focusListener);
        word9.setOnFocusChangeListener(focusListener);
        word10.setOnFocusChangeListener(focusListener);
        word11.setOnFocusChangeListener(focusListener);
        word12.setOnFocusChangeListener(focusListener);

        restore = getIntent().getExtras() != null && getIntent().getExtras().getBoolean("restore");
        resetPin = getIntent().getExtras() != null && getIntent().getExtras().getBoolean("resetPin");
        sweepWords = getIntent().getExtras() != null && getIntent().getExtras().getBoolean("sweepWords");

        if (restore) {
            //change the labels
            title.setText(getString(R.string.MenuViewController_recoverButton));
            description.setText(getString(R.string.WipeWallet_instruction));
        } else if (resetPin) {
            //change the labels
            title.setText(getString(R.string.RecoverWallet_header_reset_pin));
            description.setText(getString(R.string.RecoverWallet_subheader_reset_pin));
        } else if (sweepWords) {
            title.setText("Import UTXOS for BIP44 Migration");
            description.setText("Description for sweeping wallet");
            if (BRSharedPrefs.getExpertMode(this))
                layoutBips.setVisibility(View.VISIBLE);
        }

        word12.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    nextButton.performClick();
                }
                return false;
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                final Activity app = InputWordsActivity.this;
                String phraseToCheck = getPhrase();
                if (Utils.isEmulatorOrDebug(app) && !Utils.isNullOrEmpty(debugPhrase)) {
                    phraseToCheck = debugPhrase;
                }
                if (phraseToCheck == null) {
                    return;
                }
                String cleanPhrase = SmartValidator.cleanPaperKey(app, phraseToCheck);
                if (Utils.isNullOrEmpty(cleanPhrase)) {
                    BRReportsManager.reportBug(new NullPointerException("cleanPhrase is null or empty!"));
                    return;
                }
                if (SmartValidator.isPaperKeyValid(app, cleanPhrase)) {

                    if (restore || resetPin) {
                        if (SmartValidator.isPaperKeyCorrect(cleanPhrase, app)) {
                            Utils.hideKeyboard(app);
                            clearWords();
                            if (restore) {
                                BRDialog.showCustomDialog(InputWordsActivity.this, getString(R.string.WipeWallet_alertTitle), getString(R.string.WipeWallet_alertMessage), getString(R.string.WipeWallet_wipe), getString(R.string.Button_cancel), new BRDialogView.BROnClickListener() {
                                    @Override
                                    public void onClick(BRDialogView brDialogView) {
                                        brDialogView.dismissWithAnimation();
                                        WalletsMaster m = WalletsMaster.getInstance(InputWordsActivity.this);
                                        m.wipeWalletButKeystore(app);
                                        m.wipeKeyStore(app);
                                        Intent intent = new Intent(app, IntroActivity.class);
                                        finalizeIntent(intent);
                                    }
                                }, new BRDialogView.BROnClickListener() {
                                    @Override
                                    public void onClick(BRDialogView brDialogView) {
                                        brDialogView.dismissWithAnimation();
                                    }
                                }, null, 0);

                            } else if (resetPin) {
                                AuthManager.getInstance().setPinCode("", InputWordsActivity.this);
                                Intent intent = new Intent(app, SetPinActivity.class);
                                intent.putExtra("noPin", true);
                                finalizeIntent(intent);
                            }
                        } else {
                            BRDialog.showCustomDialog(app, "", getString(R.string.RecoverWallet_invalid), getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                                @Override
                                public void onClick(BRDialogView brDialogView) {
                                    brDialogView.dismissWithAnimation();
                                }
                            }, null, null, 0);
                        }

                    } else if (sweepWords) {
                        Intent intent = new Intent();
                        intent.putExtra("phrase", cleanPhrase);
                        boolean isExpertMode = BRSharedPrefs.getExpertMode(InputWordsActivity.this);
                        String bip;
                        if (!isExpertMode || rgBips.getCheckedRadioButtonId() == R.id.rbpBip44)
                            bip = ImportActivity.BIP44;
                        else
                            bip = ImportActivity.BIP32;

                        intent.putExtra("bip", bip);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        Utils.hideKeyboard(app);
                        WalletsMaster m = WalletsMaster.getInstance(InputWordsActivity.this);
                        m.wipeWalletButKeystore(app);
                        m.wipeKeyStore(app);
                        PostAuth.getInstance().setPhraseForKeyStore(cleanPhrase);
                        BRSharedPrefs.putAllowSpend(app, BRSharedPrefs.getCurrentWalletIso(app), false);
                        //if this screen is shown then we did not upgrade to the new app, we installed it
                        BRSharedPrefs.putGreetingsShown(app, true);
                        PostAuth.getInstance().onRecoverWalletAuth(app, false);
                    }

                } else {
                    BRDialog.showCustomDialog(app, "", getResources().getString(R.string.RecoverWallet_invalid), getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            brDialogView.dismissWithAnimation();
                        }
                    }, null, null, 0);

                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        app = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    private String getPhrase() {
        boolean success = true;

        String w1 = word1.getText().toString().toLowerCase();
        String w2 = word2.getText().toString().toLowerCase();
        String w3 = word3.getText().toString().toLowerCase();
        String w4 = word4.getText().toString().toLowerCase();
        String w5 = word5.getText().toString().toLowerCase();
        String w6 = word6.getText().toString().toLowerCase();
        String w7 = word7.getText().toString().toLowerCase();
        String w8 = word8.getText().toString().toLowerCase();
        String w9 = word9.getText().toString().toLowerCase();
        String w10 = word10.getText().toString().toLowerCase();
        String w11 = word11.getText().toString().toLowerCase();
        String w12 = word12.getText().toString().toLowerCase();

        if (Utils.isNullOrEmpty(w1)) {
            SpringAnimator.failShakeAnimation(this, word1);
            success = false;
        }
        if (Utils.isNullOrEmpty(w2)) {
            SpringAnimator.failShakeAnimation(this, word2);
            success = false;
        }
        if (Utils.isNullOrEmpty(w3)) {
            SpringAnimator.failShakeAnimation(this, word3);
            success = false;
        }
        if (Utils.isNullOrEmpty(w4)) {
            SpringAnimator.failShakeAnimation(this, word4);
            success = false;
        }
        if (Utils.isNullOrEmpty(w5)) {
            SpringAnimator.failShakeAnimation(this, word5);
            success = false;
        }
        if (Utils.isNullOrEmpty(w6)) {
            SpringAnimator.failShakeAnimation(this, word6);
            success = false;
        }
        if (Utils.isNullOrEmpty(w7)) {
            SpringAnimator.failShakeAnimation(this, word7);
            success = false;
        }
        if (Utils.isNullOrEmpty(w8)) {
            SpringAnimator.failShakeAnimation(this, word8);
            success = false;
        }
        if (Utils.isNullOrEmpty(w9)) {
            SpringAnimator.failShakeAnimation(this, word9);
            success = false;
        }
        if (Utils.isNullOrEmpty(w10)) {
            SpringAnimator.failShakeAnimation(this, word10);
            success = false;
        }
        if (Utils.isNullOrEmpty(w11)) {
            SpringAnimator.failShakeAnimation(this, word11);
            success = false;
        }
        if (Utils.isNullOrEmpty(w12)) {
            SpringAnimator.failShakeAnimation(this, word12);
            success = false;
        }

        if (!success) return null;

        return w(w1) + " " + w(w2) + " " + w(w3) + " " + w(w4) + " " + w(w5) + " " + w(w6) + " " + w(w7) + " " + w(w8) + " " + w(w9) + " " + w(w10) + " " + w(w11) + " " + w(w12);
    }

    private String w(String word) {
        return word.replaceAll(" ", "");
    }

    private void clearWords() {
        word1.setText("");
        word2.setText("");
        word3.setText("");
        word4.setText("");
        word5.setText("");
        word6.setText("");
        word7.setText("");
        word8.setText("");
        word9.setText("");
        word10.setText("");
        word11.setText("");
        word12.setText("");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    private class FocusListener implements View.OnFocusChangeListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                validateWord((EditText) v);
            } else {
                ((EditText) v).setTextColor(getColor(R.color.light_gray));
            }
        }
    }

    private void validateWord(EditText view) {
        String word = view.getText().toString();
        boolean valid = SmartValidator.isWordValid(this, word);
        view.setTextColor(getColor(valid ? R.color.light_gray : R.color.red_text));
        if (!valid)
            SpringAnimator.failShakeAnimation(this, view);
    }

}
