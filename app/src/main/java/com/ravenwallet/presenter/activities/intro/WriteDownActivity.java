package com.ravenwallet.presenter.activities.intro;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ravenwallet.R;
import com.ravenwallet.presenter.activities.HomeActivity;
import com.ravenwallet.presenter.activities.util.BRActivity;
import com.ravenwallet.presenter.entities.BRSettingsItem;
import com.ravenwallet.presenter.entities.IPFSGateway;
import com.ravenwallet.presenter.interfaces.BRAuthCompletion;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.manager.BRSharedPrefs;
import com.ravenwallet.tools.manager.FontManager;
import com.ravenwallet.tools.security.AuthManager;
import com.ravenwallet.tools.security.PostAuth;
import com.ravenwallet.tools.util.BRConstants;
import com.ravenwallet.tools.util.Bip39Wordlist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.ravenwallet.R.layout.settings_list_item;
import static com.ravenwallet.R.layout.settings_list_section;

public class WriteDownActivity extends BRActivity {
    private static final String TAG = WriteDownActivity.class.getName();
    private Button writeButton;
    private ImageButton close;
    private ListView languageList;
    public static boolean appVisible = false;
    private static WriteDownActivity app;

    private MnemonicLanguageListAdapter languageOptions;
    private Bip39Wordlist selectedLanguage;

    public static WriteDownActivity getApp() {
        return app;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_down);

        selectedLanguage = Bip39Wordlist.getWordlistForLocale();

        writeButton = findViewById(R.id.button_write_down);
        close = findViewById(R.id.close_button);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
        ImageButton faq = findViewById(R.id.faq_button);
        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                BRAnimator.showSupportFragment(app, BRConstants.paperKey);
            }
        });
        languageList = findViewById(R.id.language_list);
        languageOptions = new MnemonicLanguageListAdapter(this, R.layout.gateway_list_item);
        languageList.setAdapter(languageOptions);
        languageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bip39Wordlist newLanguage = languageOptions.getItem(i);
                languageOptions.setSelectedLanguage(newLanguage);
                languageOptions.notifyDataSetChanged();
            }
        });

        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                AuthManager.getInstance().authPrompt(WriteDownActivity.this, null, getString(R.string.VerifyPin_continueBody), true, false, new BRAuthCompletion() {
                    @Override
                    public void onComplete() {
                        PostAuth.getInstance().onPhraseCheckAuth(WriteDownActivity.this, false, languageOptions.selectedLanguage.getLanguageCode());
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarGradiant(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            Drawable background = activity.getResources().getDrawable(R.drawable.gradient_blue);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
            window.setNavigationBarColor(activity.getResources().getColor(android.R.color.transparent));

            final int lFlags = window.getDecorView().getSystemUiVisibility();
            // update the SystemUiVisibility depending on whether we want a Light or Dark theme.
            window.getDecorView().setSystemUiVisibility((lFlags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));

            window.setBackgroundDrawable(background);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        app = this;
        setStatusBarGradiant(app);
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            close();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    private void close() {
        Log.e(TAG, "close: ");
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
       startActivity(intent);
       overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
        if (!isDestroyed())
            finish();
    }


    public class MnemonicLanguageListAdapter extends ArrayAdapter<Bip39Wordlist> {

        private Context mContext;
        int layoutResourceId;

        Bip39Wordlist selectedLanguage;

        public MnemonicLanguageListAdapter(@NonNull Context context, @LayoutRes int resource) {
            super(context, resource);
            this.selectedLanguage = Bip39Wordlist.getWordlistForLocale();
            this.mContext = context;
            this.layoutResourceId = resource;
            this.addAll(Bip39Wordlist.LANGS);
        }

        public Bip39Wordlist getSelectedLanguage() {
            return selectedLanguage;
        }

        public void setSelectedLanguage(Bip39Wordlist selectedLanguage) {
            this.selectedLanguage = selectedLanguage;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Bip39Wordlist wordlist = getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }
            TextView textViewItem = convertView.findViewById(R.id.gateway_item_name);
            FontManager.overrideFonts(textViewItem);
            textViewItem.setText(String.format("%s [%s]", wordlist.getLanguageName(), wordlist.getLanguageCode()));
            ImageView checkMark = convertView.findViewById(R.id.gateway_checkmark);

            if (wordlist.getLanguageCode().equals(selectedLanguage.getLanguageCode())) {
                checkMark.setVisibility(View.VISIBLE);
            } else {
                checkMark.setVisibility(View.GONE);
            }

            return convertView;
        }

        @Override
        public int getCount() {
            return super.getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }
    }
}
