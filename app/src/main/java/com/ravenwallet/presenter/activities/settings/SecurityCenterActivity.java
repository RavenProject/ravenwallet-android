package com.ravenwallet.presenter.activities.settings;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ravenwallet.R;
import com.ravenwallet.presenter.activities.util.ActivityUTILS;
import com.ravenwallet.presenter.activities.intro.WriteDownActivity;
import com.ravenwallet.presenter.activities.UpdatePinActivity;
import com.ravenwallet.presenter.activities.util.BRActivity;
import com.ravenwallet.presenter.entities.BRSecurityCenterItem;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.tools.manager.BRSharedPrefs;
import com.ravenwallet.tools.security.BRKeyStore;
import com.ravenwallet.tools.util.BRConstants;
import com.ravenwallet.tools.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class SecurityCenterActivity extends BRActivity {
    private static final String TAG = SecurityCenterActivity.class.getName();

    public ListView mListView;
    public RelativeLayout layout;
    public List<BRSecurityCenterItem> itemList;
    public static boolean appVisible = false;
    private static SecurityCenterActivity app;
    private ImageButton close;

    public static SecurityCenterActivity getApp() {
        return app;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_center);

        itemList = new ArrayList<>();
        mListView = (ListView) findViewById(R.id.menu_listview);
        mListView.addFooterView(new View(this), null, true);
        mListView.addHeaderView(new View(this), null, true);
        mListView.setVerticalScrollBarEnabled(false);
        mListView.setClickable(false);
        close = (ImageButton) findViewById(R.id.close_button);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                onBackPressed();
            }
        });

        updateList();

        ImageButton faq = (ImageButton) findViewById(R.id.faq_button);

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                BRAnimator.showSupportFragment(app, BRConstants.securityCenter);
            }
        });

    }

    @Deprecated
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void changeStatusBarColor() {
        Window window = app.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(app, R.color.primaryColor));

        final int lFlags = window.getDecorView().getSystemUiVisibility();
        // update the SystemUiVisibility depending on whether we want a Light or Dark theme.
        window.getDecorView().setSystemUiVisibility((lFlags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));
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
        updateList();
        appVisible = true;
        app = this;

//        changeStatusBarColor();
        setStatusBarGradiant(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (ActivityUTILS.isLast(this)) {
            BRAnimator.startRvnActivity(this, false);
        } else {
            super.onBackPressed();
        }
        overridePendingTransition(R.anim.fade_up, R.anim.exit_to_bottom);
    }

    public RelativeLayout getMainLayout() {
        return layout;
    }

    public class SecurityCenterListAdapter extends ArrayAdapter<BRSecurityCenterItem> {

        private List<BRSecurityCenterItem> items;
        private Context mContext;
        private int defaultLayoutResource = R.layout.security_center_list_item;

        public SecurityCenterListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<BRSecurityCenterItem> items) {
            super(context, resource);
            this.items = items;
            this.mContext = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

//            Log.e(TAG, "getView: pos: " + position + ", item: " + items.get(position));
            if (convertView == null) {
                // inflate the layout
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(defaultLayoutResource, parent, false);
            }
            TextView title = (TextView) convertView.findViewById(R.id.item_title);
            TextView text = (TextView) convertView.findViewById(R.id.item_text);
            ImageView checkMark = (ImageView) convertView.findViewById(R.id.check_mark);

            title.setText(items.get(position).title);
            text.setText(items.get(position).text);
            checkMark.setImageResource(items.get(position).checkMarkResId);
            convertView.setOnClickListener(items.get(position).listener);
            return convertView;

        }

        @Override
        public int getCount() {
            return items == null ? 0 : items.size();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        appVisible = false;
    }

    private void updateList() {
        boolean isPinSet = BRKeyStore.getPinCode(this).length() == 6;
        itemList.clear();
        itemList.add(new BRSecurityCenterItem(getString(R.string.SecurityCenter_pinTitle), getString(R.string.SecurityCenter_pinDescription),
                isPinSet ? R.drawable.ic_check_mark_blue : R.drawable.ic_check_mark_grey, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecurityCenterActivity.this, UpdatePinActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        }));

        int resId = Utils.isFingerprintEnrolled(SecurityCenterActivity.this)
                && BRSharedPrefs.getUseFingerprint(SecurityCenterActivity.this)
                ? R.drawable.ic_check_mark_blue
                : R.drawable.ic_check_mark_grey;

        if (Utils.isFingerprintAvailable(this)) {
            itemList.add(new BRSecurityCenterItem(getString(R.string.SecurityCenter_touchIdTitle_android), getString(R.string.SecurityCenter_touchIdDescription),
                    resId, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SecurityCenterActivity.this, FingerprintActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                }
            }));
        }

        boolean isPaperKeySet = BRSharedPrefs.getPhraseWroteDown(this);
        itemList.add(new BRSecurityCenterItem(getString(R.string.SecurityCenter_paperKeyTitle), getString(R.string.SecurityCenter_paperKeyDescription),
                isPaperKeySet ? R.drawable.ic_check_mark_blue : R.drawable.ic_check_mark_grey, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecurityCenterActivity.this, WriteDownActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_bottom, R.anim.fade_down);
            }
        }));

        mListView.setAdapter(new SecurityCenterListAdapter(this, R.layout.menu_list_item, itemList));
    }
}
