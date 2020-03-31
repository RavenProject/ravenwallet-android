package com.ravenwallet.presenter.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.platform.assets.Asset;
import com.platform.assets.AssetsRepository;
import com.platform.assets.adapter.AssetsAdapter;
import com.ravenwallet.R;
import com.ravenwallet.presenter.AssetChangeListener;
import com.ravenwallet.presenter.activities.util.BRActivity;
import com.ravenwallet.presenter.activities.util.CustomItemTouchHelperCallback;
import com.ravenwallet.presenter.customviews.BRText;
import com.ravenwallet.tools.adapter.ManageAssetsAdapter;
import com.ravenwallet.tools.animation.BRAnimator;

import java.util.ArrayList;
import java.util.List;

public class ManageAssetsActivity extends BRActivity implements AssetChangeListener {
    private static final String TAG = ManageAssetsActivity.class.getName();

    public RecyclerView mAssetsList;
    public RelativeLayout layout;
    private TextView noAssetsText;
    public List<Asset> assets;
    private AssetsRepository repository;
    private BRText mTitle;
    public final static String IS_OWNED_ASSETS_VIEW_EXTRAS_KEY = "owned.assets.view.extras.key";
    private boolean isOwnedAssetsView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_assets);

        if (getIntent().getExtras() != null) {
            isOwnedAssetsView = getIntent().getExtras().getBoolean(IS_OWNED_ASSETS_VIEW_EXTRAS_KEY);
        }

        setStatusBarGradient(this);

        assets = new ArrayList<>();
        mAssetsList = findViewById(R.id.asset_list);
        noAssetsText = findViewById(R.id.no_data_text);

        mTitle = findViewById(R.id.title);
        ImageButton back = findViewById(R.id.back_button);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                onBackPressed();
            }
        });
        repository = AssetsRepository.getInstance(this);
        repository.addListener(this);
        setAssets();

        if (isOwnedAssetsView)
            mTitle.setText("Assets");
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarGradient(Activity activity) {
        Window window = activity.getWindow();
        Drawable background = ContextCompat.getDrawable(activity, R.drawable.gradient_blue);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(activity, android.R.color.transparent));
        window.setNavigationBarColor(ContextCompat.getColor(activity, android.R.color.transparent));

        final int lFlags = window.getDecorView().getSystemUiVisibility();
        // update the SystemUiVisibility depending on whether we want a Light or Dark theme.
        window.getDecorView().setSystemUiVisibility((lFlags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));

        window.setBackgroundDrawable(background);
    }

    @Override
    protected void onDestroy() {
        repository.removeListener(this);
        super.onDestroy();
    }

    private void setAssets() {
        List<Asset> assets = isOwnedAssetsView ? repository.getVisibleAssets() : repository.getAllAssets();
        if (assets.size() > 0) {
            noAssetsText.setVisibility(View.GONE);
            mAssetsList.setVisibility(View.VISIBLE);
            if (isOwnedAssetsView) {
                // Add padding
                mAssetsList.setPadding(40, 0, 40, 0);

                // Creating then setting the adapter with only the first three items
                AssetsAdapter assetsAdapter = new AssetsAdapter(this, assets);
                mAssetsList.setAdapter(assetsAdapter);
            } else {
                // Set the adapter
                ManageAssetsAdapter adapter = new ManageAssetsAdapter(this, assets);
                mAssetsList.setAdapter(adapter);

                // Set the items' divider
//                mAssetsList.addItemDecoration(new CustomDividerItemDecoration(this));

                // Set the ItemTouchHelper
                ItemTouchHelper.Callback callback = new CustomItemTouchHelperCallback(adapter);
                ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                touchHelper.attachToRecyclerView(mAssetsList);
            }
        } else {
            mAssetsList.setVisibility(View.GONE);
            noAssetsText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    @Override
    public void onChange() {
        if (isOwnedAssetsView)
            setAssets();
    }
}
