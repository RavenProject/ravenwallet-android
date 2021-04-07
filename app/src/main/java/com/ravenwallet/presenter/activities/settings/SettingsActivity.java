package com.ravenwallet.presenter.activities.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.ravenwallet.R;
import com.ravenwallet.presenter.activities.ManageAssetsActivity;
import com.ravenwallet.presenter.activities.UpdatePinActivity;
import com.ravenwallet.presenter.activities.util.BRActivity;
import com.ravenwallet.presenter.entities.BRSettingsItem;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.manager.BRSharedPrefs;

import java.util.ArrayList;
import java.util.List;

import static com.ravenwallet.R.layout.settings_list_item;
import static com.ravenwallet.R.layout.settings_list_section;

public class SettingsActivity extends BRActivity {
    private static final String TAG = SettingsActivity.class.getName();
    private ListView listView;
    public List<BRSettingsItem> items;
    public static boolean appVisible = false;
    private static SettingsActivity app;

    public static SettingsActivity getApp() {
        return app;
    }

    private ImageButton mBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        listView = findViewById(R.id.settings_list);

        mBackButton = findViewById(R.id.back_button);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }


    public class SettingsListAdapter extends ArrayAdapter<String> {

        private List<BRSettingsItem> items;
        private Context mContext;

        public SettingsListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<BRSettingsItem> items) {
            super(context, resource);
            this.items = items;
            this.mContext = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View v;
            BRSettingsItem item = items.get(position);
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();

            if (item.isSection) {
                v = inflater.inflate(settings_list_section, parent, false);
            } else {
                v = inflater.inflate(settings_list_item, parent, false);
                TextView addon = (TextView) v.findViewById(R.id.item_addon);

                if (!addon.getText().toString().isEmpty() && addon.getText().toString() != null) {
                    addon.setVisibility(View.VISIBLE);
                    addon.setText(item.addonText);
                }


                if (position == 10) {
                    ImageButton leaveArrow = v.findViewById(R.id.arrow_leave);
                    ImageButton chevronRight = v.findViewById(R.id.chevron_right);
                    leaveArrow.setVisibility(View.VISIBLE);
                    chevronRight.setVisibility(View.INVISIBLE);
                }
                /*else if (position == 9) {
                    boolean shareData = BRSharedPrefs.getShareData(SettingsActivity.this);
                    if (shareData) {
                        addon.setText("ON");
                    } else {
                        addon.setText("OFF");
                    }
                }*/

                v.setOnClickListener(item.listener);

            }

            TextView title = (TextView) v.findViewById(R.id.item_title);
            title.setText(item.title);


            return v;

        }

        @Override
        public int getCount() {
            return items == null ? 0 : items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        app = this;
        if (items == null)
            items = new ArrayList<>();
        items.clear();

        populateItems();

        listView.setAdapter(new SettingsListAdapter(this, R.layout.settings_list_item, items));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    private void populateItems() {

        items.add(new BRSettingsItem(getString(R.string.Settings_wallet), "", null, true));


        items.add(new BRSettingsItem(getString(R.string.Settings_wipe), "", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, UnlinkActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        }, false));


        items.add(new BRSettingsItem(getString(R.string.Settings_preferences), "", null, true));

//        if (AuthManager.isFingerPrintAvailableAndSetup(this)) {
//            items.add(new BRSettingsItem(getString(R.string.Settings_touchIdLimit_android), "", new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    AuthManager.getInstance().authPrompt(SettingsActivity.this, null, getString(R.string.VerifyPin_continueBody), true, false, new BRAuthCompletion() {
//                        @Override
//                        public void onComplete() {
//                            Intent intent = new Intent(SettingsActivity.this, SpendLimitActivity.class);
//                            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
//                            startActivity(intent);
//                        }
//                        @Override
//                        public void onCancel() { }
//                    });
//                }
//            }, false));
//        }

        items.add(new BRSettingsItem(getString(R.string.Settings_updatePin), "", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, UpdatePinActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        }, false));

        items.add(new BRSettingsItem(getString(R.string.Settings_currency), BRSharedPrefs.getPreferredFiatIso(this), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, DisplayCurrencyActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        }, false));

        items.add(new BRSettingsItem(getString(R.string.Settings_ipfsgateway), BRSharedPrefs.getPreferredIPFSGateway(this), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, IPFSGatewayActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        }, false));

        items.add(new BRSettingsItem(getString(R.string.Settings_currencySettings), "", null, true));

        items.add(new BRSettingsItem(getString(R.string.Settings_redeem_private_key), "", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                Intent intent = new Intent(SettingsActivity.this, ImportActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        }, false));

        items.add(new BRSettingsItem(getString(R.string.Settings_rescan_blockchain), "", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                Log.d("CurrencySettings", "Rescan tapped!");

                Intent intent = new Intent(SettingsActivity.this, SyncBlockchainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        }, false));


        items.add(new BRSettingsItem(getString(R.string.assets), "", null, true));

        items.add(new BRSettingsItem(getString(R.string.manage_assets), "", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ManageAssetsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        }, false));


        items.add(new BRSettingsItem(getString(R.string.Settings_other), "", null, true));

        items.add(new BRSettingsItem(getString(R.string.Settings_shareData), BRSharedPrefs.getShareData(this) ? "ON" : "OFF", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ShareDataActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        }, false));

        items.add(new BRSettingsItem(getString(R.string.Settings_review), "", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent appStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.ravenwallet"));
                    appStoreIntent.setPackage("com.android.vending");

                    startActivity(appStoreIntent);
                } catch (android.content.ActivityNotFoundException exception) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.ravenwallet")));
                }
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        }, false));

        items.add(new BRSettingsItem(getString(R.string.Settings_aboutRaven), "", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        }, false));

        items.add(new BRSettingsItem(getString(R.string.Settings_advancedTitle), "", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, AdvancedActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        }, false));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }
}
