package com.ravencoin.tools.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.platform.assets.BurnType;
import com.ravencoin.R;
import com.ravencoin.core.BRCoreAddress;
import com.ravencoin.core.MyTransactionAsset;
import com.ravencoin.presenter.customviews.BRText;
import com.ravencoin.presenter.entities.TxUiHolder;
import com.ravencoin.tools.manager.BRSharedPrefs;
import com.ravencoin.tools.threads.executor.BRExecutor;
import com.ravencoin.tools.util.BRDateUtil;
import com.ravencoin.tools.util.CurrencyUtils;
import com.ravencoin.tools.util.Utils;
import com.ravencoin.wallet.WalletsMaster;
import com.ravencoin.wallet.abstracts.BaseWalletManager;
import com.platform.tools.KVStoreManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.platform.assets.BurnType.GLOBAL_BURN;
import static com.platform.assets.BurnType.ISSUE_ASSET;
import static com.platform.assets.BurnType.REISSUE;
import static com.platform.assets.BurnType.SUB;
import static com.platform.assets.BurnType.UNDEFINE;
import static com.platform.assets.BurnType.UNIQUE;
import static com.ravencoin.tools.util.BRConstants.CONFIRMS_COUNT;
import static com.ravencoin.tools.util.BRConstants.CREATION_FEE;
import static com.ravencoin.tools.util.BRConstants.REISSUE_FEE;
import static com.ravencoin.tools.util.BRConstants.SATOSHIS;
import static com.ravencoin.tools.util.BRConstants.STR_BURN_ASSET_ADDESSES;
import static com.ravencoin.tools.util.BRConstants.STR_ISSUE_ASSET_BURN_ADDESSES;
import static com.ravencoin.tools.util.BRConstants.STR_REISSUE_ASSET_BURN_ADDESSES;
import static com.ravencoin.tools.util.BRConstants.STR_REISSUE_SUB_ASSET_BURN_ADDESSES;
import static com.ravencoin.tools.util.BRConstants.STR_UNIQUE_ASSET_BURN_ADDESSES;
import static com.ravencoin.tools.util.BRConstants.SUB_FEE;
import static com.ravencoin.tools.util.BRConstants.UNIQUE_FEE;
import static com.ravencoin.tools.util.BRConstants.receive;


