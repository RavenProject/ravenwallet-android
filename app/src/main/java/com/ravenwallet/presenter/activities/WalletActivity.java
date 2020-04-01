package com.ravenwallet.presenter.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.TransitionManager;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ViewFlipper;

import com.ravenwallet.R;
import com.ravenwallet.core.BRCorePeer;
import com.ravenwallet.presenter.activities.util.BRActivity;
import com.ravenwallet.presenter.customviews.BRButton;
import com.ravenwallet.presenter.customviews.BRNotificationBar;
import com.ravenwallet.presenter.customviews.BRSearchBar;
import com.ravenwallet.presenter.customviews.BRText;
import com.ravenwallet.presenter.customviews.BaseTextView;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.animation.BRDialog;
import com.ravenwallet.tools.manager.BRSharedPrefs;
import com.ravenwallet.tools.manager.FontManager;
import com.ravenwallet.tools.manager.InternetManager;
import com.ravenwallet.tools.manager.TxManager;
import com.ravenwallet.tools.sqlite.CurrencyDataSource;
import com.ravenwallet.tools.threads.executor.BRExecutor;
import com.ravenwallet.tools.util.CurrencyUtils;
import com.ravenwallet.tools.util.Utils;
import com.ravenwallet.wallet.WalletsMaster;
import com.ravenwallet.wallet.abstracts.BaseWalletManager;
import com.ravenwallet.wallet.abstracts.OnTxListModified;
import com.ravenwallet.wallet.abstracts.SyncListener;
import com.ravenwallet.wallet.RvnWalletManager;
import com.ravenwallet.wallet.util.CryptoUriParser;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.ravenwallet.tools.animation.BRAnimator.t1Size;
import static com.ravenwallet.tools.animation.BRAnimator.t2Size;

import com.ravenwallet.tools.services.SyncService;

public class WalletActivity extends BRActivity implements InternetManager.ConnectionReceiverListener, OnTxListModified, SyncListener {
    private static final String TAG = WalletActivity.class.getName();
    private static final String SYNCED_THROUGH_DATE_FORMAT = "MM/dd/yy HH:mm";
    private static final float SYNC_PROGRESS_LAYOUT_ANIMATION_ALPHA = 0.0f;
    BRText mCurrencyPriceUsd;
    BRText mBalancePrimary;
    BRText mBalanceSecondary;
    Toolbar mToolbar;
    ImageButton mBackButton;
    BRButton mSendButton;
    BRButton mReceiveButton;
    BRText mBalanceLabel;
    ProgressBar mProgressBar;
    public ViewFlipper barFlipper;
    private BRSearchBar searchBar;
    private ImageButton mSearchIcon;
    private ImageButton mSwap;
    private ConstraintLayout toolBarConstraintLayout;
    private BRNotificationBar mNotificationBar;
    private LinearLayout mProgressLayout;
    private BaseTextView mSyncStatusLabel;
    private BaseTextView mProgressLabel;
    private static WalletActivity app;
    private SyncNotificationBroadcastReceiver mSyncNotificationBroadcastReceiver;
    private InternetManager mConnectionReceiver;
    private TestLogger logger;
    private String mCurrentWalletIso;
    private BaseWalletManager mWallet;
    public static WalletActivity getApp() {
        return app;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wallet);

//        mCurrencyTitle = findViewById(R.id.currency_label);
        mCurrencyPriceUsd = findViewById(R.id.currency_usd_price);
        mBalancePrimary = findViewById(R.id.balance_primary);
        mBalanceSecondary = findViewById(R.id.balance_secondary);
        mToolbar = findViewById(R.id.wallet_bar);
        mBackButton = findViewById(R.id.back_icon);
        mSendButton = findViewById(R.id.send_button);
        mReceiveButton = findViewById(R.id.receive_button);
//        mBuyButton = findViewById(R.id.buy_button);
        barFlipper = findViewById(R.id.tool_bar_flipper);
        searchBar = findViewById(R.id.search_bar);
        mSearchIcon = findViewById(R.id.search_icon);
        toolBarConstraintLayout = findViewById(R.id.bread_toolbar);
        mSwap = findViewById(R.id.swap);
        mBalanceLabel = findViewById(R.id.balance_label);
        mProgressLabel = findViewById(R.id.syncing_label);
        mProgressLayout = findViewById(R.id.progress_layout);
        mSyncStatusLabel = findViewById(R.id.sync_status_label);
        mProgressBar = findViewById(R.id.sync_progress);
        mNotificationBar = findViewById(R.id.notification_bar);
        if (Utils.isEmulatorOrDebug(this)) {
            if (logger != null) logger.interrupt();
            logger = new TestLogger(); //Sync logger
            logger.start();
        }

