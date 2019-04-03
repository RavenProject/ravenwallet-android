package com.ravencoin.presenter.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.platform.addressBook.AddressBookItem;
import com.ravencoin.R;
import com.ravencoin.presenter.activities.util.BRActivity;
import com.ravencoin.presenter.customviews.BRDialogView;
import com.ravencoin.presenter.customviews.BRText;
import com.ravencoin.presenter.newTutorial.TutorialActivity;
import com.ravencoin.tools.animation.BRAnimator;
import com.ravencoin.tools.animation.BRDialog;

import java.util.ArrayList;
import java.util.List;

import static com.ravencoin.presenter.activities.ReEnterPinActivity.IS_CREATE_WALLET;

public class TermsAndConditionsActivity extends BRActivity {
    private static final String TAG = TermsAndConditionsActivity.class.getName();

    public List<AddressBookItem> addresses;
    private LinearLayout firstClauseLayout, secondClauseLayout, layoutUnderstood;
    private CheckBox cbxFirstClause, cbxSecondClause, cbxUnderstood;
    private BRText tvUnderstood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);

        firstClauseLayout = findViewById(R.id.first_clause_layout);
        secondClauseLayout = findViewById(R.id.second_clause_layout);
        layoutUnderstood = findViewById(R.id.layout_understood);
        cbxFirstClause = findViewById(R.id.cbx_first_clause);
        cbxSecondClause = findViewById(R.id.cbx_second_clause);
        cbxUnderstood = findViewById(R.id.cbx_understood_terms_of_use);
        tvUnderstood = findViewById(R.id.understood_terms_of_use_text);
        Button confirmButton = findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cbxUnderstood.isChecked()) {
                    Intent intent = new Intent(TermsAndConditionsActivity.this, SetPinActivity.class);
                    intent.putExtra("noPin", false);
                    intent.putExtra(IS_CREATE_WALLET, true);
                    startActivity(intent);
                }
            }
        });
        cbxFirstClause.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked && cbxUnderstood.isChecked())
                    cbxUnderstood.setChecked(false);
            }
        });

        cbxSecondClause.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked && cbxUnderstood.isChecked())
                    cbxUnderstood.setChecked(false);
            }
        });

        firstClauseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cbxFirstClause.setChecked(!cbxFirstClause.isChecked());
            }
        });

        secondClauseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cbxSecondClause.setChecked(!cbxSecondClause.isChecked());
                cbxUnderstood.setEnabled(cbxFirstClause.isEnabled() && cbxSecondClause.isChecked());
            }
        });

        layoutUnderstood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCbxUnderstood();
            }
        });

        setStatusBarGradient(this);

        addresses = new ArrayList<>();
        ImageButton close = findViewById(R.id.close_button);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                onBackPressed();
            }
        });
        setStatusBarGradient(this);
    }

    private void updateCbxUnderstood() {
        cbxUnderstood.setChecked(!cbxUnderstood.isChecked() && (cbxFirstClause.isChecked() && cbxSecondClause.isChecked()));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarGradient(Activity activity) {
        Window window = activity.getWindow();
        Drawable background = ContextCompat.getDrawable(activity, R.drawable.gradient_blue_reverse);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(activity, android.R.color.transparent));
        window.setNavigationBarColor(ContextCompat.getColor(activity, android.R.color.transparent));

        final int lFlags = window.getDecorView().getSystemUiVisibility();
        // update the SystemUiVisibility depending on whether we want a Light or Dark theme.
        window.getDecorView().setSystemUiVisibility((lFlags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));

        window.setBackgroundDrawable(background);
    }

    @Override
    public void onBackPressed() {
        int c = getFragmentManager().getBackStackEntryCount();
        if (c > 0) {
            super.onBackPressed();
            return;
        }
        Intent intent = new Intent(this, TutorialActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_up, R.anim.exit_to_bottom);
        if (!isDestroyed()) {
            finish();
        }
    }
}
