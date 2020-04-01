package com.ravenwallet.presenter.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.TransitionManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ravenwallet.R;
import com.ravenwallet.presenter.activities.util.BRActivity;
import com.ravenwallet.presenter.customviews.BRDialogView;
import com.ravenwallet.presenter.interfaces.BROnSignalCompletion;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.animation.BRDialog;
import com.ravenwallet.tools.animation.SpringAnimator;
import com.ravenwallet.tools.manager.BRReportsManager;
import com.ravenwallet.tools.manager.BRSharedPrefs;
import com.ravenwallet.tools.security.SmartValidator;
import com.ravenwallet.tools.util.Bip39Reader;
import com.ravenwallet.tools.util.Utils;

import java.util.Locale;
import java.util.Random;


public class PaperKeyProveActivity extends BRActivity {
    private static final String TAG = PaperKeyProveActivity.class.getName();
    private Button submit;
    private EditText wordEditFirst;
    private EditText wordEditSecond;
    private TextView wordTextFirst;
    private TextView wordTextSecond;
    private ImageView checkMark1;
    private ImageView checkMark2;
    private SparseArray<String> sparseArrayWords = new SparseArray<>();
    public static boolean appVisible = false;
    private static PaperKeyProveActivity app;
    private ConstraintLayout constraintLayout;
    private ConstraintSet applyConstraintSet = new ConstraintSet();
    private ConstraintSet resetConstraintSet = new ConstraintSet();

    public static PaperKeyProveActivity getApp() {
        return app;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_key_prove);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        submit = findViewById(R.id.button_submit);
        wordEditFirst = findViewById(R.id.word_edittext_first);
        wordEditSecond = findViewById(R.id.word_edittext_second);
        wordTextFirst = findViewById(R.id.word_number_first);
        wordTextSecond = findViewById(R.id.word_number_second);

        checkMark1 = findViewById(R.id.check_mark_1);
        checkMark2 = findViewById(R.id.check_mark_2);

//        wordEditFirst.setOnFocusChangeListener(new FocusListener());
//        wordEditSecond.setOnFocusChangeListener(new FocusListener());

        wordEditFirst.addTextChangedListener(new BRTextWatcher());
        wordEditSecond.addTextChangedListener(new BRTextWatcher());

