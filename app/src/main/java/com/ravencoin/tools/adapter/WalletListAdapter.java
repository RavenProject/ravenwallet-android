package com.ravencoin.tools.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.ravencoin.R;
import com.ravencoin.core.BRCorePeer;
import com.ravencoin.presenter.customviews.BRText;
import com.ravencoin.tools.manager.BRSharedPrefs;
import com.ravencoin.tools.manager.SyncManager;
import com.ravencoin.tools.threads.executor.BRExecutor;
import com.ravencoin.tools.util.CurrencyUtils;
import com.ravencoin.wallet.WalletsMaster;
import com.ravencoin.wallet.abstracts.BaseWalletManager;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by byfieldj on 1/31/18.
 */

public class WalletListAdapter extends RecyclerView.Adapter<WalletListAdapter.WalletItemViewHolder> {

    public static final String TAG = WalletListAdapter.class.getName();

    private final Context mContext;
    private ArrayList<WalletItem> mWalletItems;
    private WalletItem mCurrentWalletSyncing;
    private SyncManager mSyncManager;
    private boolean mObserverIsStarting;


    public WalletListAdapter(Context context, ArrayList<BaseWalletManager> walletList) {
        this.mContext = context;
        mWalletItems = new ArrayList<>();
        for (BaseWalletManager w : walletList) {
            this.mWalletItems.add(new WalletItem(w));
        }
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
    public void onBindViewHolder(final WalletItemViewHolder holder, int position) {

        WalletItem item = mWalletItems.get(position);
        final BaseWalletManager wallet = item.walletManager;
        String name = wallet.getName(mContext);
        String exchangeRate     = CurrencyUtils.getFormattedAmount(mContext, BRSharedPrefs.getPreferredFiatIso(mContext), wallet.getFiatExchangeRate(mContext));
        String fiatBalance      = CurrencyUtils.getFormattedAmount(mContext, BRSharedPrefs.getPreferredFiatIso(mContext), wallet.getFiatBalance(mContext));
        String cryptoBalance    = CurrencyUtils.getFormattedAmount(mContext, wallet.getIso(mContext), new BigDecimal(wallet.getCachedBalance(mContext)));

        final String iso = wallet.getIso(mContext);

        // Set wallet fields
        holder.mWalletName.setText(name);
        holder.mTradePrice.setText(exchangeRate);
//        if (position == 0)
//            holder.mTradePrice.setText("$0.0356254");
        holder.mWalletBalanceUSD.setText(fiatBalance);
        holder.mWalletBalanceCurrency.setText(cryptoBalance);
        holder.mSyncingProgressBar.setVisibility(item.mShowSyncing ? View.VISIBLE : View.INVISIBLE);
        holder.mSyncingProgressBar.setProgress(item.mProgress);
        holder.mSyncingLabel.setVisibility(item.mShowSyncingLabel ? View.VISIBLE : View.INVISIBLE);
        holder.mSyncingLabel.setText(String.format("%s %d%%", item.mLabelText, item.mProgress));
        holder.mWalletBalanceCurrency.setVisibility(item.mShowBalance ? View.VISIBLE : View.INVISIBLE);

        Drawable drawable = mContext.getResources().getDrawable(R.drawable.crypto_card_shape, null);
        ((GradientDrawable) drawable).setColor(Color.parseColor(wallet.getUiConfiguration().colorHex));

        holder.mParent.setBackground(drawable);

    }

    public void stopObserving() {
        if (mSyncManager != null)
            mSyncManager.stopSyncing();
    }

    public void startObserving() {
        if (mObserverIsStarting) return;
        mObserverIsStarting = true;

        BRExecutor.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mCurrentWalletSyncing = getNextWalletToSync();
                    Log.e(TAG, "startObserving.." + mCurrentWalletSyncing);
                    if (mCurrentWalletSyncing == null) {
                        Log.e(TAG, "startObserving: all wallets synced:" + Thread.currentThread());
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                for (WalletItem item : mWalletItems) {

                                    item.updateData(false, false, true, 100, "Done");
                                    notifyDataSetChanged();

                                }
                            }
                        });

                        return;
                    }

                    Log.e(TAG, "startObserving: connecting: " + mCurrentWalletSyncing.walletManager.getIso(mContext));
                    mCurrentWalletSyncing.walletManager.connectWallet(mContext);
                    mSyncManager = SyncManager.getInstance();
                    mSyncManager.startSyncing(mContext, mCurrentWalletSyncing.walletManager, new SyncManager.OnProgressUpdate() {
                        @Override
                        public boolean onProgressUpdated(double progress) {
                            return updateUi(mCurrentWalletSyncing, progress);
                        }
                    });
                } finally {
                    mObserverIsStarting = false;
                }

            }
        });

    }

    private boolean updateUi(WalletItem currentWallet, double syncProgress) {

        if (mCurrentWalletSyncing == null || mCurrentWalletSyncing.walletManager == null) {
            Log.e(TAG, "run: should not happen but ok, ignore it.");
            return false;
        }
        if (syncProgress > 0.0 && syncProgress < 1.0) {
            int progress = (int) (syncProgress * 100);
//            Log.d(TAG, "ISO: " + currentWallet.walletManager.getIso(mContext) + " (" + progress + "%)");

            mCurrentWalletSyncing.updateData(true, true, false, progress, "Syncing");
            notifyDataSetChanged();
        }

        // HAS NOT STARTED SYNCING
        else if (syncProgress == 0.0) {
//            Log.d(TAG, "ISO: " + currentWallet.walletManager.getIso(mContext) + " (0%)");
            mCurrentWalletSyncing.updateData(false, true, false, 0, "Waiting to Sync");
            notifyDataSetChanged();
        }

        // FINISHED SYNCING
        else if (syncProgress == 1.0) {
//            Log.d(TAG, "ISO: " + currentWallet.walletManager.getIso(mContext) + " (100%)");

            //Done should not be seen but if it is because of a bug or something, then let if be a decent explanation
            mCurrentWalletSyncing.updateData(false, false, true, 100, "Done");
            notifyDataSetChanged();

            //start from beginning
            startObserving();
            return false;

        }
        return true;
    }

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
        public RelativeLayout mParent;
        public BRText mSyncingLabel;
        public ProgressBar mSyncingProgressBar;


        public WalletItemViewHolder(View view) {
            super(view);

            mWalletName = view.findViewById(R.id.wallet_name);
            mTradePrice = view.findViewById(R.id.wallet_trade_price);
            mWalletBalanceUSD = view.findViewById(R.id.wallet_balance_usd);
            mWalletBalanceCurrency = view.findViewById(R.id.wallet_balance_currency);
            mParent = view.findViewById(R.id.wallet_card);
            mSyncingLabel = view.findViewById(R.id.syncing_label);
            mSyncingProgressBar = view.findViewById(R.id.sync_progress);
        }
    }

    private class WalletItem {
        public BaseWalletManager walletManager;
        private boolean mShowSyncing = true;
        private boolean mShowSyncingLabel = true;
        private boolean mShowBalance = false;
        private int mProgress; //1 - 100%
        private String mLabelText = "Waiting to Sync";

        public WalletItem(BaseWalletManager walletManager) {
            this.walletManager = walletManager;
        }

        public void updateData(boolean showSyncing, boolean showSyncingLabel, boolean showBalance, int progress, String labelText) {
            mShowSyncing = showSyncing;
            mShowSyncingLabel = showSyncingLabel;
            mShowBalance = showBalance;
            mProgress = progress;
            mLabelText = labelText;
        }
    }
}
