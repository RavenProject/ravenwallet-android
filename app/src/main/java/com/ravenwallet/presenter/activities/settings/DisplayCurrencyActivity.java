package com.ravenwallet.presenter.activities.settings;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ravenwallet.R;
import com.ravenwallet.presenter.activities.util.BRActivity;
import com.ravenwallet.presenter.entities.CurrencyEntity;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.manager.BRSharedPrefs;
import com.ravenwallet.tools.manager.FontManager;
import com.ravenwallet.tools.sqlite.CurrencyDataSource;
import com.ravenwallet.tools.util.BRConstants;
import com.ravenwallet.tools.util.CurrencyUtils;
import com.ravenwallet.wallet.WalletsMaster;
import com.ravenwallet.wallet.abstracts.BaseWalletManager;

import java.math.BigDecimal;
import java.util.Currency;

import static com.ravenwallet.tools.util.BRConstants.SATOSHIS;


public class DisplayCurrencyActivity extends BRActivity {
    private static final String TAG = DisplayCurrencyActivity.class.getName();
    private TextView exchangeText;
    private ListView listView;
    private CurrencyListAdapter adapter;
    //    private String ISO;
//    private float rate;
    public static boolean appVisible = false;
    private static DisplayCurrencyActivity app;
    private Button leftButton;
    private Button rightButton;
    private Button middleButton;
    BaseWalletManager mWalletManager;


    public static DisplayCurrencyActivity getApp() {
        return app;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_currency);

