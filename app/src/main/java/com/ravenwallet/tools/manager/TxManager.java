package com.ravenwallet.tools.manager;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import androidx.annotation.WorkerThread;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.ravenwallet.R;
import com.ravenwallet.presenter.activities.WalletActivity;
import com.ravenwallet.presenter.entities.TxUiHolder;
import com.ravenwallet.tools.adapter.TransactionListAdapter;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.threads.executor.BRExecutor;
import com.ravenwallet.wallet.WalletsMaster;
import com.ravenwallet.wallet.abstracts.BaseWalletManager;

import java.util.List;


/**
 * RavenWalletP
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 7/19/17.
 * Copyright (c) 2017 breadwallet LLC
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
public class TxManager {

    private static final String TAG = TxManager.class.getName();
    private static TxManager instance;
    private ListView txList;
    private ImageView emptyView;
    public TransactionListAdapter adapter;

    public static TxManager getInstance() {
        if (instance == null) instance = new TxManager();
        return instance;
    }

    public void init(final WalletActivity app) {
        txList = app.findViewById(R.id.tx_list);
        emptyView = app.findViewById(R.id.empty_list);
//        txList.setLayoutManager(new CustomLinearLayoutManager(app));
        txList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TxUiHolder item = adapter.getItems().get(position);
                BRAnimator.showTransactionDetails(app, item, position);
            }
        });
        if (adapter == null)
            adapter = new TransactionListAdapter(app, null);
        txList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        //setupSwipe(app);
    }

    private TxManager() {
    }

    public void onResume(final Activity app) {
        crashIfNotMain();
        updateTxList(app,true);
    }

    @WorkerThread
    public synchronized void updateTxList(final Context app, final boolean firstInit) {
//        long start = System.currentTimeMillis();
        BaseWalletManager wallet = WalletsMaster.getInstance(app).getCurrentWallet(app);
        if (wallet == null) {
            Log.e(TAG, "updateTxList: wallet is null");
            return;
        }
        if (TxManager.getInstance().adapter != null) {
            TxManager.getInstance().adapter.updateData();
        }
        final List<TxUiHolder> items = wallet.getTxUiHolders();
        if (adapter != null) {
            BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                @Override
                public void run() {
                    if (items != null && !items.isEmpty()) {
                        adapter.setItems(items, firstInit);
                        //txList.setAdapter(adapter);
                        txList.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    } else {
                        txList.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    public void onPause() {
        if(adapter!=null)
        adapter.clearData();
    }

    private class CustomLinearLayoutManager extends LinearLayoutManager {

        public CustomLinearLayoutManager(Context context) {
            super(context);
        }

        /**
         * Disable predictive animations. There is a bug in RecyclerView which causes views that
         * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
         * adapter size has decreased since the ViewHolder was recycled.
         */
        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }

        public CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public CustomLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }
    }

    private void crashIfNotMain() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalAccessError("Can only call from main thread");
        }
    }

}