        constraintLayout = findViewById(R.id.constraintLayout);
        resetConstraintSet.clone(constraintLayout);
        applyConstraintSet.clone(constraintLayout);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                TransitionManager.beginDelayedTransition(constraintLayout);
                applyConstraintSet.setMargin(R.id.word_number_first, ConstraintSet.TOP, 8);
                applyConstraintSet.setMargin(R.id.line1, ConstraintSet.TOP, 16);
                applyConstraintSet.setMargin(R.id.line2, ConstraintSet.TOP, 16);
                applyConstraintSet.setMargin(R.id.word_number_second, ConstraintSet.TOP, 8);
                applyConstraintSet.applyTo(constraintLayout);


            }
        }, 500);


        wordEditSecond.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.submit || id == EditorInfo.IME_NULL) {
                    submit.performClick();
                    return true;
                }
                return false;
            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;

                if (isWordCorrect(true) && isWordCorrect(false)) {
                    Utils.hideKeyboard(PaperKeyProveActivity.this);
                    BRSharedPrefs.putPhraseWroteDown(PaperKeyProveActivity.this, true);
                    BRAnimator.showRvnSignal(PaperKeyProveActivity.this, getString(R.string.Alerts_paperKeySet), getString(R.string.Alerts_paperKeySetSubheader), R.drawable.ic_check_mark_white, new BROnSignalCompletion() {
                        @Override
                        public void onComplete() {
                            Intent intent = new Intent(PaperKeyProveActivity.this, HomeActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
                            finishAffinity();
                        }
                    });
                } else {

                    if (!isWordCorrect(true)) {
                        wordEditFirst.setTextColor(getColor(R.color.red_text));
                        SpringAnimator.failShakeAnimation(PaperKeyProveActivity.this, wordEditFirst);
                    }

                    if (!isWordCorrect(false)) {
                        wordEditSecond.setTextColor(getColor(R.color.red_text));
                        SpringAnimator.failShakeAnimation(PaperKeyProveActivity.this, wordEditSecond);
                    }
                }

            }
        });
        String cleanPhrase = null;

        cleanPhrase = getIntent().getExtras() == null ? null : getIntent().getStringExtra("phrase");

        if (Utils.isNullOrEmpty(cleanPhrase)) {
            throw new RuntimeException(TAG + ": cleanPhrase is null");
        }

        String wordArray[] = cleanPhrase.split(" ");

        if (wordArray.length == 12 && cleanPhrase.charAt(cleanPhrase.length() - 1) == '\0') {
            BRDialog.showCustomDialog(this, getString(R.string.JailbreakWarnings_title),
                    getString(R.string.Alert_keystore_generic_android), getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            brDialogView.dismissWithAnimation();
                        }
                    }, null, null, 0);
            BRReportsManager.reportBug(new IllegalArgumentException("Paper Key error, please contact support at breadwallet.com"), false);
        } else {
            randomWordsSetUp(wordArray);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarGradiant(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            Drawable background = activity.getResources().getDrawable(R.drawable.regular_blue);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
            window.setNavigationBarColor(activity.getResources().getColor(android.R.color.transparent));

            final int lFlags = window.getDecorView().getSystemUiVisibility();
            // update the SystemUiVisibility depending on whether we want a Light or Dark theme.
            window.getDecorView().setSystemUiVisibility((lFlags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));

            window.setBackgroundDrawable(background);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        app = this;

        setStatusBarGradiant(app);
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

    private void randomWordsSetUp(String[] words) {
        final Random random = new Random();
        int n = random.nextInt(10) + 1;

        sparseArrayWords.append(n, words[n]);

        while (sparseArrayWords.get(n) != null) {
            n = random.nextInt(10) + 1;
        }

        sparseArrayWords.append(n, words[n]);

        wordTextFirst.setText(String.format(Locale.getDefault(), getString(R.string.ConfirmPaperPhrase_word), (sparseArrayWords.keyAt(0) + 1)));
        wordTextSecond.setText(String.format(Locale.getDefault(), getString(R.string.ConfirmPaperPhrase_word), (sparseArrayWords.keyAt(1) + 1)));

    }

    private boolean isWordCorrect(boolean first) {
        if (first) {
            String edit = Bip39Reader.cleanWord(wordEditFirst.getText().toString());
            return SmartValidator.isWordValid(PaperKeyProveActivity.this, edit) && edit.equalsIgnoreCase(sparseArrayWords.get(sparseArrayWords.keyAt(0)));
        } else {
            String edit = Bip39Reader.cleanWord(wordEditSecond.getText().toString());
            return SmartValidator.isWordValid(PaperKeyProveActivity.this, edit) && edit.equalsIgnoreCase(sparseArrayWords.get(sparseArrayWords.keyAt(1)));
        }
    }

//    private class FocusListener implements View.OnFocusChangeListener {
//
//        @Override
//        public void onFocusChange(View v, boolean hasFocus) {
//            if (!hasFocus) {
//                validateWord((EditText) v);
//            } else {
//                ((EditText) v).setTextColor(getColor(R.color.light_gray));
//            }
//        }
//    }

    private void validateWord(EditText view) {
        String word = view.getText().toString();
        boolean valid = SmartValidator.isWordValid(this, word);
        view.setTextColor(getColor(valid ? R.color.light_gray : R.color.red_text));
//        if (!valid)
//            SpringAnimator.failShakeAnimation(this, view);
        if (isWordCorrect(true)) {
            checkMark1.setVisibility(View.VISIBLE);
        } else {
            checkMark1.setVisibility(View.INVISIBLE);
        }

        if (isWordCorrect(false)) {
            checkMark2.setVisibility(View.VISIBLE);
        } else {
            checkMark2.setVisibility(View.INVISIBLE);
        }
    }

    private class BRTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            validateWord(wordEditFirst);
            validateWord(wordEditSecond);

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }


}