        ImageButton faq = findViewById(R.id.faq_button);

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                BRAnimator.showSupportFragment(app, BRConstants.displayCurrency);
            }
        });

        mWalletManager = WalletsMaster.getInstance(this).getCurrentWallet(this);

        exchangeText = findViewById(R.id.exchange_text);
        listView = findViewById(R.id.currency_list_view);
        adapter = new CurrencyListAdapter(this);
        adapter.addAll(CurrencyDataSource.getInstance(this).getAllCurrencies(this, WalletsMaster.getInstance(this).getCurrentWallet(this).getIso(this)));
        leftButton = findViewById(R.id.left_button);
        rightButton = findViewById(R.id.right_button);
        middleButton = findViewById(R.id.middle_button);

        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButton(0);
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButton(2);
            }
        });

        middleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButton(1);
            }
        });

        int unit = BRSharedPrefs.getCryptoDenomination(this, "RVN"); // any iso, using one for all for now
        if (unit == BRConstants.CURRENT_UNIT_URVN) {
            setButton(0);
        }
        if (unit == BRConstants.CURRENT_UNIT_MRVN) {
            setButton(1);
        } else {
            setButton(2);
        }
        updateExchangeRate();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                TextView currencyItemText = (TextView) view.findViewById(R.id.currency_item_text);
                final String selectedCurrency = currencyItemText.getText().toString();
                String iso = selectedCurrency.substring(0, 3);
                BRSharedPrefs.putPreferredFiatIso(DisplayCurrencyActivity.this, iso);

                updateExchangeRate();

            }

        });
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void updateExchangeRate() {
        //set the rate from the last saved
        String iso = BRSharedPrefs.getPreferredFiatIso(this);
        CurrencyEntity entity = CurrencyDataSource.getInstance(this).getCurrencyByCode(this, WalletsMaster.getInstance(this).getCurrentWallet(this).getIso(this), iso);
        if (entity != null) {
            String formattedExchangeRate = CurrencyUtils.getFormattedAmount(DisplayCurrencyActivity.this, BRSharedPrefs.getPreferredFiatIso(this), new BigDecimal(entity.rate));
            exchangeText.setText(String.format("%s = %s", CurrencyUtils.getFormattedAmount(this, mWalletManager.getIso(this), new BigDecimal(SATOSHIS)), formattedExchangeRate));
        }
        adapter.notifyDataSetChanged();
    }

    private void setButton(int position) {
        switch (position) {
            case 0:
                BRSharedPrefs.putCryptoDenomination(this, mWalletManager.getIso(this), BRConstants.CURRENT_UNIT_URVN);
                leftButton.setTextColor(getColor(R.color.white));
                leftButton.setBackground(getDrawable(R.drawable.b_half_left_blue));
                rightButton.setTextColor(getColor(R.color.dark_blue));
                rightButton.setBackground(getDrawable(R.drawable.b_half_right_blue_stroke));
                middleButton.setTextColor(getColor(R.color.dark_blue));
                middleButton.setBackground(getDrawable(R.drawable.b_middle_blue_stroke));
                break;
            case 1:
                BRSharedPrefs.putCryptoDenomination(this, mWalletManager.getIso(this), BRConstants.CURRENT_UNIT_MRVN);
                middleButton.setTextColor(getColor(R.color.white));
                middleButton.setBackground(getDrawable(R.drawable.b_half_middle_blue));
                leftButton.setTextColor(getColor(R.color.dark_blue));
                leftButton.setBackground(getDrawable(R.drawable.b_half_left_blue_stroke));
                rightButton.setTextColor(getColor(R.color.dark_blue));
                rightButton.setBackground(getDrawable(R.drawable.b_half_right_blue_stroke));
                break;
            case 2:
                BRSharedPrefs.putCryptoDenomination(this, mWalletManager.getIso(this), BRConstants.CURRENT_UNIT_RAVENS);
                rightButton.setTextColor(getColor(R.color.white));
                rightButton.setBackground(getDrawable(R.drawable.b_half_right_blue));
                leftButton.setTextColor(getColor(R.color.dark_blue));
                leftButton.setBackground(getDrawable(R.drawable.b_half_left_blue_stroke));
                middleButton.setTextColor(getColor(R.color.dark_blue));
                middleButton.setBackground(getDrawable(R.drawable.b_middle_blue_stroke));
                break;
        }
        updateExchangeRate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        app = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    public class CurrencyListAdapter extends ArrayAdapter<CurrencyEntity> {
        public final String TAG = CurrencyListAdapter.class.getName();

        private final Context mContext;
        private final int layoutResourceId;
        private TextView textViewItem;
        private final Point displayParameters = new Point();

        public CurrencyListAdapter(Context mContext) {

            super(mContext, R.layout.currency_list_item);

            this.layoutResourceId = R.layout.currency_list_item;
            this.mContext = mContext;
            ((Activity) mContext).getWindowManager().getDefaultDisplay().getSize(displayParameters);
//        currencyListAdapter = this;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final String oldIso = BRSharedPrefs.getPreferredFiatIso(mContext);
            if (convertView == null) {
                // inflate the layout
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }
            // get the TextView and then set the text (item name) and tag (item ID) values
            textViewItem = convertView.findViewById(R.id.currency_item_text);
            FontManager.overrideFonts(textViewItem);
            String iso = getItem(position).code;
            Currency c = null;
            try {
                c = Currency.getInstance(iso);
            } catch (IllegalArgumentException ignored) {
            }
            textViewItem.setText(c == null ? iso : String.format("%s (%s)", iso, c.getSymbol()));
            ImageView checkMark = convertView.findViewById(R.id.currency_checkmark);

            if (iso.equalsIgnoreCase(oldIso)) {
                checkMark.setVisibility(View.VISIBLE);
            } else {
                checkMark.setVisibility(View.GONE);
            }
            normalizeTextView();
            return convertView;

        }

        @Override
        public int getCount() {
            return super.getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return IGNORE_ITEM_VIEW_TYPE;
        }

        private boolean isTextSizeAcceptable(TextView textView) {
            textView.measure(0, 0);
            int textWidth = textView.getMeasuredWidth();
            int checkMarkWidth = 76 + 20;
            return (textWidth <= (displayParameters.x - checkMarkWidth));
        }

        private boolean normalizeTextView() {
            int count = 0;
//        Log.d(TAG, "Normalizing the text view !!!!!!");
            while (!isTextSizeAcceptable(textViewItem)) {
                count++;
                float textSize = textViewItem.getTextSize();
//            Log.e(TAG, "The text size is: " + String.valueOf(textSize));
                textViewItem.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize - 2);
                this.notifyDataSetChanged();
            }
            return (count > 0);
        }

    }


}
