package com.ravenwallet.tools.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.platform.chart.model.RVNToBTCData;
import com.platform.chart.widget.ChartModel;
import com.platform.chart.widget.ChartView;
import com.platform.chart.widget.SeriesElement;
import com.ravenwallet.R;
import com.ravenwallet.core.BRCorePeer;
import com.ravenwallet.presenter.activities.HomeActivity;
import com.ravenwallet.presenter.activities.WalletActivity;
import com.ravenwallet.presenter.customviews.BRText;
import com.ravenwallet.tools.manager.BRSharedPrefs;
import com.ravenwallet.tools.util.CurrencyUtils;
import com.ravenwallet.wallet.WalletsMaster;
import com.ravenwallet.wallet.abstracts.BaseWalletManager;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by byfieldj on 1/31/18.
 */

public class WalletListAdapter extends RecyclerView.Adapter<WalletListAdapter.WalletItemViewHolder> {

    public static final String TAG = WalletListAdapter.class.getName();

    private final HomeActivity mContext;
    private ArrayList<WalletItem> mWalletItems;
    private WalletItem mCurrentWalletSyncing;
    //   private SyncManager mSyncManager;
    private boolean mObserverIsStarting;
    private boolean chartLoaded;
    private String CHART_URL = "https://bittrex.com/Api/v2.0/pub/market/GetTicks?marketName=BTC-RVN&tickInterval=day";


    public WalletListAdapter(HomeActivity context, ArrayList<BaseWalletManager> walletList) {
        this.mContext = context;
        mWalletItems = new ArrayList<>();
        for (BaseWalletManager w : walletList) {
            this.mWalletItems.add(new WalletItem(w));
        }
        mCurrentWalletSyncing = getNextWalletToSync();

    }

    @Override
    public WalletItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View convertView = inflater.inflate(R.layout.wallet_list_item, parent, false);

        return new WalletItemViewHolder(convertView);
    }

    public BaseWalletManager getItemAt(int pos) {
        return mWalletItems.get(pos).walletManager;
    }

    @Override
    public void onBindViewHolder(final WalletItemViewHolder holder, final int position) {
        holder.setIsRecyclable(false);
        WalletItem item = mWalletItems.get(position);
        final BaseWalletManager wallet = item.walletManager;
        String name = wallet.getName(mContext);
        String exchangeRate = CurrencyUtils.getFormattedAmount(mContext, BRSharedPrefs.getPreferredFiatIso(mContext), wallet.getFiatExchangeRate(mContext));
        String fiatBalance = CurrencyUtils.getFormattedAmount(mContext, BRSharedPrefs.getPreferredFiatIso(mContext), wallet.getFiatBalance(mContext));
        String cryptoBalance = CurrencyUtils.getFormattedAmount(mContext, wallet.getIso(mContext), new BigDecimal(wallet.getCachedBalance(mContext)));

        final String iso = wallet.getIso(mContext);

        // Set wallet fields
        holder.mWalletName.setText(name);
        holder.mTradePrice.setText(exchangeRate);
//        if (position == 0)
//            holder.mTradePrice.setText("$0.0356254");
        holder.mWalletBalanceUSD.setText(fiatBalance);
        holder.mWalletBalanceCurrency.setText(cryptoBalance);
        holder.mSyncingProgressBar.setVisibility(item.mShowSyncing ? View.VISIBLE : View.INVISIBLE);
        holder.mSyncingProgressBar.setProgress(item.mShowSyncing ? View.VISIBLE : View.INVISIBLE);
        holder.mSyncingLabel.setVisibility(item.mShowSyncing ? View.VISIBLE : View.INVISIBLE);
        holder.mSyncingLabel.setText(item.mLabelText);
        holder.mWalletBalanceCurrency.setVisibility(!item.mShowSyncing ? View.VISIBLE : View.INVISIBLE);
        Drawable drawable = mContext.getResources().getDrawable(R.drawable.crypto_card_shape, null);
        ((GradientDrawable) drawable).setColor(Color.parseColor(wallet.getUiConfiguration().colorHex));
        holder.mParent.setBackground(drawable);
        holder.mChart.setBackgroundColor(mContext.getColor(R.color.primaryColor));
        holder.mChart.setBackground(drawable);
        holder.mChart.setLayerType(WebView.LAYER_TYPE_NONE, null);
        if (!chartLoaded)
            getRVNValueHistory(holder.mChart);
        holder.mWalletInfos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (position >= mAdapter.getItemCount() || position < 0) return;
                BRSharedPrefs.putCurrentWalletIso(mContext, getItemAt(position).getIso(mContext));
//                Log.d("HomeActivity", "Saving current wallet ISO as " + mAdapter.getItemAt(position).getIso(HomeActivity.this));

                Intent newIntent = new Intent(mContext, WalletActivity.class);
                mContext.startActivity(newIntent);
                mContext.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                chartLoaded = false;
            }
        });
    }

