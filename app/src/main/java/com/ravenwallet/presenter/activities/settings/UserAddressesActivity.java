package com.ravenwallet.presenter.activities.settings;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.platform.addressBook.AddressBookItem;
import com.platform.addressBook.adapter.CustomDividerItemDecoration;
import com.platform.addressBook.event.OnAddressClickedListener;
import com.ravenwallet.R;
import com.ravenwallet.core.BRCoreAddress;
import com.ravenwallet.presenter.activities.util.BRActivity;
import com.ravenwallet.tools.animation.BRAnimator;
import com.ravenwallet.wallet.RvnWalletManager;

import java.util.ArrayList;
import java.util.List;

public class UserAddressesActivity extends BRActivity {
    private static final String TAG = UserAddressesActivity.class.getName();

    public RecyclerView mAddressBookList;
    public RelativeLayout layout;
    public List<AddressBookItem> addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_addresses);

        setStatusBarGradient(this);

        addresses = new ArrayList<>();
        mAddressBookList = findViewById(R.id.address_book_list);
        ImageButton close = findViewById(R.id.close_button);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                onBackPressed();
            }
        });

        setAddresses();
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
    public void onBackPressed() {
        super.onBackPressed();
//        int c = getFragmentManager().getBackStackEntryCount();
//        if (c > 0) {
//            super.onBackPressed();
//            return;
//        }
//        Intent intent = new Intent(this, HomeActivity.class);
//        startActivity(intent);
//        overridePendingTransition(R.anim.fade_up, R.anim.exit_to_bottom);
//        if (!isDestroyed()) {
//            finish();
//        }
    }

    private void setAddresses() {
        RvnWalletManager walletManager = RvnWalletManager.getInstance(this);
        BRCoreAddress[] addresses = walletManager.getWallet().getAllAddresses();
        if (addresses.length > 0) {
            mAddressBookList.setVisibility(View.VISIBLE);
            mAddressBookList.setAdapter(new UserAddressesAdapter(addresses, new OnAddressClickedListener() {
                @Override
                public void onAddressClicked(AddressBookItem address) {

                }

                @Override
                public void onAddressClicked(BRCoreAddress address) {
                    BRAnimator.showReceiveFragment(UserAddressesActivity.this, false, address.stringify());
                }
            }));
            mAddressBookList.addItemDecoration(new CustomDividerItemDecoration(this));
        } else {
            mAddressBookList.setVisibility(View.GONE);
            TextView noAddressesText = findViewById(R.id.no_data_text);
            noAddressesText.setVisibility(View.VISIBLE);
        }
    }
}