/**
 * RavenWallet
 * <p>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 7/27/15.
 * Copyright (c) 2016 breadwallet LLC
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class TransactionListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = TransactionListAdapter.class.getName();

    private final Context mContext;
    private final int txResId;
    private final int promptResId;
    private List<TxUiHolder> backUpFeed;
    private List<TxUiHolder> itemFeed;
    //    private Map<String, TxMetaData> mds;

    private final int txType = 0;
    private final int promptType = 1;
    private boolean updatingReverseTxHash;
    private boolean updatingData;

//    private boolean updatingMetadata;

    public TransactionListAdapter(Context mContext, List<TxUiHolder> items) {
        this.txResId = R.layout.tx_item;
        this.promptResId = R.layout.prompt_item;
        this.mContext = mContext;
        items = new ArrayList<>();
        init(items);
//        updateMetadata();
    }

    public void setItems(List<TxUiHolder> items) {
        //this.backUpFeed = items!= null?items:new ArrayList<TxUiHolder>();
        init(items);
    }

    private void init(List<TxUiHolder> items) {
        if (items == null) items = new ArrayList<>();
        if (itemFeed == null) itemFeed = new ArrayList<>();
        if (backUpFeed == null) backUpFeed = new ArrayList<>();
        //if (itemFeed.isEmpty())
            this.itemFeed = items;
        this.backUpFeed = items;
    }

    public void updateData() {
        if (updatingData) return;
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                long s = System.currentTimeMillis();
                List<TxUiHolder> newItems = new ArrayList<>(itemFeed);
                TxUiHolder item;
                for (int i = 0; i < newItems.size(); i++) {
                    item = newItems.get(i);
                    item.metaData = KVStoreManager.getInstance().getTxMetaData(mContext, item.getTxHash());
                    item.txReversed = Utils.reverseHex(Utils.bytesToHex(item.getTxHash()));
                }
                backUpFeed = newItems;
                String log = String.format("newItems: %d, took: %d", newItems.size(), (System.currentTimeMillis() - s));
                Log.e(TAG, "updateData: " + log);
                updatingData = false;
            }
        });

    }


    public List<TxUiHolder> getItems() {
        return itemFeed;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate the layout
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        return new TxHolder(inflater.inflate(txResId, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case txType:
                holder.setIsRecyclable(false);
                setTexts((TxHolder) holder, position);
                break;
            case promptType:
                //setPrompt((PromptHolder) holder);
                break;
        }

    }

    @Override
    public int getItemViewType(int position) {
        return txType;
    }

    @Override
    public int getItemCount() {
        return itemFeed.size();
    }

    private void setTexts(final TxHolder convertView, int position) {
        BaseWalletManager wallet = WalletsMaster.getInstance(mContext).getCurrentWallet(mContext);
        TxUiHolder item = itemFeed.get(position);
        item.metaData = KVStoreManager.getInstance().getTxMetaData(mContext, item.getTxHash());

        String commentString = "";
        if (item.metaData != null) {
            if (item.metaData.comment != null) {
                commentString = item.metaData.comment;
            }
        }

        boolean received = item.getSent() == 0;

        if (received) {
            convertView.transactionAmount.setTextColor(mContext.getResources().getColor(R.color.transaction_amount_received_color, null));
            convertView.transactionAction.setText("Received");
        } else {
            convertView.transactionAmount.setTextColor(mContext.getResources().getColor(R.color.viewfinder_laser, null));
            convertView.transactionAction.setText("Sent");
        }

        // If this transaction failed, show the "FAILED" indicator in the cell
        if (!item.isValid())
            showTransactionFailed(convertView, item, received);
        if (item.getAsset() == null) {
            BigDecimal cryptoAmount = new BigDecimal(item.getAmount());
            Log.e(TAG, "setTexts: crypto:" + cryptoAmount);
            boolean isCryptoPreferred = BRSharedPrefs.isCryptoPreferred(mContext);
            String preferredIso = isCryptoPreferred ? wallet.getIso(mContext) : BRSharedPrefs.getPreferredFiatIso(mContext);
            BigDecimal amount = isCryptoPreferred ? cryptoAmount : wallet.getFiatForSmallestCrypto(mContext, cryptoAmount, null);
            Log.e(TAG, "setTexts: amount:" + amount);
            convertView.transactionAmount.setText(CurrencyUtils.getFormattedAmount(mContext, preferredIso, amount));
        } else {
            String assetTxt = getAssetInfo(wallet, item.getAsset());
            convertView.transactionAmount.setText(assetTxt);
        }

        int blockHeight = item.getBlockHeight();
        int confirms = blockHeight == Integer.MAX_VALUE ? 0 : BRSharedPrefs.getLastBlockHeight(mContext, wallet.getIso(mContext)) - blockHeight + 1;

        int level = 0;
        if (confirms <= 0) {
            long relayCount = wallet.getPeerManager().getRelayCount(item.getTxHash());
            if (relayCount <= 0)
                level = 0;
            else if (relayCount == 1)
                level = 1;
            else
                level = 2;
        } else {
            if (confirms == 1)
                level = 3;
            else if (confirms == 2)
                level = 4;
            else if (confirms == 3)
                level = 5;
            else
                level = 6;
        }
        switch (level) {
            case 0:
                showTransactionProgress(convertView, 0);
                break;
            case 1:
                showTransactionProgress(convertView, 20);

                break;
            case 2:
                showTransactionProgress(convertView, 40);
                break;
            case 3:
                showTransactionProgress(convertView, 60);
                break;
            case 4:
                showTransactionProgress(convertView, 80);
                break;
            case 5:
                //showTransactionProgress(convertView, 100);
                break;
        }

        Log.d(TAG, "Level -> " + level);
        String toAddress = getToAddress(wallet, item.getTo());
        boolean isInProgress = level <= CONFIRMS_COUNT;
        if (received) {
            String status = isInProgress ? "receiving via " : "received via ";
            convertView.transactionDetail.setText(!commentString.isEmpty() ?
                    commentString : status + wallet.decorateAddress(mContext, toAddress));
        } else {
            String[] addresses = item.getTo();
            long amount = item.getAmount();
            boolean isBurn = true;
            BurnType burnType = getBurnType(addresses, amount);
            String txDetails;
            String status = isInProgress ? "(Burning) " : "(Burnt)";
            switch (burnType) {
                case UNDEFINE:
                default:
                    isBurn = false;
                    txDetails = !commentString.isEmpty() ? commentString : (!isInProgress ? "sent to " : "sending to ") + wallet.decorateAddress(mContext, toAddress);
                    break;
                case ISSUE_ASSET:
                    txDetails = "Asset creation fee " + status;
                    break;
                case REISSUE:
                    txDetails = "Reissue Asset fee " + status;
                    break;
                case SUB:
                    txDetails = "Sub Asset fee " + status;
                    break;
                case UNIQUE:
                    txDetails = "Unique Asset fee " + status;
                    break;
                case GLOBAL_BURN:
                    txDetails = "Asset " + status;
                    break;
            }
            convertView.transactionDetail.setText(txDetails);
            if (isBurn) {
                convertView.transactionDetail.setTextColor(mContext.getResources().getColor(R.color.viewfinder_laser, null));
            }
        }


        //if it's 0 we use the current time.
        long timeStamp = item.getTimeStamp() == 0 ? System.currentTimeMillis() : item.getTimeStamp() * 1000;

        String shortDate = BRDateUtil.getShortDate(timeStamp);

        convertView.transactionDate.setText(shortDate);

    }

    @Nullable
    private String getToAddress(BaseWalletManager wallet, String[] tos) {
        String toAddress = "";
        if (tos != null)
            for (String to : tos) {
                boolean isMyAddress = wallet.getWallet().containsAddress(new BRCoreAddress(to));
                if (!isMyAddress) {
                    toAddress = to;
                    break;
                }
            }
        if (TextUtils.isEmpty(toAddress) && tos != null)
            toAddress = tos[0];
        return toAddress;
    }

    private BurnType getBurnType(String[] addresses, long txAmount) {
        long amount = Math.abs(txAmount);
        if (addresses == null || addresses.length <= 0)
            return UNDEFINE;
        if (amount == CREATION_FEE * SATOSHIS) {
            for (String address : addresses)
                if (STR_ISSUE_ASSET_BURN_ADDESSES.contains(address))
                    return ISSUE_ASSET;
        }
        if (amount == REISSUE_FEE * SATOSHIS)
            for (String address : addresses)
                if (STR_REISSUE_ASSET_BURN_ADDESSES.contains(address))
                    return REISSUE;

        if (amount == SUB_FEE * SATOSHIS)
            for (String address : addresses)
                if (STR_REISSUE_SUB_ASSET_BURN_ADDESSES.contains(address))
                    return SUB;

        if (amount == UNIQUE_FEE * SATOSHIS)
            for (String address : addresses)
                if (STR_UNIQUE_ASSET_BURN_ADDESSES.contains(address))
                    return UNIQUE;
        for (String address : addresses)
            if (STR_BURN_ASSET_ADDESSES.contains(address))
                return GLOBAL_BURN;
        return UNDEFINE;
    }

    @NonNull
    private String getAssetInfo(BaseWalletManager wallet, MyTransactionAsset mAsset) {
        if (mAsset != null) {
            if (mAsset.getName().endsWith("!"))
                return mAsset.getName();
            else {
                double assetAmount = wallet.getCryptoForSmallestCrypto(mContext, new BigDecimal(mAsset.getAmount())).doubleValue();
                return com.platform.assets.Utils.formatAssetAmount(assetAmount, mAsset.getUnit()) + " " + mAsset.getName();
            }
        }
        return "";
    }

    private void showTransactionProgress(TxHolder holder, int progress) {


        if (progress < 100) {
            holder.transactionProgress.setVisibility(View.VISIBLE);
            holder.transactionDate.setVisibility(View.GONE);
            holder.transactionProgress.setProgress(progress);

            RelativeLayout.LayoutParams detailParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            detailParams.addRule(RelativeLayout.RIGHT_OF, holder.transactionProgress.getId());
//            detailParams.addRule(RelativeLayout.ALIGN_BOTTOM, holder.transactionProgress.getId());
            detailParams.setMargins(Utils.getPixelsFromDps(mContext, 16), Utils.getPixelsFromDps(mContext, 36), 0, 0);
            holder.transactionDetail.setLayoutParams(detailParams);

            holder.transactionAction.setText("In Progress ...");
        } else {
            holder.transactionProgress.setVisibility(View.INVISIBLE);
            holder.transactionDate.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams startingParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            startingParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            startingParams.addRule(RelativeLayout.CENTER_VERTICAL);
            startingParams.setMargins(Utils.getPixelsFromDps(mContext, 16), 0, 0, 0);
            holder.transactionDetail.setLayoutParams(startingParams);
            holder.setIsRecyclable(true);

            holder.transactionAction.setText("Received");
        }
    }

    private void showTransactionFailed(TxHolder holder, TxUiHolder tx, boolean received) {

        holder.transactionDate.setVisibility(View.INVISIBLE);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.RIGHT_OF, holder.transactionFailed.getId());
        params.setMargins(16, 0, 0, 0);
        params.addRule(RelativeLayout.CENTER_VERTICAL, holder.transactionFailed.getId());
        holder.transactionDetail.setLayoutParams(params);

        if (!received) {
            holder.transactionDetail.setText("sending to " + tx.getTo()[0]);
        }
    }

    public void filterBy(String query, boolean[] switches) {
        filter(query, switches);
    }

    public void resetFilter() {
        itemFeed = backUpFeed;
        notifyDataSetChanged();
    }

    private void filter(final String query, final boolean[] switches) {
        long start = System.currentTimeMillis();
        String lowerQuery = query.toLowerCase().trim();
       /* if (Utils.isNullOrEmpty(lowerQuery) && !switches[0] && !switches[1] && !switches[2] && !switches[3])
           return;*/
        int switchesON = 0;
        for (boolean i : switches) if (i) switchesON++;

        final List<TxUiHolder> filteredList = new ArrayList<>();
        TxUiHolder item;
        for (int i = 0; i < backUpFeed.size(); i++) {
            item = backUpFeed.get(i);
            boolean matchesHash = item.getTxHashHexReversed() != null && item.getTxHashHexReversed().contains(lowerQuery);
            boolean matchesAddress = (item.getFrom() != null && item.getFrom().length > 0 && item.getFrom()[0].contains(lowerQuery))
                    || (item.getTo() != null && item.getTo().length > 0 && item.getTo()[0].contains(lowerQuery));
            boolean matchesMemo = item.metaData != null && item.metaData.comment != null && item.metaData.comment.toLowerCase().contains(lowerQuery);
            boolean matchesAssetName = item.getAsset() != null && item.getAsset().getName() != null && item.getAsset().getName().toLowerCase().contains(lowerQuery);
            if (matchesHash || matchesAddress || matchesMemo || matchesAssetName) {
                if (switchesON == 0) {
                    filteredList.add(item);
                } else {
                    boolean willAdd = true;
                    BaseWalletManager wallet = WalletsMaster.getInstance(mContext).getCurrentWallet(mContext);

                    int confirms = item.getBlockHeight() ==
                            Integer.MAX_VALUE ? 0
                            : BRSharedPrefs.getLastBlockHeight(mContext, wallet.getIso(mContext)) - item.getBlockHeight() + 1;
                    //pending
                    if (switches[2] && confirms >= CONFIRMS_COUNT) {
                        willAdd = false;
                    }

                    //complete
                    if (switches[3] && confirms < CONFIRMS_COUNT) {
                        willAdd = false;
                    }

                    boolean received = item.getSent() == 0;
                    //filter by sent and this is received
                    if (switches[0] && received) {
                        willAdd = false;
                    }
                    //filter by received and this is sent
                    if (switches[1] && !received) {
                        willAdd = false;
                    }

                    if (willAdd) filteredList.add(item);
                }
            }

        }
        itemFeed = filteredList;
        notifyDataSetChanged();

        Log.e(TAG, "filter: " + query + " took: " + (System.currentTimeMillis() - start));
    }

    private class TxHolder extends RecyclerView.ViewHolder {
        public RelativeLayout mainLayout;
        public ConstraintLayout constraintLayout;
        public TextView sentReceived;
        public TextView amount;
        public TextView account;
        public TextView status;
        public TextView status_2;
        public TextView timestamp;
        public TextView comment;
        public ImageView arrowIcon;

        public BRText transactionDate;
        public BRText transactionAmount;
        public BRText transactionDetail;
        public Button transactionFailed;
        public BRText transactionAction;
        public ProgressBar transactionProgress;


        public TxHolder(View view) {
            super(view);

            transactionDate = view.findViewById(R.id.tx_date);
            transactionAmount = view.findViewById(R.id.tx_amount);
            transactionDetail = view.findViewById(R.id.tx_description);
            transactionFailed = view.findViewById(R.id.tx_failed_button);
            transactionProgress = view.findViewById(R.id.tx_progress);
            transactionAction = view.findViewById(R.id.tx_action);

        }
    }

}