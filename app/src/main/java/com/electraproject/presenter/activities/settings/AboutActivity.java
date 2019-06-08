package com.electraproject.presenter.activities.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.electraproject.BuildConfig;
import com.electraproject.R;
import com.electraproject.tools.animation.UiUtils;
import com.electraproject.tools.util.LogsUtils;
import com.electraproject.tools.util.BRConstants;

import java.util.Locale;

public class AboutActivity extends BaseSettingsActivity {
    private static final int VERSION_CLICK_COUNT_FOR_BACKDOOR = 5;
    private int mVersionClickedCount;

    @Override
    public int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    public int getBackButtonId() {
        return R.id.back_button;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView infoText = findViewById(R.id.info_text);
        TextView policyText = findViewById(R.id.policy_text);

        infoText.setText(String.format(Locale.getDefault(), getString(R.string.About_footer), BuildConfig.VERSION_NAME, BuildConfig.BUILD_VERSION));
        infoText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVersionClickedCount++;
                if (mVersionClickedCount >= VERSION_CLICK_COUNT_FOR_BACKDOOR) {
                    mVersionClickedCount = 0;
                    LogsUtils.shareLogs(AboutActivity.this);
                }
            }
        });

        ImageView telegramShare = findViewById(R.id.telegram_share_button);
        ImageView twitterShare = findViewById(R.id.twitter_share_button);
        ImageView discordShare = findViewById(R.id.discord_share_button);
        ImageView websiteShare = findViewById(R.id.website_share_button);

        websiteShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BRConstants.URL_WEBSITE));
                startActivity(browserIntent);
                AboutActivity.this.overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
            }
        });
        telegramShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BRConstants.URL_TELEGRAM));
                startActivity(browserIntent);
                AboutActivity.this.overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
            }
        });
        twitterShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BRConstants.URL_TWITTER));
                startActivity(browserIntent);
                AboutActivity.this.overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
            }
        });
        discordShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BRConstants.URL_DISCORD));
                startActivity(browserIntent);
                AboutActivity.this.overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
            }
        });
        policyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BRConstants.URL_PRIVACY_POLICY));
                startActivity(browserIntent);
                AboutActivity.this.overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mVersionClickedCount = 0;
    }

    @Override
    public void onBackPressed() {
        if (UiUtils.isLast(this)) {
            UiUtils.startBreadActivity(this, false);
        } else {
            super.onBackPressed();
        }
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }


}