//    public void stopObserving() {
//        SyncService.unregisterSyncNotificationBroadcastReceiver(mContext.getApplicationContext(),
//                mSyncNotificationBroadcastReceiver);
//    }
//
//    public void startObserving() {
//        if (mObserverIsStarting) return;
//        mObserverIsStarting = true;
//        SyncService.registerSyncNotificationBroadcastReceiver(mContext.getApplicationContext(), mSyncNotificationBroadcastReceiver);
//
//        BRExecutor.getInstance().forBackgroundTasks().execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    mCurrentWalletSyncing = getNextWalletToSync();
//                    Log.e(TAG, "startObserving.." + mCurrentWalletSyncing);
//                    if (mCurrentWalletSyncing == null) {
//                        Log.e(TAG, "startObserving: all wallets synced:" + Thread.currentThread());
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//                                for (WalletItem item : mWalletItems) {
//                                    item.updateData(false);
//                                    notifyDataSetChanged();
//                                }
//                            }
//                        });
//
//                        return;
//                    }
//                    String walletIso = mCurrentWalletSyncing.walletManager.getIso(mContext);
//                    Log.e(TAG, "startObserving: connecting: " + mCurrentWalletSyncing.walletManager.getIso(mContext));
//                    mCurrentWalletSyncing.walletManager.connectWallet(mContext);
//                    SyncService.startService(mContext, walletIso);
//                } finally {
//                    mObserverIsStarting = false;
//                }
//
//            }
//        });
//
//    }


    //return the next wallet that is not connected or null if all are connected
    private WalletItem getNextWalletToSync() {
        BaseWalletManager currentWallet = WalletsMaster.getInstance(mContext).getCurrentWallet(mContext);
        if (currentWallet.getPeerManager().getSyncProgress(BRSharedPrefs.getStartHeight(mContext, currentWallet.getIso(mContext))) == 1)
            currentWallet = null;

        for (WalletItem w : mWalletItems) {
            if (currentWallet == null) {
                if (w.walletManager.getPeerManager().getSyncProgress(BRSharedPrefs.getStartHeight(mContext, w.walletManager.getIso(mContext))) < 1 ||
                        w.walletManager.getPeerManager().getConnectStatus() != BRCorePeer.ConnectStatus.Connected) {
                    w.walletManager.getPeerManager().connect();
                    return w;
                }
            } else {
                if (w.walletManager.getIso(mContext).equalsIgnoreCase(currentWallet.getIso(mContext)))
                    return w;
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return mWalletItems.size();
    }

    public class WalletItemViewHolder extends RecyclerView.ViewHolder {

        public BRText mWalletName;
        public BRText mTradePrice;
        public BRText mWalletBalanceUSD;
        public BRText mWalletBalanceCurrency;
        public CardView mParent;
        public RelativeLayout mWalletInfos;
        public BRText mSyncingLabel;
        public ProgressBar mSyncingProgressBar;
        public BRText lblBitterex;
        public ChartView mChart;

        public WalletItemViewHolder(View view) {
            super(view);

            mWalletName = view.findViewById(R.id.wallet_name);
            mTradePrice = view.findViewById(R.id.wallet_trade_price);
            mWalletBalanceUSD = view.findViewById(R.id.wallet_balance_usd);
            mWalletBalanceCurrency = view.findViewById(R.id.wallet_balance_currency);
            mParent = view.findViewById(R.id.wallet_card);
            mWalletInfos = view.findViewById(R.id.layout_wallet_info);
            mSyncingLabel = view.findViewById(R.id.syncing_label);
            mSyncingProgressBar = view.findViewById(R.id.sync_progress);
            lblBitterex = view.findViewById(R.id.lbl_bitterex);
            mChart = view.findViewById(R.id.chart_view);
        }
    }

    private class WalletItem {
        public BaseWalletManager walletManager;
        private boolean mShowSyncing = true;
        //private boolean mShowSyncingLabel = true;
        // private boolean mShowBalance = false;
        //private int mProgress; //1 - 100%
        private String mLabelText = "Waiting to Sync";

        public WalletItem(BaseWalletManager walletManager) {
            this.walletManager = walletManager;
        }

        public void updateData(boolean showSyncProgress) {
            updateData(showSyncProgress, null);
        }

        public void updateData(boolean showSyncing, String labelText) {
            mShowSyncing = showSyncing;
            //    mShowSyncingLabel = showSyncingLabel;
            //    mShowBalance = showBalance;
            //  mProgress = progress;
            if (labelText != null) {
                mLabelText = labelText;
            }
        }
    }

    private void getRVNValueHistory(final ChartView chartView) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(mContext);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, CHART_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        RVNToBTCData data = new Gson().fromJson(response, RVNToBTCData.class);

                        // Set the data on the chart view
                        setChartData(chartView, data);
                        chartLoaded = true;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", error.toString());
//                chartView.setVisibility(View.GONE);
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void setChartData(ChartView chartView, RVNToBTCData data) {
        ChartModel chartModel = new ChartModel()
                .chartType(ChartModel.ChartType.AreaSpline)
                .backgroundColor("#2e3e80")
                .dataLabelEnabled(false)
                .yAxisGridLineWidth(0)
                .borderRadius(5)
                .series(new SeriesElement[]{
                        new SeriesElement().name("RVN")
                                .data(
                                        data.toObjectArray()
                                ).lineWidth(1.0f)
                                .step(true)
                });
        chartView.aa_drawChartWithChartModel(chartModel);
        chartView.setVisibility(View.VISIBLE);
    }

    //    /**
//     * The {@link SyncNotificationBroadcastReceiver} is responsible for receiving updates from the
//     * {@link SyncService} and updating the UI accordingly.
//     */
//    private class SyncNotificationBroadcastReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (SyncService.ACTION_SYNC_PROGRESS_UPDATE.equals(intent.getAction())) {
//                String intentWalletIso = intent.getStringExtra(SyncService.EXTRA_WALLET_CURRENCY_CODE);
//                double progress = intent.getDoubleExtra(SyncService.EXTRA_PROGRESS, SyncService.PROGRESS_NOT_DEFINED);
//
//                if (mCurrentWalletSyncing == null) {
//                    Log.e(TAG, "SyncNotificationBroadcastReceiver.onReceive: mCurrentWalletSyncing is null. Wallet:" + intentWalletIso + " Progress:" + progress + " Ignored");
//                    return;
//                }
//
//                String currentWalletCurrencyCode = mCurrentWalletSyncing.walletManager.getIso(mContext);
//                if (currentWalletCurrencyCode.equals(intentWalletIso)) {
//                    if (progress >= SyncService.PROGRESS_START) {
//                        updateUi(mCurrentWalletSyncing, progress);
//                    } else {
//                        Log.e(TAG, "SyncNotificationBroadcastReceiver.onReceive: Progress not set:" + progress);
//                    }
//                } else {
//                    Log.e(TAG, "SyncNotificationBroadcastReceiver.onReceive: Wrong wallet. Expected:" + currentWalletCurrencyCode + " Actual:" + intentWalletIso + " Progress:" + progress);
//                }
//            }
//        }
//    }
    public void updateUi(boolean showSyncing, String label) {

        if (mCurrentWalletSyncing == null || mCurrentWalletSyncing.walletManager == null) {
            Log.e(TAG, "run: should not happen but ok, ignore it.");
            return;
        }
        Log.e(TAG, "label " + label);
        mCurrentWalletSyncing.updateData(showSyncing, label);
        notifyDataSetChanged();
    }
}
