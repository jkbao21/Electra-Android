
package com.electraproject.presenter.activities.intro;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;

import com.electraproject.BuildConfig;
import com.electraproject.R;
import com.electraproject.presenter.activities.util.BRActivity;
import com.electraproject.presenter.customviews.BRButton;
import com.electraproject.presenter.customviews.BaseTextView;
import com.electraproject.tools.animation.UiUtils;
import com.electraproject.tools.util.ServerBundlesHelper;
import com.electraproject.tools.util.EventUtils;
import com.electraproject.tools.security.PostAuth;
import com.electraproject.tools.threads.executor.BRExecutor;
import com.electraproject.tools.util.Utils;
import com.platform.APIClient;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 8/4/15.
 * Copyright (c) 2016 breadwallet LLC
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class IntroTos extends BRActivity {
    private static final String TAG = IntroActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_tos);
        setOnClickListeners();
        BaseTextView baseTextView = (BaseTextView) findViewById(R.id.texttos);
        baseTextView.setMovementMethod(new ScrollingMovementMethod());
        updateBundles();

        if (BuildConfig.DEBUG) {
            Utils.printPhoneSpecs(this);
        }

        PostAuth.getInstance().onCanaryCheck(IntroTos.this, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventUtils.pushEvent(EventUtils.EVENT_LANDING_PAGE_APPEARED);
    }

    private void updateBundles() {
        BRExecutor.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                ServerBundlesHelper.extractBundlesIfNeeded(getApplicationContext());
                final long startTime = System.currentTimeMillis();
                APIClient apiClient = APIClient.getInstance(getApplicationContext());
                apiClient.updateBundle();
                long endTime = System.currentTimeMillis();
                Log.d(TAG, "updateBundle DONE in " + (endTime - startTime) + "ms");
            }
        });
    }

    private void setOnClickListeners() {
        BRButton buttonNewWallet = findViewById(R.id.introagree);
        BRButton buttonRecoverWallet = findViewById(R.id.introdecline);
        buttonNewWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UiUtils.isClickAllowed()) {
                    return;
                }
                EventUtils.pushEvent(EventUtils.EVENT_LANDING_PAGE_GET_STARTED);
                Intent intent = new Intent(IntroTos.this, OnBoardingActivity.class);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                startActivity(intent);
            }
        });

        buttonRecoverWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UiUtils.isClickAllowed()) {
                    return;
                }
                EventUtils.pushEvent(EventUtils.EVENT_LANDING_PAGE_RESTORE_WALLET);
                Intent intent = new Intent(IntroTos.this, IntroActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        });
    }

}