        setUpBarFlipper();

        BRAnimator.init(this);
        mBalancePrimary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);//make it the size it should be after animation to get the X
        mBalanceSecondary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);//make it the size it should be after animation to get the X


        mSendButton.setHasShadow(false);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Activity app = WalletActivity.this;
//                BaseWalletManager wm = WalletsMaster.getInstance(app).getCurrentWallet(app);
//                CryptoUriParser.processRequest(WalletActivity.this, "bitcoin:?r=https://bitpay.com/i/HUsFqTFirmVtgE4PhLzcRx", wm);
                BRAnimator.showSendFragment(WalletActivity.this, null, false, null);

            }
        });

        mReceiveButton.setHasShadow(false);
        mReceiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BRAnimator.showReceiveFragment(WalletActivity.this, true, null);

            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });

        mSearchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!BRAnimator.isClickAllowed()) return;
                barFlipper.setDisplayedChild(1); //search bar
                searchBar.onShow(true);
            }
        });

        mBalancePrimary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swap();
            }
        });
        mBalanceSecondary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swap();
            }
        });

        TxManager.getInstance().init(this);

        onConnectionChanged(InternetManager.getInstance().isConnected(this));
        mWallet = WalletsMaster.getInstance(this).getCurrentWallet(this);
        setupUI();

        boolean cryptoPreferred = BRSharedPrefs.isCryptoPreferred(this);
        setPriceTags(cryptoPreferred, true);

        // Check if the "Twilight" screen altering app is currently running
        if (checkIfScreenAlteringAppIsRunning()) {
            BRDialog.showSimpleDialog(this, getString(R.string.Dialog_screenAlteringTitle), getString(R.string.Dialog_screenAlteringMessage));
        }
        TxManager.getInstance().onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnectionReceiver != null)
            unregisterReceiver(mConnectionReceiver);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //since we have one instance of activity at all times, this is needed to know when a new intent called upon this activity
        handleUrlClickIfNeeded(intent);
    }

    private void handleUrlClickIfNeeded(Intent intent) {
        Uri data = intent.getData();
        if (data != null && !data.toString().isEmpty()) {
            //handle external click with crypto scheme
            CryptoUriParser.processRequest(this, data.toString(), WalletsMaster.getInstance(this).getCurrentWallet(this));
        }
    }

    private void updateListTx(boolean firstInit) {
        updateBalance();
        TxManager.getInstance().updateTxList(WalletActivity.this,firstInit);
    }

    private void setupUI() {
        final BaseWalletManager wallet = WalletsMaster.getInstance(this).getCurrentWallet(this);
        if (wallet == null) {
            Log.e(TAG, "updateListTx: wallet is null");
            return ;
        }

        mToolbar.setBackgroundColor(Color.parseColor(wallet.getUiConfiguration().colorHex));
        mReceiveButton.setColor(Color.parseColor(wallet.getUiConfiguration().colorHex));
    }

    private void updateBalance() {
        final BaseWalletManager wallet = WalletsMaster.getInstance(this).getCurrentWallet(this);
        if (wallet == null) {
            Log.e(TAG, "updateListTx: wallet is null");
            return ;
        }
        String fiatExchangeRate = CurrencyUtils.getFormattedAmount(this, BRSharedPrefs.getPreferredFiatIso(this), wallet.getFiatExchangeRate(this));
        String fiatBalance = CurrencyUtils.getFormattedAmount(this, BRSharedPrefs.getPreferredFiatIso(this), wallet.getFiatBalance(this));
        String cryptoBalance = CurrencyUtils.getFormattedAmount(this, wallet.getIso(this), new BigDecimal(wallet.getCachedBalance(this)));

        mCurrencyPriceUsd.setText(String.format("%s per %s", fiatExchangeRate, wallet.getIso(this)));
        mBalancePrimary.setText(fiatBalance);
        mBalanceSecondary.setText(cryptoBalance);
    }

    // This method checks if a screen altering app(such as Twightlight) is currently running
    // If it is, notify the user that the BRD app will not function properly and they should
    // disable it
    private boolean checkIfScreenAlteringAppIsRunning() {


        // Use the ActivityManager API if sdk version is less than 21
        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        assert usm != null;
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
        if (appList != null && appList.size() > 0) {
            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
            String currentPackageName = "";
            for (UsageStats usageStats : appList) {
                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                currentPackageName = usageStats.getPackageName();


                if (currentPackageName.equals("com.urbandroid.lux")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void swap() {
        if (!BRAnimator.isClickAllowed()) return;
        boolean b = BRSharedPrefs.isCryptoPreferred(this);
        setPriceTags(!b, true);
        BRSharedPrefs.setIsCryptoPreferred(this, !b);
    }

    private void setPriceTags(final boolean cryptoPreferred, boolean animate) {
        ConstraintSet set = new ConstraintSet();
        set.clone(toolBarConstraintLayout);
        if (animate)
            TransitionManager.beginDelayedTransition(toolBarConstraintLayout);
        int px8 = Utils.getPixelsFromDps(this, 8);
        int px16 = Utils.getPixelsFromDps(this, 16);
//
//        //align first item to parent right
//        set.connect(!cryptoPreferred ? R.id.balance_secondary : R.id.balance_primary, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, px16);
//        //align swap symbol after the first item
//        set.connect(R.id.swap, ConstraintSet.START, !cryptoPreferred ? R.id.balance_secondary : R.id.balance_primary, ConstraintSet.START, px8);
//        //align second item after swap symbol
//        set.connect(!cryptoPreferred ? R.id.balance_secondary : R.id.balance_primary, ConstraintSet.START, mSwap.getId(), ConstraintSet.END, px8);
//

        // CRYPTO on RIGHT
        if (cryptoPreferred) {

            // Align crypto balance to the right parent
            set.connect(R.id.balance_secondary, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, px8);
            set.connect(R.id.balance_secondary, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, -px8);

            // Align swap icon to left of crypto balance
            set.connect(R.id.swap, ConstraintSet.END, R.id.balance_secondary, ConstraintSet.START, px8);

            // Align usd balance to left of swap icon
            set.connect(R.id.balance_primary, ConstraintSet.END, R.id.swap, ConstraintSet.START, px8);

            mBalancePrimary.setPadding(0, 0, 0, Utils.getPixelsFromDps(this, 6));
            mBalanceSecondary.setPadding(0, 0, 0, Utils.getPixelsFromDps(this, 4));
            mSwap.setPadding(0, 0, 0, Utils.getPixelsFromDps(this, 2));

            Log.d(TAG, "CryptoPreferred " + cryptoPreferred);

            mBalanceSecondary.setTextSize(t1Size);
            mBalancePrimary.setTextSize(t2Size);

            set.applyTo(toolBarConstraintLayout);

        }

        // CRYPTO on LEFT
        else {

            // Align primary to right of parent
            set.connect(R.id.balance_primary, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, px8);

            // Align swap icon to left of usd balance
            set.connect(R.id.swap, ConstraintSet.END, R.id.balance_primary, ConstraintSet.START, px8);


            // Align secondary currency to the left of swap icon
            set.connect(R.id.balance_secondary, ConstraintSet.END, R.id.swap, ConstraintSet.START, px8);

            mBalancePrimary.setPadding(0, 0, 0, Utils.getPixelsFromDps(this, 2));
            mBalanceSecondary.setPadding(0, 0, 0, Utils.getPixelsFromDps(this, 4));
            mSwap.setPadding(0, 0, 0, Utils.getPixelsFromDps(this, 2));

            //mBalancePrimary.setPadding(0,0, 0, Utils.getPixelsFromDps(this, -4));

            Log.d(TAG, "CryptoPreferred " + cryptoPreferred);

            mBalanceSecondary.setTextSize(t2Size);
            mBalancePrimary.setTextSize(t1Size);


            set.applyTo(toolBarConstraintLayout);

        }


        if (!cryptoPreferred) {
            mBalanceSecondary.setTextColor(getResources().getColor(R.color.currency_subheading_color, null));
            mBalancePrimary.setTextColor(getResources().getColor(R.color.white, null));
            mBalanceSecondary.setTypeface(FontManager.get(this, "CircularPro-Book.otf"));

        } else {
            mBalanceSecondary.setTextColor(getResources().getColor(R.color.white, null));
            mBalancePrimary.setTextColor(getResources().getColor(R.color.currency_subheading_color, null));
            mBalanceSecondary.setTypeface(FontManager.get(this, "CircularPro-Bold.otf"));

        }

        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        updateListTx(false);
                    }
                }, toolBarConstraintLayout.getLayoutTransition().getDuration(LayoutTransition.CHANGE_APPEARING));
    }

    @Deprecated
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
        app = this;
        InternetManager.registerConnectionReceiver(this, this);
        setStatusBarGradiant(app);

        WalletsMaster.getInstance(app).initWallets(app);

        setupNetworking();


        CurrencyDataSource.getInstance(this).addOnDataChangedListener(new CurrencyDataSource.OnDataChanged() {
            @Override
            public void onChanged() {
                BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                            @Override
                            public void run() {
                                updateListTx(false);
                    }
                });
            }
        });
        final BaseWalletManager wallet = WalletsMaster.getInstance(this).getCurrentWallet(this);
        wallet.addTxListModifiedListener(this);
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                long balance = wallet.getWallet().getBalance();
                wallet.setCashedBalance(app, balance);
                BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                    @Override
                    public void run() {
                      updateListTx(false);
                    }
                });

            }
        });

        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                if (wallet.getPeerManager().getConnectStatus() != BRCorePeer.ConnectStatus.Connected)
                    wallet.connectWallet(WalletActivity.this);
            }
        });

        mCurrentWalletIso = wallet.getIso(app);

        wallet.addSyncListeners(this);

        // SyncManager.getInstance().startSyncing(this, wallet, this);
        mSyncNotificationBroadcastReceiver = new SyncNotificationBroadcastReceiver();
        SyncService.registerSyncNotificationBroadcastReceiver(getApplicationContext(), mSyncNotificationBroadcastReceiver);
        SyncService.startService(getApplicationContext(), mCurrentWalletIso);

        handleUrlClickIfNeeded(getIntent());
    }

    @Override
    public void syncStopped(String err) {

    }

    @Override
    public void syncStarted() {
        //SyncManager.getInstance().startSyncing(WalletActivity.this, wallet, WalletActivity.this);
        SyncService.startService(getApplicationContext(), mCurrentWalletIso);
    }

    @Override
    protected void onPause() {
        super.onPause();
        InternetManager.unregisterConnectionReceiver(this, this);
        if (mWallet != null) {
            mWallet.removeSyncListener(this);
        }
        SyncService.unregisterSyncNotificationBroadcastReceiver(getApplicationContext(), mSyncNotificationBroadcastReceiver);
//        TxManager.getInstance().onPause();
    }

    private void setUpBarFlipper() {
        barFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.flipper_enter));
        barFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.flipper_exit));
    }

    public void resetFlipper() {
        barFlipper.setDisplayedChild(0);
    }

    private void setupNetworking() {
        if (mConnectionReceiver == null) mConnectionReceiver = InternetManager.getInstance();
        IntentFilter mNetworkStateFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnectionReceiver, mNetworkStateFilter);
        InternetManager.addConnectionListener(this);
    }


    @Override
    public void onConnectionChanged(boolean isConnected) {
        Log.d(TAG, "onConnectionChanged");
        if (isConnected) {
            if (barFlipper != null && barFlipper.getDisplayedChild() == 2) {
                barFlipper.setDisplayedChild(0);
            }
            SyncService.startService(getApplicationContext(), mCurrentWalletIso);
        } else {
            if (barFlipper != null)
                barFlipper.setDisplayedChild(2);

        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        return;
//        int c = getFragmentManager().getBackStackEntryCount();
//        if (c > 0) {
//            super.onBackPressed();
//            return;
//        }
//        Intent intent = new Intent(this, HomeActivity.class);
//        startActivity(intent);
//        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
//        if (!isDestroyed()) {
//            finish();
//        }
    }

    @Override
    public void txListModified(final String hash) {
        BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
            @Override
            public void run() {
                updateListTx(false);
            }
        });
    }

    /*    @Override
        public boolean onProgressUpdated(double progress) {
            mProgressBar.setProgress((int) (progress * 100));
            mProgressLabel.setText(String.format("%s %d%%", getString(R.string.syncing), (int) (progress * 100)));
            if (progress == 1) {
                mProgressLayout.animate()
                        .translationY(-mProgressLayout.getHeight())
                        .alpha(SYNC_PROGRESS_LAYOUT_ANIMATION_ALPHA)
                        .setDuration(DateUtils.SECOND_IN_MILLIS)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                mProgressLayout.setVisibility(View.GONE);
                            }
                        });
                return false;
            }
            StringBuffer labelText = new StringBuffer(getString(R.string.SyncingView_syncing));
            labelText.append(' ')
                    .append(NumberFormat.getPercentInstance().format(progress));
            mProgressLabel.setText(labelText);
            mProgressLayout.setVisibility(View.VISIBLE);
            final BaseWalletManager wallet = WalletsMaster.getInstance(this).getCurrentWallet(this);

            if ( wallet instanceof BRCoreWalletManager) {
                BRCoreWalletManager brCoreWalletManager = (BRCoreWalletManager) wallet;
                long syncThroughDateInMillis = brCoreWalletManager.getPeerManager().getLastBlockTimestamp() * DateUtils.SECOND_IN_MILLIS;
                String syncedThroughDate = new SimpleDateFormat(SYNCED_THROUGH_DATE_FORMAT).format(syncThroughDateInMillis);
                mSyncStatusLabel.setText(String.format(getString(R.string.SyncingView_syncedThrough), syncedThroughDate));
            }
            return true;
        }
    */
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    //test logger
    class TestLogger extends Thread {
        private static final String TAG = "TestLogger";

        @Override
        public void run() {
            super.run();

            while (true) {
                StringBuilder builder = new StringBuilder();
                for (BaseWalletManager w : WalletsMaster.getInstance(WalletActivity.this).getAllWallets()) {
                    builder.append("   " + w.getIso(WalletActivity.this));
                    String connectionStatus = "";
                    if (w.getPeerManager().getConnectStatus() == BRCorePeer.ConnectStatus.Connected)
                        connectionStatus = "Connected";
                    else if (w.getPeerManager().getConnectStatus() == BRCorePeer.ConnectStatus.Disconnected)
                        connectionStatus = "Disconnected";
                    else if (w.getPeerManager().getConnectStatus() == BRCorePeer.ConnectStatus.Connecting)
                        connectionStatus = "Connecting";

                    double progress = w.getPeerManager().getSyncProgress(BRSharedPrefs.getStartHeight(WalletActivity.this, w.getIso(WalletActivity.this)));

                    builder.append(" - " + connectionStatus + " " + progress * 100 + "%     ");

                }

                Log.e(TAG, "testLog: " + builder.toString());

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * The {@link SyncNotificationBroadcastReceiver} is responsible for receiving updates from the
     * {@link SyncService} and updating the UI accordingly.
     */
    private class SyncNotificationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SyncService.ACTION_SYNC_PROGRESS_UPDATE.equals(intent.getAction())) {
                String intentWalletIso = intent.getStringExtra(SyncService.EXTRA_WALLET_CURRENCY_CODE);
                double progress = intent.getDoubleExtra(SyncService.EXTRA_PROGRESS, SyncService.PROGRESS_NOT_DEFINED);
                if (mCurrentWalletIso.equals(intentWalletIso)) {
                    if (progress >= SyncService.PROGRESS_START) {
                        updateSyncProgress(progress);
                    } else {
                        Log.e(TAG, "SyncNotificationBroadcastReceiver.onReceive: Progress not set:" + progress);
                    }
                } else {
                    Log.e(TAG, "SyncNotificationBroadcastReceiver.onReceive: Wrong wallet. Expected:"
                            + mCurrentWalletIso + " Actual:" + intentWalletIso + " Progress:" + progress);
                }
            }
        }
    }

    public void updateSyncProgress(double progress) {
        long blockHeight = mWallet.getPeerManager().getLastBlockHeight();
        long prefBlockHeight = BRSharedPrefs.getLastBlockHeight(WalletActivity.this, mWallet.getIso(app));
        boolean canSend = blockHeight - prefBlockHeight <= 100;
        if (canSend) {
            mSendButton.setType(4);
            mSendButton.setColor(Color.parseColor(mWallet.getUiConfiguration().colorHex));
            mSendButton.setEnabled(true);
        } else {
           // mSendButton.setColor(Color.parseColor(mWallet.getUiConfiguration().colorHex));
            mSendButton.setEnabled(false);
            mSendButton.setType(5);
        }
        if (progress != SyncService.PROGRESS_FINISH) {
            StringBuffer labelText = new StringBuffer(getString(R.string.SyncingView_syncing));
            labelText.append(' ')
                    .append(NumberFormat.getPercentInstance().format(progress));
            mProgressLabel.setText(labelText);
            mProgressLayout.setVisibility(View.VISIBLE);

            if (mWallet instanceof RvnWalletManager) {
                RvnWalletManager rvnWalletManager = (RvnWalletManager) mWallet;
                long syncThroughDateInMillis = rvnWalletManager.getPeerManager().getLastBlockTimestamp() * DateUtils.SECOND_IN_MILLIS;
                String syncedThroughDate = new SimpleDateFormat(SYNCED_THROUGH_DATE_FORMAT).format(syncThroughDateInMillis);
                mSyncStatusLabel.setText(String.format(getString(R.string.SyncingView_syncedThrough), syncedThroughDate));
            }
        } else {
            mProgressLayout.animate()
                    .translationY(-mProgressLayout.getHeight())
                    .alpha(SYNC_PROGRESS_LAYOUT_ANIMATION_ALPHA)
                    .setDuration(DateUtils.SECOND_IN_MILLIS)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mProgressLayout.setVisibility(View.GONE);
                        }
                    });
        }

    }
}
