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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ravenwallet.R;
import com.ravenwallet.presenter.activities.util.BRActivity;
import com.ravenwallet.presenter.customviews.BRDialogView;
import com.ravenwallet.presenter.entities.IPFSGateway;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.animation.BRDialog;
import com.ravenwallet.tools.manager.BREventManager;
import com.ravenwallet.tools.manager.BRSharedPrefs;
import com.ravenwallet.tools.manager.FontManager;
import com.ravenwallet.tools.util.BRConstants;
import com.ravenwallet.wallet.WalletsMaster;
import com.ravenwallet.wallet.abstracts.BaseWalletManager;

import java.util.Currency;


public class IPFSGatewayActivity extends BRActivity {
    private static final String TAG = IPFSGatewayActivity.class.getName();
    private ListView listView;
    private GatewayListAdapter adapter;
    public static boolean appVisible = false;
    private static IPFSGatewayActivity app;
    BaseWalletManager mWalletManager;


    public static IPFSGatewayActivity getApp() {
        return app;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipfs_gateway_settings);

        ImageButton faq = findViewById(R.id.faq_button);

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                BRDialog.showCustomDialog(getApp(),
                        getString(R.string.Settings_ipfsgateway),
                        getString(R.string.Settings_ipfsgateway_faq),
                        getString(R.string.Button_ok), null,
                        new BRDialogView.BROnClickListener() {
                            @Override
                            public void onClick(BRDialogView brDialogView) {
                                brDialogView.dismissWithAnimation();
                            }
                        }, null, null, 0);
            }
        });

        mWalletManager = WalletsMaster.getInstance(this).getCurrentWallet(this);

        listView = findViewById(R.id.gateway_list_view);
        adapter = new GatewayListAdapter(this);

        adapter.add(new IPFSGateway("ipfs.io A", "ipfs.io"));
        adapter.add(new IPFSGateway("ipfs.io B", "gateway.ipfs.io"));

        adapter.add(new IPFSGateway("Cloudflare A", "cf-ipfs.com"));
        adapter.add(new IPFSGateway("Cloudflare B", "cloudflare-ipfs.com"));

        adapter.add(new IPFSGateway("DWeb", "dweb.link"));
        adapter.add(new IPFSGateway("GreyH.at", "ipfs.greyh.at"));
        adapter.add(new IPFSGateway("infura.io", "ipfs.infura.io"));
        //Really wanted this one to work, but it times out too much to be reliable :(
        //adapter.add(new IPFSGateway("Ravenland", "gateway.ravenland.org"));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                IPFSGateway newGateway = adapter.getItem(position);
                BRSharedPrefs.putPreferredIPFSGateway(IPFSGatewayActivity.this, newGateway.hostname);
                adapter.notifyDataSetChanged();
            }

        });

        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
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

    public class GatewayListAdapter extends ArrayAdapter<IPFSGateway> {
        public final String TAG = GatewayListAdapter.class.getName();

        private final Context mContext;
        private final int layoutResourceId;
        private TextView textViewItem;
        private final Point displayParameters = new Point();

        public GatewayListAdapter(Context mContext) {

            super(mContext, R.layout.gateway_list_item);

            this.layoutResourceId = R.layout.gateway_list_item;
            this.mContext = mContext;
            ((Activity) mContext).getWindowManager().getDefaultDisplay().getSize(displayParameters);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final String oldHost = BRSharedPrefs.getPreferredIPFSGateway(mContext);
            if (convertView == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }
            textViewItem = convertView.findViewById(R.id.gateway_item_name);
            FontManager.overrideFonts(textViewItem);
            IPFSGateway gateway = getItem(position);
            textViewItem.setText(String.format("%s (%s)", gateway.name, gateway.hostname));
            ImageView checkMark = convertView.findViewById(R.id.gateway_checkmark);

            if (gateway.hostname.equalsIgnoreCase(oldHost)) {
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
            while (!isTextSizeAcceptable(textViewItem)) {
                count++;
                float textSize = textViewItem.getTextSize();
                textViewItem.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize - 2);
                this.notifyDataSetChanged();
            }
            return (count > 0);
        }

    }


}
