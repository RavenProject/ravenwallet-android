package com.ravenwallet;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Point;
import android.hardware.fingerprint.FingerprintManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.ravenwallet.presenter.activities.util.BRActivity;
import com.ravenwallet.tools.listeners.SyncReceiver;
import com.ravenwallet.tools.manager.InternetManager;
import com.ravenwallet.tools.util.Utils;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

//import static com.platform.APIClient.BREAD_POINT;

public class RavenApp extends Application {

    private static final String TAG = RavenApp.class.getName();
    public static int DISPLAY_HEIGHT_PX;
    FingerprintManager mFingerprintManager;
    // host is the server(s) on which the API is hosted
    public static String HOST = "api.ravenwallet.org";
    private static List<OnAppBackgrounded> listeners;
    private static Timer isBackgroundChecker;
    public static AtomicInteger activityCounter = new AtomicInteger();
    public static long backgroundedTime;
    public static boolean appInBackground;
    private static Context mContext;

    public static final boolean IS_ALPHA = false;

//    public static final Map<String, String> mHeaders = new HashMap<>();

    private static Activity currentActivity;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseCrashlytics.getInstance();

        mContext = this;

        if (!Utils.isEmulatorOrDebug(this) && IS_ALPHA)
            throw new RuntimeException("can't be alpha for release");

////        boolean isTestVersion = BREAD_POINT.contains("staging") || BREAD_POINT.contains("stage");
//        boolean isTestNet = BuildConfig.TESTNET;
//        String lang = getCurrentLocale(this);

//        mHeaders.put("X-Is-Internal", IS_ALPHA ? "true" : "false");
//        mHeaders.put("X-Testflight", "false");
//        mHeaders.put("X-Bitcoin-Testnet", isTestNet ? "true" : "false");
//        mHeaders.put("Accept-Language", lang);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int DISPLAY_WIDTH_PX = size.x;
        DISPLAY_HEIGHT_PX = size.y;

        mFingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);

        registerReceiver(InternetManager.getInstance(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

//        addOnBackgroundedListener(new OnAppBackgrounded() {
//            @Override
//            public void onBackgrounded() {
//
//            }
//        });

    }

    @TargetApi(Build.VERSION_CODES.N)
    public String getCurrentLocale(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return ctx.getResources().getConfiguration().getLocales().get(0).getLanguage();
        } else {
            //noinspection deprecation
            return ctx.getResources().getConfiguration().locale.getLanguage();
        }
    }

//    public static Map<String, String> getRvnHeaders() {
//        return mHeaders;
//    }

    public static Context getRvnContext() {
        Context app = currentActivity;
        if (app == null) app = SyncReceiver.app;
        if (app == null) app = mContext;
        return app;
    }

    public static void setRvnContext(Activity app) {
        currentActivity = app;
    }

    public static synchronized void fireListeners() {
        if (listeners == null) return;
        List<OnAppBackgrounded> copy = listeners;
        for (OnAppBackgrounded lis : copy) if (lis != null) lis.onBackgrounded();
    }

    public static void addOnBackgroundedListener(OnAppBackgrounded listener) {
        if (listeners == null) listeners = new ArrayList<>();
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public static boolean isAppInBackground(final Context context) {
        return context == null || activityCounter.get() <= 0;
    }

    //call onStop on evert activity so
    public static void onStop(final BRActivity app) {
        if (isBackgroundChecker != null) isBackgroundChecker.cancel();
        isBackgroundChecker = new Timer();
        TimerTask backgroundCheck = new TimerTask() {
            @Override
            public void run() {
                if (isAppInBackground(app)) {
                    backgroundedTime = System.currentTimeMillis();
                    Log.e(TAG, "App went in background!");
                    // APP in background, do something
                    isBackgroundChecker.cancel();
                    fireListeners();
                }
                // APP in foreground, do something else
            }
        };

        isBackgroundChecker.schedule(backgroundCheck, 500, 500);
    }

    public interface OnAppBackgrounded {
        void onBackgrounded();
    }
}
