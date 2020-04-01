package com.ravenwallet.presenter.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.platform.assets.Asset;
import com.platform.assets.AssetsRepository;
import com.platform.assets.adapter.AssetsAdapter;
import com.platform.chart.model.RVNToBTCData;
import com.platform.chart.widget.ChartModel;
import com.platform.chart.widget.ChartView;
import com.platform.chart.widget.SeriesElement;
import com.ravenwallet.R;
import com.ravenwallet.presenter.AssetChangeListener;
import com.ravenwallet.presenter.activities.settings.SecurityCenterActivity;
import com.ravenwallet.presenter.activities.settings.SettingsActivity;
import com.ravenwallet.presenter.activities.util.ActivityUTILS;
import com.ravenwallet.presenter.activities.util.BRActivity;
import com.ravenwallet.presenter.customviews.BRButton;
import com.ravenwallet.presenter.customviews.BRNotificationBar;
import com.ravenwallet.presenter.customviews.BRText;
import com.ravenwallet.presenter.newTutorial.TutorialActivity;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.manager.BREventManager;
import com.ravenwallet.tools.manager.BRSharedPrefs;
import com.ravenwallet.tools.manager.InternetManager;
import com.ravenwallet.tools.manager.PromptManager;
import com.ravenwallet.tools.services.SyncService;
import com.ravenwallet.tools.sqlite.CurrencyDataSource;
import com.ravenwallet.tools.threads.executor.BRExecutor;
import com.ravenwallet.tools.util.CurrencyUtils;
import com.ravenwallet.wallet.WalletsMaster;
import com.ravenwallet.wallet.abstracts.BaseWalletManager;
import com.ravenwallet.wallet.RvnWalletManager;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import static com.ravenwallet.presenter.activities.ManageAssetsActivity.IS_OWNED_ASSETS_VIEW_EXTRAS_KEY;

/**
 * Home activity that will show a list of a user's wallets
 */

public class HomeActivity extends BRActivity implements InternetManager.ConnectionReceiverListener, AssetChangeListener {

    private static final String TAG = HomeActivity.class.getSimpleName();

    //private RecyclerView mWalletRecycler;
    //private WalletListAdapter mAdapter;
    private BRText mFiatTotal;
    private RelativeLayout mAssetCreation;
    private RelativeLayout mSettings;
    private RelativeLayout mAddressBook;
    private RelativeLayout mSecurity;
    private RelativeLayout mSupport;
    private RelativeLayout mTotorial;
    private RelativeLayout mShowMoreLayout;
    private PromptManager.PromptItem mCurrentPrompt;
    public BRNotificationBar mNotificationBar;
    private AssetsAdapter assetsAdapter;
    private BRText mPromptTitle;
    private BRText mPromptDescription;
    private BRButton mPromptContinue;
    private BRButton mPromptDismiss;
    private CardView mPromptCard;
    private AssetsRepository repository;
    private ChartView mChart;
    private ChartModel chartModel;
    private BRText mWalletName;
    private BRText mTradePrice;
    private BRText mWalletBalanceUSD;
    private BRText mWalletBalanceCurrency;
    private CardView mParent;
    private RelativeLayout mWalletInfos;
    private BRText mSyncingLabel;
    private ProgressBar mSyncingProgressBar;
    private BRText lblBitterex;
    private ImageView imageLogo;
    private GifDrawable gifDrawable = null;

    private String chartType = ChartModel.ChartType.AreaSpline;
    String CHART_URL = "https://bittrex.com/Api/v2.0/pub/market/GetTicks?marketName=BTC-RVN&tickInterval=day";

    private static HomeActivity app;
    private SyncNotificationBroadcastReceiver mSyncNotificationBroadcastReceiver = new SyncNotificationBroadcastReceiver();

    // Assets
    private TextView mAssetsLabel;
    private RecyclerView mAssetsRecycler;

    private final static int MAX_ASSETS_NUMBER_TO_SHOW = 3;

