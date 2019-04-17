package com.ravencoin.presenter.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
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
import com.ravencoin.R;
import com.ravencoin.presenter.AssetChangeListener;
import com.ravencoin.presenter.activities.settings.SecurityCenterActivity;
import com.ravencoin.presenter.activities.settings.SettingsActivity;
import com.ravencoin.presenter.activities.util.ActivityUTILS;
import com.ravencoin.presenter.activities.util.BRActivity;
import com.ravencoin.presenter.customviews.BRButton;
import com.ravencoin.presenter.customviews.BRNotificationBar;
import com.ravencoin.presenter.customviews.BRText;
import com.ravencoin.presenter.newTutorial.TutorialActivity;
import com.ravencoin.tools.animation.BRAnimator;
import com.ravencoin.tools.manager.BREventManager;
import com.ravencoin.tools.manager.BRSharedPrefs;
import com.ravencoin.tools.manager.InternetManager;
import com.ravencoin.tools.manager.PromptManager;
import com.ravencoin.tools.services.SyncService;
import com.ravencoin.tools.sqlite.CurrencyDataSource;
import com.ravencoin.tools.threads.executor.BRExecutor;
import com.ravencoin.tools.util.CurrencyUtils;
import com.ravencoin.wallet.WalletsMaster;
import com.ravencoin.wallet.abstracts.BaseWalletManager;
import com.ravencoin.wallet.wallets.raven.RvnWalletManager;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static com.ravencoin.presenter.activities.ManageAssetsActivity.IS_OWNED_ASSETS_VIEW_EXTRAS_KEY;

/**
 * Home activity that will show a list of a user's wallets
 */

public class HomeActivity extends BRActivity implements InternetManager.ConnectionReceiverListener, AssetChangeListener {

    private static final String TAG = HomeActivity.class.getSimpleName();

    //    private RecyclerView mWalletRecycler;
//    private WalletListAdapter mAdapter;
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
    private String chartType = ChartModel.ChartType.AreaSpline;
    String CHART_URL = "https://international.bittrex.com/Api/v2.0/pub/market/GetTicks?marketName=BTC-RVN&tickInterval=day";

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
//        mAdapter = new WalletListAdapter(this, walletList);
//        mWalletRecycler.setLayoutManager(new LinearLayoutManager(this));
//        mWalletRecycler.setAdapter(mAdapter);

        assetsAdapter = new AssetsAdapter(this, new ArrayList<Asset>());
        mAssetsRecycler.setAdapter(assetsAdapter);


//        mWalletRecycler.addOnItemTouchListener(new RecyclerItemClickListener(this, mWalletRecycler, new RecyclerItemClickListener.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position, float x, float y) {
//                if (position >= mAdapter.getItemCount() || position < 0) return;
//                BRSharedPrefassetsAdapters.putCurrentWalletIso(HomeActivity.this, mAdapter.getItemAt(position).getIso(HomeActivity.this));
////                Log.d("HomeActivity", "Saving current wallet ISO as " + mAdapter.getItemAt(position).getIso(HomeActivity.this));
//
//                Intent newIntent = new Intent(HomeActivity.this, WalletActivity.class);
//                startActivity(newIntent);
//                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
//            }
//
//            @Override
//            public void onLongItemClick(View view, int position) {
//
//            }
//        }));

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

        final String iso = wallet.getIso(this);

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
                //     .legendVerticalAlign(ChartModel.ChartLegendVerticalAlignType.Bottom)
                .series(
                        new SeriesElement[]{
                                new SeriesElement().data(
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

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mAdapter.startObserving();
//            }
//        }, 500);

//        InternetManager.addConnectionListener(new InternetManager.ConnectionReceiverListener() {
//            @Override
//            public void onConnectionChanged(boolean isConnected) {
//                Log.e(TAG, "onConnectionChanged: " + isConnected);
//                if (isConnected) {
//                    mAdapter.startObserving();
//                }
//            }
//        });

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
            StringBuffer labelText = new StringBuffer(getString(R.string.SyncingView_syncing));
            labelText.append(' ')
                    .append(NumberFormat.getPercentInstance().format(syncProgress));
            label = labelText.toString();
            syncing = true;
        }
        setWalletFields(syncing, label);

    }

    public void startObserving() {
        SyncService.startService(HomeActivity.this, RvnWalletManager.getInstance(this).getIso(this));
    }
}
