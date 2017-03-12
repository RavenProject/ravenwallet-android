package com.breadwallet.presenter.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.breadwallet.R;
import com.breadwallet.tools.animation.BRAnimator;
import com.breadwallet.tools.security.PostAuthenticationProcessor;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.wallet.BRWalletManager;


public class IntroWriteDownActivity extends Activity {
    private static final String TAG = IntroWriteDownActivity.class.getName();
    private Button writeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_down);
        setStatusBarColor(android.R.color.transparent);
        writeButton = (Button) findViewById(R.id.button_write_down);
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostAuthenticationProcessor.getInstance().onCreateWalletAuth(IntroWriteDownActivity.this, false);

            }
        });
    }

    @Override
    public void onBackPressed() {
        BRWalletManager.getInstance().startBreadActivity(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case BRConstants.PUT_PHRASE_NEW_WALLET_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    PostAuthenticationProcessor.getInstance().onCreateWalletAuth(this, true);
                } else {
                    Log.e(TAG, "WARNING: resultCode != RESULT_OK");
                    BRWalletManager m = BRWalletManager.getInstance();
                    m.wipeWalletButKeystore(this);
                    BRAnimator.resetFragmentAnimator();
                    finish();
                }
                break;
//            case BRConstants.PUT_PHRASE_RECOVERY_WALLET_REQUEST_CODE:
//                if (resultCode == RESULT_OK) {
//                    PostAuthenticationProcessor.getInstance().onRecoverWalletAuth(this, true);
//                } else {
//                    finish();
//                }
//                break;
//            case BRConstants.CANARY_REQUEST_CODE:
//                if (resultCode == RESULT_OK) {
//                    PostAuthenticationProcessor.getInstance().onCanaryCheck(this, true);
//                } else {
//                    finish();
//                }
//                break;
        }
    }

    private void setStatusBarColor(int color) {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(color));
    }

}