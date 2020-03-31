package com.ravenwallet.presenter.activities.settings;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.ravenwallet.R;
import com.ravenwallet.presenter.activities.InputWordsActivity;
import com.ravenwallet.presenter.activities.util.BRActivity;
import com.ravenwallet.presenter.customviews.BRDialogView;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.animation.BRDialog;
import com.ravenwallet.tools.threads.Import12WordsListener;
import com.ravenwallet.tools.threads.Import12WordsTask;
import com.ravenwallet.tools.util.BRConstants;


public class ImportActivity extends BRActivity implements Import12WordsListener {
    private Button scan, sweep;
    private static final String TAG = ImportActivity.class.getName();
    public static final String BIP32 = "bip32", BIP44 = "bip44";
    private ImageButton close;
    private static final int REQUEST_WORDS = 33;


    private Import12WordsTask mImport12WordsTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        scan = findViewById(R.id.scan_button);
        sweep = findViewById(R.id.sweep_button);
        close = findViewById(R.id.close_button);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ImageButton faq = findViewById(R.id.faq_button);

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                BRAnimator.showSupportFragment(ImportActivity.this, BRConstants.importWallet);
            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                BRAnimator.openAddressScanner(ImportActivity.this, BRConstants.SCANNER_REQUEST);
            }
        });

        sweep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                Intent intent = new Intent(ImportActivity.this, InputWordsActivity.class);
                intent.putExtra("sweepWords", true);
                startActivityForResult(intent, REQUEST_WORDS);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mImport12WordsTask != null && mImport12WordsTask.getStatus() == AsyncTask.Status.RUNNING)
            mImport12WordsTask.cancel(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WORDS && resultCode == RESULT_OK && data != null && data.hasExtra("phrase")) {
            String phrase = data.getStringExtra("phrase");
            String bip = data.getStringExtra("bip");
            if (TextUtils.isEmpty(phrase)) return;
            if (mImport12WordsTask == null || mImport12WordsTask.getStatus() == AsyncTask.Status.FINISHED)
                mImport12WordsTask = new Import12WordsTask(ImportActivity.this,
                        phrase.getBytes(),
                        bip,
                        ImportActivity.this);
            if (mImport12WordsTask.getStatus() != AsyncTask.Status.RUNNING)
                mImport12WordsTask.execute();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case BRConstants.CAMERA_REQUEST_ID: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    BRAnimator.openAddressScanner(this, BRConstants.SCANNER_REQUEST);
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    public void error(int msgId) {
        BRDialog.showCustomDialog(ImportActivity.this, getString(R.string.JailbreakWarnings_title),
                getString(msgId), getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismissWithAnimation();
                    }
                }, null, null, 0);
    }

    @Override
    public void error(String msg) {
        BRDialog.showCustomDialog(ImportActivity.this, getString(R.string.JailbreakWarnings_title),
                msg, getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismissWithAnimation();
                    }
                }, null, null, 0);
    }
}
