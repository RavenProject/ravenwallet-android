package com.ravenwallet.presenter.activities.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.ravenwallet.R;
import com.ravenwallet.presenter.activities.intro.IntroActivity;
import com.ravenwallet.presenter.activities.util.BRActivity;
import com.ravenwallet.presenter.customviews.BRDialogView;
import com.ravenwallet.presenter.entities.BRSettingsItem;
import com.ravenwallet.tools.animation.BRDialog;
import com.ravenwallet.tools.manager.BRSharedPrefs;
import com.ravenwallet.wallet.WalletsMaster;

import java.util.ArrayList;
import java.util.List;

import static com.ravenwallet.R.layout.settings_list_item;
import static com.ravenwallet.R.layout.settings_list_section;
import static com.ravenwallet.R.layout.settings_list_toggle;

public class AdvancedActivity extends BRActivity {
    private static final String TAG = AdvancedActivity.class.getName();
    private ListView listView;
    public List<BRSettingsItem> items;


    private ImageButton mBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced);

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
                if (item.title != null && item.title.equals(getString(R.string.expert_mode))) {
                    v = inflater.inflate(settings_list_toggle, parent, false);
                    final SwitchCompat toggleButton = v.findViewById(R.id.toggle);
                    boolean expertMode = BRSharedPrefs.getExpertMode(getContext());
                    toggleButton.setChecked(expertMode);
                    toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                items.add(3, new BRSettingsItem(getString(R.string.show_address_input), "",
                                        null, false));
                                items.add(4, new BRSettingsItem(getString(R.string.used_address), "",
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(AdvancedActivity.this, UserAddressesActivity.class);
                                                startActivity(intent);
                                                overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
                                            }
                                        }, false));

                                items.add(5, new BRSettingsItem(getString(R.string.reset_title), "", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        BRDialog.showCustomDialog(
                                                AdvancedActivity.this,
                                                getString(R.string.wipe_wallet_warning_title),
                                                getString(R.string.wipe_wallet_warning_message),
                                                getString(R.string.wipe),
                                                getString(R.string.cancel),
                                                new BRDialogView.BROnClickListener() {
                                                    @Override
                                                    public void onClick(BRDialogView brDialogView) {
                                                        brDialogView.dismissWithAnimation();
                                                        WalletsMaster m = WalletsMaster.getInstance(AdvancedActivity.this);
                                                        m.wipeAll(AdvancedActivity.this);
//                                                        m.wipeKeyStore(app);
                                                        Intent intent = new Intent(AdvancedActivity.this, IntroActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        finalizeIntent(intent);
                                                    }
                                                },
                                                new BRDialogView.BROnClickListener() {
                                                    @Override
                                                    public void onClick(BRDialogView brDialogView) {
                                                        brDialogView.dismissWithAnimation();
                                                    }
                                                },
                                                null,
                                                0, true
                                        );
                                    }
                                }, false));
                            } else {
                                List<BRSettingsItem> removeList = new ArrayList<>();
                                for (BRSettingsItem item1 : items) {
                                    if (item1.title != null &&
                                            (item1.title.equals(getString(R.string.reset_title)) ||
                                                    item1.title.equals(getString(R.string.used_address))||
                                                    item1.title.equals(getString(R.string.show_address_input)))) {
                                        removeList.add(item1);
                                    }
                                }
                                if (!removeList.isEmpty())
                                    items.removeAll(removeList);
                            }
                            notifyDataSetChanged();
                            BRSharedPrefs.putExpertMode(getContext(), isChecked);

                        }
                    });
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            toggleButton.setChecked(!toggleButton.isChecked());

                        }
                    });
                } else if (item.title != null && item.title.equals(getString(R.string.show_address_input))) {
                    v = inflater.inflate(settings_list_toggle, parent, false);
                    final SwitchCompat toggleButton = v.findViewById(R.id.toggle);
                    boolean showAddressInput = BRSharedPrefs.getShowAddressInput(getContext());
                    toggleButton.setChecked(showAddressInput);
                    toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            BRSharedPrefs.putShowAddressInput(getContext(), isChecked);
                        }
                    });
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            toggleButton.setChecked(!toggleButton.isChecked());

                        }
                    });
                } else {
                    v = inflater.inflate(settings_list_item, parent, false);
                    TextView addon = v.findViewById(R.id.item_addon);
                    addon.setText(item.addonText);
                    v.setOnClickListener(item.listener);
                }
            }

            TextView title = v.findViewById(R.id.item_title);
            if (!item.isSection)
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

        items.add(new BRSettingsItem("", "", null, true));

        items.add(new BRSettingsItem(getString(R.string.RavenNodeSelector_title), "", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdvancedActivity.this, NodesActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.empty_300);

            }
        }, false));

        items.add(new BRSettingsItem(getString(R.string.expert_mode), "",
                null, false));

        boolean expertMode = BRSharedPrefs.getExpertMode(this);
        if (expertMode) {
            items.add(new BRSettingsItem(getString(R.string.show_address_input), "",
                    null, false));
            items.add(new BRSettingsItem(getString(R.string.used_address), "",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(AdvancedActivity.this, UserAddressesActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
                        }
                    }, false));

            items.add(new BRSettingsItem(getString(R.string.reset_title), "", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BRDialog.showCustomDialog(
                            AdvancedActivity.this,
                            getString(R.string.wipe_wallet_warning_title),
                            getString(R.string.wipe_wallet_warning_message),
                            getString(R.string.wipe),
                            getString(R.string.cancel),
                            new BRDialogView.BROnClickListener() {
                                @Override
                                public void onClick(BRDialogView brDialogView) {
                                    brDialogView.dismissWithAnimation();
                                    WalletsMaster m = WalletsMaster.getInstance(AdvancedActivity.this);
                                    m.wipeWalletButKeystore(AdvancedActivity.this);
                                    m.wipeKeyStore(AdvancedActivity.this);
                                    Intent intent = new Intent(AdvancedActivity.this, IntroActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    finalizeIntent(intent);
                                }
                            },
                            new BRDialogView.BROnClickListener() {
                                @Override
                                public void onClick(BRDialogView brDialogView) {
                                    brDialogView.dismissWithAnimation();
                                }
                            },
                            null,
                            0, true
                    );
                }
            }, false));

        }


    }

}
