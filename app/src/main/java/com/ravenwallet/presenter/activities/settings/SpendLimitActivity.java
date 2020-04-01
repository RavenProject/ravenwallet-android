package com.ravenwallet.presenter.activities.settings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.ravenwallet.R;
import com.ravenwallet.presenter.activities.util.BRActivity;
import com.ravenwallet.presenter.customviews.BRText;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.security.AuthManager;
import com.ravenwallet.tools.security.BRKeyStore;
import com.ravenwallet.tools.util.BRConstants;
import com.ravenwallet.tools.util.CurrencyUtils;
import com.ravenwallet.wallet.RvnWalletManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


import static com.ravenwallet.tools.util.BRConstants.SATOSHIS;


public class SpendLimitActivity extends BRActivity {
    private static final String TAG = SpendLimitActivity.class.getName();
    private ListView listView;
    private LimitAdaptor adapter;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spend_limit);

        ImageButton faq = findViewById(R.id.faq_button);

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                BRAnimator.showSupportFragment(SpendLimitActivity.this, BRConstants.fingerprintSpendingLimit);
            }
        });

        listView = findViewById(R.id.limit_list);
        listView.setFooterDividersEnabled(true);
        adapter = new LimitAdaptor(this);
        List<Long> items = new ArrayList<>();
        items.add(getAmountByStep(0).longValue());
        items.add(getAmountByStep(1).longValue());
        items.add(getAmountByStep(2).longValue());
        items.add(getAmountByStep(3).longValue());
        items.add(getAmountByStep(4).longValue());

        adapter.addAll(items);
        listView.setAdapter(adapter);
    }

    //satoshis
    private BigDecimal getAmountByStep(int step) {
        BigDecimal result;
        switch (step) {
            case 0:
                result = new BigDecimal(0);
                break;
            case 1:
                result = new BigDecimal(10 * SATOSHIS);
                break;
            case 2:
                result = new BigDecimal(50 * SATOSHIS);
                break;
            case 3:
            default:
                result = new BigDecimal(500 * SATOSHIS);
                break;
            case 4:
                result = new BigDecimal(1000 * SATOSHIS);
                break;
        }
        return result;
    }



    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    public class LimitAdaptor extends ArrayAdapter<Long> {

        private final Context mContext;
        private final int layoutResourceId;
        private List<Long> items;
        private long limit;

        public LimitAdaptor(Context mContext) {
            super(mContext, R.layout.currency_list_item);
            this.layoutResourceId = R.layout.currency_list_item;
            this.mContext = mContext;
            items = new ArrayList<>();
            limit = BRKeyStore.getSpendLimit(mContext);
        }

        public void addAll(List<Long> items) {
            if (items != null)
                this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                // inflate the layout
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(layoutResourceId, parent, false);
                holder = new Holder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
//            final long limit = BRKeyStore.getSpendLimit(app);
            // get the TextView and then set the text (item name) and tag (item ID) values
            final long item = getItem(position);

            String cryptoAmount = CurrencyUtils.getFormattedAmount(mContext, RvnWalletManager.ISO, new BigDecimal(item));
            String text = String.format(item == 0 ? mContext.getString(R.string.TouchIdSpendingLimit) : "%s", cryptoAmount);
            holder.textViewItem.setText(text);

            if (item == limit) {
                holder.checkMark.setVisibility(View.VISIBLE);
            } else {
                holder.checkMark.setVisibility(View.GONE);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    limit = item;
                    notifyDataSetChanged();
                    BRKeyStore.putSpendLimit(item, mContext);
                    RvnWalletManager wallet = RvnWalletManager.getInstance(mContext);
                    if (wallet != null) {
                        long totalSent = wallet.getTotalSent(mContext); //collect total total sent
                        AuthManager.getInstance().setTotalLimit(mContext, totalSent + BRKeyStore.getSpendLimit(mContext));
                    }
                }
            });
            return convertView;

        }

        @Override
        public Long getItem(int position) {
            return items.get(position);
        }

        @Override
        public int getCount() {
            if (items != null)
                return items.size();
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            return IGNORE_ITEM_VIEW_TYPE;
        }

        private class Holder {
            BRText textViewItem;
            ImageView checkMark;

            public Holder(View view) {
                textViewItem = view.findViewById(R.id.currency_item_text);
                checkMark = view.findViewById(R.id.currency_checkmark);
            }
        }

    }

}
