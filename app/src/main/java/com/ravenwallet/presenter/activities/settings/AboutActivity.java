package com.ravenwallet.presenter.activities.settings;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ravenwallet.R;
import com.ravenwallet.presenter.activities.util.ActivityUTILS;
import com.ravenwallet.presenter.activities.util.BRActivity;
import com.ravenwallet.tools.animation.BRAnimator;

import java.util.Locale;

import static com.ravenwallet.tools.util.BRConstants.PRIVACY_URL;

public class AboutActivity extends BRActivity {
    private static final String TAG = AboutActivity.class.getName();
//    private TextView termsText;
    private TextView privacy;
    private TextView infoText;

    private ImageView reddit;
    private ImageView twitter;
    private ImageView wiki;
    private ImageView site;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        infoText = (TextView) findViewById(R.id.info_text);
//        termsText = (TextView) findViewById(R.id.terms_text);
        privacy = (TextView) findViewById(R.id.policy_text);

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int verCode = pInfo != null ? pInfo.versionCode : 0;

        infoText.setText(String.format(Locale.getDefault(), getString(R.string.About_footer), verCode));

        reddit = (ImageView) findViewById(R.id.reddit_share_button);
        twitter = (ImageView) findViewById(R.id.twitter_share_button);
        wiki = (ImageView) findViewById(R.id.wiki_share_button);
        site = (ImageView) findViewById(R.id.site_share_button);

        reddit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://reddit.com/r/ravenwallet/"));
                startActivity(browserIntent);
                overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
            }
        });

        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/ravenwallet"));
                startActivity(browserIntent);
                overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
            }
        });
        wiki.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://raven.wiki"));
                startActivity(browserIntent);
                overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
            }
        });
        site.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://ravenwallet.com"));
                startActivity(browserIntent);
                overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
            }
        });
        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_URL));
                startActivity(browserIntent);
                overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (ActivityUTILS.isLast(this)) {
            BRAnimator.startRvnActivity(this, false);
        } else {
            super.onBackPressed();
        }
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }
}
