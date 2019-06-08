package com.electraproject.presenter.activities.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.electraproject.R;
import com.electraproject.presenter.activities.util.BRActivity;
import com.electraproject.presenter.customviews.BRDialogView;
import com.electraproject.tools.animation.UiUtils;
import com.electraproject.tools.animation.BRDialog;
import com.electraproject.tools.manager.BRSharedPrefs;
import com.electraproject.tools.threads.executor.BRExecutor;
import com.electraproject.wallet.WalletsMaster;


public class SyncBlockchainActivity extends BRActivity {
    private static final String TAG = SyncBlockchainActivity.class.getName();
    private Button mRescanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_blockchain);

        mRescanButton = findViewById(R.id.button_scan);
        mRescanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UiUtils.isClickAllowed()) {
                    return;
                }
                BRDialog.showCustomDialog(SyncBlockchainActivity.this, getString(R.string.ReScan_alertTitle),
                        getString(R.string.ReScan_footer), getString(R.string.ReScan_alertAction), getString(R.string.Button_cancel),
                        new BRDialogView.BROnClickListener() {
                            @Override
                            public void onClick(BRDialogView brDialogView) {
                                brDialogView.dismissWithAnimation();
                                rescanClicked();
                            }
                        }, new BRDialogView.BROnClickListener() {
                            @Override
                            public void onClick(BRDialogView brDialogView) {
                                brDialogView.dismissWithAnimation();
                            }
                        }, null, 0);

            }
        });

    }

    private void rescanClicked() {
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                Activity thisApp = SyncBlockchainActivity.this;
                BRSharedPrefs.putStartHeight(thisApp, BRSharedPrefs.getCurrentWalletCurrencyCode(thisApp), 0);
                BRSharedPrefs.putAllowSpend(thisApp, BRSharedPrefs.getCurrentWalletCurrencyCode(thisApp), false);
                WalletsMaster.getInstance(thisApp).getCurrentWallet(thisApp).rescan(thisApp);
                UiUtils.startBreadActivity(thisApp, false);

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

}
