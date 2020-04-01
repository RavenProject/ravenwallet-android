
package com.ravenwallet.presenter.activities.intro;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.ravenwallet.BuildConfig;
import com.ravenwallet.R;
import com.ravenwallet.presenter.activities.HomeActivity;
import com.ravenwallet.presenter.activities.util.BRActivity;
import com.ravenwallet.presenter.newTutorial.TutorialActivity;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.manager.BRReportsManager;
import com.ravenwallet.tools.security.BRKeyStore;
import com.ravenwallet.tools.security.PostAuth;
import com.ravenwallet.tools.security.SmartValidator;
import com.ravenwallet.tools.util.BRConstants;
import com.ravenwallet.tools.util.Utils;
import com.ravenwallet.wallet.WalletsMaster;

import java.io.Serializable;


/**
 * RavenWallet
 * <p/>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 8/4/15.
 * Copyright (c) 2016 breadwallet LLC
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class IntroActivity extends BRActivity implements Serializable {
    private static final String TAG = IntroActivity.class.getName();
    public Button newWalletButton;
    public Button recoverWalletButton;
    public static IntroActivity introActivity;
    public static boolean appVisible = false;
    private static IntroActivity app;
    private View splashScreen;
    private ImageButton faq;

    public static IntroActivity getApp() {
        return app;
    }

    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        newWalletButton = (Button) findViewById(R.id.button_new_wallet);
        recoverWalletButton = (Button) findViewById(R.id.button_recover_wallet);
        splashScreen = findViewById(R.id.splash_screen);
        setListeners();
        //TODO uncomment
        //updateBundles();
//        SyncManager.getInstance().updateAlarms(this);
        faq = (ImageButton) findViewById(R.id.faq_button);

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                BRAnimator.showSupportFragment(app, BRConstants.startView);
            }
        });


        if (!BuildConfig.DEBUG && BRKeyStore.AUTH_DURATION_SEC != 300) {
            Log.e(TAG, "onCreate: BRKeyStore.AUTH_DURATION_SEC != 300");
            BRReportsManager.reportBug(new RuntimeException("AUTH_DURATION_SEC should be 300"), true);
        }
        introActivity = this;

        getWindowManager().getDefaultDisplay().getSize(screenParametersPoint);

        if (Utils.isEmulatorOrDebug(this))
            Utils.printPhoneSpecs();

        byte[] masterPubKey = BRKeyStore.getMasterPublicKey(this);

        boolean isFirstAddressCorrect = false;
        if (masterPubKey != null && masterPubKey.length != 0) {
            isFirstAddressCorrect = SmartValidator.checkFirstAddress(this, masterPubKey);
        }
        if (!isFirstAddressCorrect) {
            WalletsMaster.getInstance(this).wipeWalletButKeystore(this);
        }

        PostAuth.getInstance().onCanaryCheck(this, false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                splashScreen.setVisibility(View.GONE);
            }
        }, 1000);

    }

//    private void updateBundles() {
//        BRExecutor.getInstance().forBackgroundTasks().execute(new Runnable() {
//            @Override
//            public void run() {
//                Thread.currentThread().setName("updateBundle");
//                final long startTime = System.currentTimeMillis();
//                APIClient apiClient = APIClient.getInstance(IntroActivity.this);
//              apiClient.updateBundle();
//                long endTime = System.currentTimeMillis();
//                Log.d(TAG, "updateBundle DONE in " + (endTime - startTime) + "ms");
//            }
//        });
//    }


    private void setListeners() {
        newWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                HomeActivity bApp = HomeActivity.getApp();
                Intent intent = new Intent(IntroActivity.this, TutorialActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                if (bApp != null) bApp.finish();
            }
        });

        recoverWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                HomeActivity bApp = HomeActivity.getApp();
                if (bApp != null) bApp.finish();
                Intent intent = new Intent(IntroActivity.this, RecoverActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                if (bApp != null) bApp.finish();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void changeStatusBarColor() {
        Window window = app.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(app, R.color.primaryColor));

        final int lFlags = window.getDecorView().getSystemUiVisibility();
        // update the SystemUiVisibility depending on whether we want a Light or Dark theme.
        window.getDecorView().setSystemUiVisibility((lFlags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarGradiant(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            Drawable background = activity.getResources().getDrawable(R.drawable.gradient_blue);
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

//        changeStatusBarColor();
        setStatusBarGradiant(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