    public static HomeActivity getApp() {
        return app;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        WalletsMaster.getInstance(this).initWallets(this);

//        ArrayList<BaseWalletManager> walletList = new ArrayList<>();

//        walletList.addAll(WalletsMaster.getInstance(this).getAllWallets());

//        mWalletRecycler = findViewById(R.id.rv_wallet_list);
        mFiatTotal = findViewById(R.id.total_assets_usd);
        imageLogo = findViewById(R.id.image_logo);
        mAssetCreation = findViewById(R.id.create_asset);
        mSettings = findViewById(R.id.settings_row);
        mAddressBook = findViewById(R.id.address_book_row);
        mSecurity = findViewById(R.id.security_row);
        mSupport = findViewById(R.id.support_row);
        mTotorial = findViewById(R.id.tutorial_row);
        mNotificationBar = findViewById(R.id.notification_bar);

        mPromptCard = findViewById(R.id.prompt_card);
        mPromptTitle = findViewById(R.id.prompt_title);
        mPromptDescription = findViewById(R.id.prompt_description);
        mPromptContinue = findViewById(R.id.continue_button);
        mPromptDismiss = findViewById(R.id.dismiss_button);
        mShowMoreLayout = findViewById(R.id.show_more_layout);
        mAssetsLabel = findViewById(R.id.assets_label);
        mAssetsRecycler = findViewById(R.id.asset_list);

        mWalletName = findViewById(R.id.wallet_name);
        mTradePrice = findViewById(R.id.wallet_trade_price);
        mWalletBalanceUSD = findViewById(R.id.wallet_balance_usd);
        mWalletBalanceCurrency = findViewById(R.id.wallet_balance_currency);
        mParent = findViewById(R.id.layout_wallet);
        mWalletInfos = findViewById(R.id.layout_wallet_info);
        mSyncingLabel = findViewById(R.id.syncing_label);
        mSyncingProgressBar = findViewById(R.id.sync_progress);
        lblBitterex = findViewById(R.id.lbl_bitterex);
        mChart = findViewById(R.id.chart_view);

        assetsAdapter = new AssetsAdapter(this, new ArrayList<Asset>());
        mAssetsRecycler.setAdapter(assetsAdapter);

        imageLogo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final GifImageView logoAnim = findViewById(R.id.logo_anim);
                if (logoAnim == null) return false;
                try {
                    Display display = getWindowManager().getDefaultDisplay();
                    DisplayMetrics outMetrics = new DisplayMetrics();
                    display.getMetrics(outMetrics);
                    gifDrawable = new GifDrawable(getResources(), R.drawable.logo_anim);
                    logoAnim.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, outMetrics.heightPixels));
                    logoAnim.setImageDrawable(gifDrawable);
                    logoAnim.setVisibility(View.VISIBLE);
                    gifDrawable.start();
                    setStatusBarGradiant(HomeActivity.this, android.R.color.black);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setStatusBarGradiant(HomeActivity.this, android.R.color.transparent);
                            logoAnim.setVisibility(View.GONE);
                            gifDrawable.stop();
                        }
                    }, gifDrawable.getDuration());
                } catch (Exception e) {
                    e.printStackTrace();
                    logoAnim.setVisibility(View.GONE);
                    if (gifDrawable != null)
                        gifDrawable.stop();
                }
                return false;
            }
        });
        mAssetCreation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BRAnimator.showAssetCreationFragment(HomeActivity.this);
            }
        });
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        });
        mAddressBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AddressBookActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
            }
        });
        mSecurity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SecurityCenterActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
            }
        });
        mSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                BRAnimator.showSupportFragment(HomeActivity.this, null);
            }
        });
        mTotorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, TutorialActivity.class);
                intent.putExtra(TutorialActivity.REPLAY_TUTO, true);
                startActivityForResult(intent, 0);
            }
        });

        mPromptDismiss.setColor(Color.parseColor("#5667a5"));
        mPromptDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePrompt();
            }
        });

        mPromptContinue.setColor(Color.parseColor("#f16726"));
        mPromptContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PromptManager.PromptInfo info = PromptManager.getInstance().promptInfo(app, mCurrentPrompt);
                if (info.listener != null)
                    info.listener.onClick(mPromptContinue);
                else
                    Log.e(TAG, "Continue :" + info.title + " (FAILED)");
            }
        });
        BaseWalletManager wallet = RvnWalletManager.getInstance(this);
        setWalletFields(true, null);
        Drawable drawable = getResources().getDrawable(R.drawable.crypto_card_shape, null);
        assert wallet != null;
        ((GradientDrawable) drawable).setColor(Color.parseColor(wallet.getUiConfiguration().colorHex));
        mParent.setBackground(drawable);
        mChart.setBackgroundColor(getColor(R.color.primaryColor));
        mChart.setBackground(drawable);
        mChart.setLayerType(WebView.LAYER_TYPE_NONE, null);
        getRVNValueHistory();
        mWalletInfos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                BRSharedPrefs.putCurrentWalletIso(HomeActivity.this, iso);
                Intent newIntent = new Intent(HomeActivity.this, WalletActivity.class);
                startActivity(newIntent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        });
        startObserving();
    }

    private void setWalletFields(boolean mShowSyncing, String labelSync) {
        BaseWalletManager wallet = RvnWalletManager.getInstance(this);
        if (wallet == null) return;
        String name = wallet.getName(this);
        String exchangeRate = CurrencyUtils.getFormattedAmount(this,
                BRSharedPrefs.getPreferredFiatIso(this), wallet.getFiatExchangeRate(this));
        String fiatBalance = CurrencyUtils.getFormattedAmount(this,
                BRSharedPrefs.getPreferredFiatIso(this), wallet.getFiatBalance(this));
        String cryptoBalance = CurrencyUtils.getFormattedAmount(this, wallet.getIso(this),
                new BigDecimal(wallet.getCachedBalance(this)));

        // Set wallet fields
        mWalletName.setText(name);
        mTradePrice.setText(exchangeRate);
//        if (position == 0)
//            mTradePrice.setText("$0.0356254");
        mWalletBalanceUSD.setText(fiatBalance);
        mWalletBalanceCurrency.setText(cryptoBalance);
        mSyncingProgressBar.setVisibility(mShowSyncing ? View.VISIBLE : View.INVISIBLE);
        mSyncingProgressBar.setProgress(mShowSyncing ? View.VISIBLE : View.INVISIBLE);
        mSyncingLabel.setVisibility(mShowSyncing ? View.VISIBLE : View.INVISIBLE);
        mSyncingLabel.setText(labelSync);
        mWalletBalanceCurrency.setVisibility(!mShowSyncing ? View.VISIBLE : View.INVISIBLE);
    }

    private void getRVNValueHistory() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, CHART_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        RVNToBTCData data = new Gson().fromJson(response, RVNToBTCData.class);

                        // Set the data on the chart view
                        setChartData(data);
                        mChart.setVisibility(View.VISIBLE);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", error.toString());
                mChart.setVisibility(View.GONE);
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void setChartData(RVNToBTCData data) {
        chartModel = new ChartModel()
                .chartType(chartType)
                .backgroundColor("#2e3e80")
                .dataLabelEnabled(false)
                .yAxisGridLineWidth(0)
                .borderRadius(4)
                .series(
                        new SeriesElement[]{
                                new SeriesElement().name("RVN").data(
                                        data.toObjectArray()
                                ).lineWidth(1.0f)
                                        .step(true)

                        });
        mChart.aa_drawChartWithChartModel(chartModel);
    }

    private void setAssets() {
        List<Asset> assets = getAssetList();
        int assetsSize = assets.size();
        if (assetsSize == 0) {
            mAssetsLabel.setVisibility(View.GONE);
            mAssetsRecycler.setVisibility(View.GONE);
            mShowMoreLayout.setVisibility(View.GONE);
        } else {
            mAssetsLabel.setVisibility(View.VISIBLE);
            mAssetsRecycler.setVisibility(View.VISIBLE);
            if (assetsSize <= MAX_ASSETS_NUMBER_TO_SHOW) {
                // Creating then setting the adapter
                assetsAdapter.setAssets(assets);
                ViewCompat.setNestedScrollingEnabled(mAssetsRecycler, false);

                // Hiding the Show More layout
                mShowMoreLayout.setVisibility(View.GONE);
            } else {
                // Creating then setting the adapter with only the first three items
                assetsAdapter.setAssets(assets.subList(0, 3));
                ViewCompat.setNestedScrollingEnabled(mAssetsRecycler, false);

                // Showing the Show More layout
                mShowMoreLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private List<Asset> getAssetList() {
        return repository.getVisibleAssets();

    }

    public void hidePrompt() {
        mPromptCard.setVisibility(View.GONE);
        Log.e(TAG, "hidePrompt: " + mCurrentPrompt);
        if (mCurrentPrompt == PromptManager.PromptItem.SHARE_DATA) {
            BRSharedPrefs.putShareDataDismissed(app, true);
        }
        if (mCurrentPrompt != null)
            BREventManager.getInstance().pushEvent("prompt." + PromptManager.getInstance().getPromptName(mCurrentPrompt) + ".dismissed");
        mCurrentPrompt = null;

    }

    private void showNextPromptIfNeeded() {
        PromptManager.PromptItem toShow = PromptManager.getInstance().nextPrompt(this);
        if (toShow != null) {
            mCurrentPrompt = toShow;
//            Log.d(TAG, "showNextPrompt: " + toShow);
            PromptManager.PromptInfo promptInfo = PromptManager.getInstance().promptInfo(this, toShow);
            mPromptCard.setVisibility(View.VISIBLE);
            mPromptTitle.setText(promptInfo.title);
            mPromptDescription.setText(promptInfo.description);
            mPromptContinue.setOnClickListener(promptInfo.listener);

        } else {
            Log.i(TAG, "showNextPrompt: nothing to show");
            mPromptCard.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        app = this;

        ActivityUTILS.changeStatusBarColor(app, R.color.extra_light_blue_background);
        showNextPromptIfNeeded();
        InternetManager.registerConnectionReceiver(this, this);
        SyncService.registerSyncNotificationBroadcastReceiver(this, mSyncNotificationBroadcastReceiver);

        updateUi();
        CurrencyDataSource.getInstance(this).addOnDataChangedListener(new CurrencyDataSource.OnDataChanged() {
            @Override
            public void onChanged() {
                BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                    @Override
                    public void run() {
                        updateUi();
                    }
                });

            }
        });
        repository = AssetsRepository.getInstance(this);
        repository.addListener(this);
        // Set the Assets view
        setAssets();
    }

    @Override
    protected void onPause() {
        super.onPause();
        InternetManager.unregisterConnectionReceiver(this, this);
        SyncService.unregisterSyncNotificationBroadcastReceiver(this,
                mSyncNotificationBroadcastReceiver);
        repository.removeListener(this);
    }

    private void updateUi() {
        BigDecimal fiatTotalAmount = WalletsMaster.getInstance(this).getAggregatedFiatBalance(this);
        if (fiatTotalAmount == null) {
            Log.e(TAG, "updateUi: fiatTotalAmount is null");
            return;
        }
        mFiatTotal.setText(CurrencyUtils.getFormattedAmount(this, BRSharedPrefs.getPreferredFiatIso(this), fiatTotalAmount));
    }

    @Override
    public void onConnectionChanged(boolean isConnected) {
        Log.d(TAG, "onConnectionChanged: isConnected: " + isConnected);
        if (isConnected) {
            if (mNotificationBar != null) {
                mNotificationBar.setVisibility(View.GONE);
            }
            if (mChart.getVisibility() == View.GONE)
                getRVNValueHistory();
            startObserving();
        } else {
            if (mNotificationBar != null) {
                mNotificationBar.setVisibility(View.VISIBLE);
                mNotificationBar.bringToFront();
            }
        }
    }

    public void closeNotificationBar() {
        mNotificationBar.setVisibility(View.INVISIBLE);
    }


    public void onShowMoreClicked(View view) {
        Intent intent = new Intent(this, ManageAssetsActivity.class);
        intent.putExtra(IS_OWNED_ASSETS_VIEW_EXTRAS_KEY, true);
        startActivity(intent);
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    @Override
    public void onChange() {
        setAssets();
    }

    private class SyncNotificationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SyncService.ACTION_SYNC_PROGRESS_UPDATE.equals(intent.getAction())) {
                String intentWalletIso = intent.getStringExtra(SyncService.EXTRA_WALLET_CURRENCY_CODE);
                double progress = intent.getDoubleExtra(SyncService.EXTRA_PROGRESS, SyncService.PROGRESS_NOT_DEFINED);
                updateUi(progress);
            }
        }
    }

    public void updateUi(double syncProgress) {

        String label = null;
        boolean syncing = false;
        if (syncProgress > SyncService.PROGRESS_START && syncProgress < SyncService.PROGRESS_FINISH) {
            String labelText = getString(R.string.SyncingView_syncing) + ' ' +
                    NumberFormat.getPercentInstance().format(syncProgress);
            label = labelText;
            syncing = true;
        }
        setWalletFields(syncing, label);

    }

    public void startObserving() {
        SyncService.startService(HomeActivity.this, RvnWalletManager.getInstance(this).getIso(this));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarGradiant(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            Drawable background = activity.getResources().getDrawable(R.drawable.gradient_blue);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(color, null));
            window.setNavigationBarColor(activity.getResources().getColor(color, null));

            //final int lFlags = window.getDecorView().getSystemUiVisibility();
            //update the SystemUiVisibility depending on whether we want a Light or Dark theme.
            //window.getDecorView().setSystemUiVisibility((lFlags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));
            //window.setBackgroundDrawable(background);
        }
    }
}
