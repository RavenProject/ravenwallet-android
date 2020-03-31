package com.ravenwallet.presenter.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.platform.addressBook.AddressBookItem;
import com.platform.addressBook.AddressBookRepository;
import com.platform.addressBook.adapter.AddressBookAdapter;
import com.platform.addressBook.adapter.CustomDividerItemDecoration;
import com.platform.addressBook.event.OnAddressClickedListener;
import com.platform.addressBook.event.OnAddressSavedListener;
import com.ravenwallet.R;
import com.ravenwallet.core.BRCoreAddress;
import com.ravenwallet.presenter.activities.util.BRActivity;
import com.ravenwallet.tools.animation.BRAnimator;

import java.util.ArrayList;
import java.util.List;

public class AddressBookActivity extends BRActivity implements OnAddressClickedListener, OnAddressSavedListener {
    private static final String TAG = AddressBookActivity.class.getName();

    public RecyclerView mAddressBookList;
    public RelativeLayout layout;
    public List<AddressBookItem> addresses;

    public final static String PICK_ADDRESS_VIEW_EXTRAS_KEY = "pick.address.view.extras.key";
    private boolean isPickAddressView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_book);

        setStatusBarGradient(this);

        if (getIntent().getExtras() != null) {
            isPickAddressView = getIntent().getExtras().getBoolean(PICK_ADDRESS_VIEW_EXTRAS_KEY);
        }

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

        FloatingActionButton addAddressButton = findViewById(R.id.add_address_button);
        addAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BRAnimator.showAddressAddingFragment(AddressBookActivity.this);
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
        overridePendingTransition(R.anim.fade_up, R.anim.exit_to_bottom);
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
        AddressBookRepository repository = AddressBookRepository.getInstance(this);
        List<AddressBookItem> addresses = repository.getAllAddresses();

        if (addresses.size() > 0) {
            mAddressBookList.setVisibility(View.VISIBLE);
            mAddressBookList.setAdapter(new AddressBookAdapter(addresses, this));
            mAddressBookList.addItemDecoration(new CustomDividerItemDecoration(this));
        } else {
            mAddressBookList.setVisibility(View.GONE);
            TextView noAddressesText = findViewById(R.id.no_data_text);
            noAddressesText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAddressClicked(AddressBookItem address) {
        if (isPickAddressView) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", address);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        } else {
            BRAnimator.showSendFragment(this, null, true, address);
        }
    }

    @Override
    public void onAddressClicked(BRCoreAddress address) {

    }

    @Override
    public void onAddressSaved() {
        setAddresses();
    }
}
