package com.ravenwallet.presenter.newTutorial;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.ravenwallet.R;
import com.ravenwallet.presenter.activities.TermsAndConditionsActivity;


public class TutorialActivity extends FragmentActivity {

    public static final String REPLAY_TUTO = "replayTuto";
    private static final int NUM_PAGES = 5;
    private boolean isReplay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        // Instantiate a ViewPager and a PagerAdapter.
        final ViewPager mPager = findViewById(R.id.view_pager);
        PagerAdapter mPagerAdapter = new CustomPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        isReplay = getIntent().getBooleanExtra(REPLAY_TUTO, false);
        TextView skip = findViewById(R.id.skip);
        final TextView nextOrFinish = findViewById(R.id.next);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == NUM_PAGES - 1) {
                    // Last page of the tutorial
                    nextOrFinish.setText(getString(R.string.finish));
                } else {
                    nextOrFinish.setText(getString(R.string.next));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeTutorial();
            }
        });
        nextOrFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPage = mPager.getCurrentItem();
                if (currentPage == NUM_PAGES - 1) {
                    // Last page of the tutorial
                    closeTutorial();
                } else {
                    // Scroll to the next page
                    mPager.setCurrentItem(currentPage + 1, true);
                }
            }
        });
    }

    private void closeTutorial() {
        if (isReplay) {
            setResult(RESULT_OK);
            finish();
        } else {
            Intent intent = new Intent(TutorialActivity.this, TermsAndConditionsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
            finishAffinity();
        }
    }

    private class CustomPagerAdapter extends FragmentStatePagerAdapter {
        CustomPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return TutorialScreenFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setStatusBarGradiant(this);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarGradiant(Activity activity) {
        Window window = activity.getWindow();
        Drawable background = activity.getResources().getDrawable(R.drawable.regular_blue);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setNavigationBarColor(activity.getResources().getColor(android.R.color.transparent));

        final int lFlags = window.getDecorView().getSystemUiVisibility();
        // update the SystemUiVisibility depending on whether we want a Light or Dark theme.
        window.getDecorView().setSystemUiVisibility((lFlags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));

        window.setBackgroundDrawable(background);

    }
}
