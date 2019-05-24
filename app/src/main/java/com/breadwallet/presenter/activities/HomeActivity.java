/**
 * BreadWallet
 * <p/>
 * Created by byfieldj on <jade@breadwallet.com> 1/17/18.
 * Copyright (c) 2019 breadwallet LLC
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

package com.breadwallet.presenter.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ViewFlipper;

import com.breadwallet.R;
import com.breadwallet.model.Wallet;
import com.breadwallet.presenter.activities.settings.SettingsActivity;
import com.breadwallet.presenter.activities.util.BRActivity;
import com.breadwallet.presenter.customviews.BRButton;
import com.breadwallet.presenter.customviews.BRNotificationBar;
import com.breadwallet.presenter.customviews.BRSearchBar;
import com.breadwallet.presenter.customviews.BaseTextView;
import com.breadwallet.presenter.entities.CryptoRequest;
import com.breadwallet.presenter.fragments.FragmentSend;
import com.breadwallet.presenter.viewmodels.MainViewModel;
import com.breadwallet.tools.adapter.WalletListAdapter;
import com.breadwallet.tools.animation.BRDialog;
import com.breadwallet.tools.animation.UiUtils;
import com.breadwallet.tools.manager.AppEntryPointHandler;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.manager.InternetManager;
import com.breadwallet.tools.manager.TxManager;
import com.breadwallet.tools.services.SyncService;
import com.breadwallet.tools.sqlite.RatesDataSource;
import com.breadwallet.tools.threads.executor.BRExecutor;
import com.breadwallet.tools.util.CurrencyUtils;
import com.breadwallet.tools.util.Utils;
import com.breadwallet.wallet.WalletsMaster;
import com.breadwallet.wallet.abstracts.BalanceUpdateListener;
import com.breadwallet.wallet.abstracts.BaseWalletManager;
import com.breadwallet.wallet.abstracts.OnTxListModified;
import com.breadwallet.wallet.abstracts.SyncListener;
import com.breadwallet.wallet.wallets.bitcoin.BaseBitcoinWalletManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by byfieldj on 1/17/18.
 * <p>
 * Home activity that will show a list of a user's wallets
 */

public class HomeActivity extends BRActivity implements InternetManager.ConnectionReceiverListener,
        OnTxListModified, RatesDataSource.OnDataChanged, SyncListener, BalanceUpdateListener {
    private static final String TAG = HomeActivity.class.getSimpleName();
    private static final String URBAN_APP_PACKAGE_NAME = "com.urbandroid.lux";
    public static final String EXTRA_DATA = "com.breadwallet.presenter.activities.HomeActivity.EXTRA_DATA";

    private static final String SYNCED_THROUGH_DATE_FORMAT = "MM/dd/yy HH:mm";
    private static final float SYNC_PROGRESS_LAYOUT_ANIMATION_ALPHA = 0.0f;

    public static final String EXTRA_CRYPTO_REQUEST = "com.breadwallet.presenter.activities.HomeActivity.EXTRA_CRYPTO_REQUEST";
    private static final int SEND_SHOW_DELAY = 300;

    private BRNotificationBar mNotificationBar;
    private LinearLayout mMenuLayout;
    private MainViewModel mViewModel;
    private BRButton mSendButton;
    private BRButton mReceiveButton;
    private BaseTextView mCurrencyPriceUsd;
    private BaseTextView mBalancePrimary;
    private BaseTextView mBalanceSecondary;
    private BRSearchBar mSearchBar;
    public ViewFlipper mBarFlipper;
    private LinearLayout mProgressLayout;
    private BaseTextView mSyncStatusLabel;
    private BaseTextView mProgressLabel;
    private ProgressBar mSyncProgress;

    private SyncNotificationBroadcastReceiver mSyncNotificationBroadcastReceiver;
    private String mCurrentWalletIso;

    private BaseWalletManager mWallet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        BRSharedPrefs.putIsNewWallet(this, false);

        mCurrencyPriceUsd = findViewById(R.id.currency_usd_price);
        mBalancePrimary = findViewById(R.id.balance_primary);
        mBalanceSecondary = findViewById(R.id.balance_secondary);
        mProgressLayout = findViewById(R.id.progress_layout);
        mSyncStatusLabel = findViewById(R.id.sync_status_label);
        mProgressLabel = findViewById(R.id.syncing_label);
        mSyncProgress = findViewById(R.id.sync_progress);
        mNotificationBar = findViewById(R.id.notification_bar);
        mSearchBar = findViewById(R.id.search_bar);
        ImageButton searchIcon = findViewById(R.id.search_icon);
        mBarFlipper = findViewById(R.id.tool_bar_flipper);

        // Get ViewModel, observe updates to Wallet and aggregated balance data
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        mMenuLayout = findViewById(R.id.menu_layout);
        mMenuLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                intent.putExtra(SettingsActivity.EXTRA_MODE, SettingsActivity.MODE_SETTINGS);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
            }
        });

        mSendButton = findViewById(R.id.send_button);
        mSendButton.setHasShadow(false);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSendFragment(null);
            }
        });

        mReceiveButton = findViewById(R.id.receive_button);
        mReceiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UiUtils.showReceiveFragment(HomeActivity.this, true);
            }
        });

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!UiUtils.isClickAllowed()) {
                    return;
                }
                mBarFlipper.setDisplayedChild(1); //search bar
                mSearchBar.onShow(true);
            }
        });

        TxManager.getInstance().init(this);

        onConnectionChanged(InternetManager.getInstance().isConnected(this));

        updateUi();

        mWallet = WalletsMaster.getInstance(this).getCurrentWallet(this);

        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                Activity app = HomeActivity.this;
                WalletsMaster.getInstance(app).refreshBalances(app);
                if (mWallet != null) {
                    mWallet.refreshAddress(app);
                }
            }
        });

        // Check if the "Twilight" screen altering app is currently running
        if (Utils.checkIfScreenAlteringAppIsRunning(this, URBAN_APP_PACKAGE_NAME)) {
            BRDialog.showSimpleDialog(this, getString(R.string.Alert_ScreenAlteringAppDetected),
                    getString(R.string.Android_screenAlteringMessage));
        }
    }

    private void updateUi() {
        final BaseWalletManager walletManager = WalletsMaster.getInstance(this).getCurrentWallet(this);
        if (walletManager == null) {
            Log.e(TAG, "updateUi: wallet is null");
            return;
        }

        BigDecimal rate = walletManager.getFiatExchangeRate(this).setScale(8, RoundingMode.HALF_EVEN);
        String fiatBalance = CurrencyUtils.getFormattedAmount(getApplication(),
                BRSharedPrefs.getPreferredFiatIso(getApplication()), walletManager.getFiatBalance(getApplication()));
        BigDecimal cryptoBalance = convertSats(walletManager.getCachedBalance(this));

        mCurrencyPriceUsd.setText(String.format(getString(R.string.Account_exchangeRate),
                rate.toPlainString(), walletManager.getCurrencyCode()));
        mBalancePrimary.setText(fiatBalance);
        mBalanceSecondary.setText(String.format(getString(R.string.Account_balanceValue),
                cryptoBalance.toString(), walletManager.getCurrencyCode()));

        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("BG:" + TAG + ":updateTxList");
                TxManager.getInstance().updateTxList(HomeActivity.this);
            }
        });

        if (mWallet instanceof BaseBitcoinWalletManager) {
            BaseBitcoinWalletManager baseBitcoinWalletManager = (BaseBitcoinWalletManager) mWallet;
            long syncThroughDateInMillis = baseBitcoinWalletManager.getPeerManager()
                    .getLastBlockTimestamp() * DateUtils.SECOND_IN_MILLIS;
            String syncedThroughDate = new SimpleDateFormat(SYNCED_THROUGH_DATE_FORMAT,
                    Locale.getDefault()).format(syncThroughDateInMillis);
            mSyncStatusLabel.setText(String.format(getString(R.string.SyncingView_syncedThrough),
                    syncedThroughDate));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUi();
    }
    public void resetFlipper() {
        mBarFlipper.setDisplayedChild(0);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        showSendIfNeeded(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        InternetManager.registerConnectionReceiver(this, this);
        RatesDataSource.getInstance(this).addOnDataChangedListener(this);
        final BaseWalletManager wallet = WalletsMaster.getInstance(this).getCurrentWallet(this);
        if (wallet != null) {
            wallet.addTxListModifiedListener(this);
            BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    wallet.refreshCachedBalance(HomeActivity.this);
                    BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            updateUi();
                        }
                    });
                    if (wallet.getConnectStatus() != 2) {
                        wallet.connect(HomeActivity.this);
                    }

                }
            });

            wallet.addBalanceChangedListener(this);

            mCurrentWalletIso = wallet.getCurrencyCode();

            wallet.addSyncListener(this);
        }

        mSyncNotificationBroadcastReceiver = new SyncNotificationBroadcastReceiver();
        SyncService.registerSyncNotificationBroadcastReceiver(getApplicationContext(),
                mSyncNotificationBroadcastReceiver);
        SyncService.startService(getApplicationContext(), mCurrentWalletIso);

        showSendIfNeeded(getIntent());

        mViewModel.refreshWallets();
    }

    private synchronized void showSendIfNeeded(final Intent intent) {
        final CryptoRequest request = (CryptoRequest) intent.getSerializableExtra(EXTRA_CRYPTO_REQUEST);
        intent.removeExtra(EXTRA_CRYPTO_REQUEST);
        if (request != null) {
            showSendFragment(request);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        InternetManager.unregisterConnectionReceiver(this, this);
        if (mWallet != null) {
            mWallet.removeSyncListener(this);
        }
        SyncService.unregisterSyncNotificationBroadcastReceiver(getApplicationContext(),
                mSyncNotificationBroadcastReceiver);
    }

    @Override
    public void onConnectionChanged(boolean isConnected) {
        Log.d(TAG, "onConnectionChanged: isConnected: " + isConnected);
        if (isConnected) {
            if (mNotificationBar != null) {
                mNotificationBar.setVisibility(View.GONE);
            }

            SyncService.startService(getApplicationContext(), mCurrentWalletIso);
        } else {
            if (mNotificationBar != null) {
                mNotificationBar.setVisibility(View.VISIBLE);
                mNotificationBar.bringToFront();
            }
        }
    }

    public void closeNotificationBar() {
        mNotificationBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void txListModified(String hash) {
        BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("UI:" + TAG + ":updateUi");
                updateUi();
            }
        });
    }

    /* SyncListener methods */
    @Override
    public void syncStopped(String error) {

    }

    @Override
    public void syncStarted() {
        SyncService.startService(getApplicationContext(), mCurrentWalletIso);
    }
    /* SyncListener methods End*/

    public void updateSyncProgress(double progress) {
        if (progress != SyncService.PROGRESS_FINISH) {
            StringBuffer labelText = new StringBuffer(getString(R.string.SyncingView_syncing));
            labelText.append(' ')
                    .append(NumberFormat.getPercentInstance().format(progress));
            mProgressLabel.setText(labelText);
            mProgressLabel.setVisibility(View.VISIBLE);
            mSyncProgress.setVisibility(View.VISIBLE);
        } else {
            mProgressLabel.setVisibility(View.GONE);
            mSyncProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onChanged() {
        BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
        @Override
        public void run() {
            updateUi();
        }
    });
    }

    @Override
    public void onBalanceChanged(BigDecimal newBalance) {
        BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
            @Override
            public void run() {
                updateUi();
            }
        });
    }

    /**
     * The {@link SyncNotificationBroadcastReceiver} is responsible for receiving updates from the
     * {@link SyncService} and updating the UI accordingly.
     */
    private class SyncNotificationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SyncService.ACTION_SYNC_PROGRESS_UPDATE.equals(intent.getAction())) {
                String intentWalletIso = intent.getStringExtra(SyncService.EXTRA_WALLET_CURRENCY_CODE);
                double progress = intent.getDoubleExtra(SyncService.EXTRA_PROGRESS, SyncService.PROGRESS_NOT_DEFINED);
                if (mCurrentWalletIso.equals(intentWalletIso)) {
                    if (progress >= SyncService.PROGRESS_START) {
                        HomeActivity.this.updateSyncProgress(progress);
                    } else {
                        Log.e(TAG, "SyncNotificationBroadcastReceiver.onReceive: Progress not set:" + progress);
                    }
                } else {
                    Log.e(TAG, "SyncNotificationBroadcastReceiver.onReceive: Wrong wallet. Expected:"
                            + mCurrentWalletIso + " Actual:" + intentWalletIso + " Progress:" + progress);
                }
            }
        }
    }

    public void showSendFragment(final CryptoRequest request) {
        // TODO: Find a better solution.
        if (FragmentSend.isIsSendShown()) {
            return;
        }
        FragmentSend.setIsSendShown(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FragmentSend fragmentSend = (FragmentSend) getSupportFragmentManager()
                        .findFragmentByTag(FragmentSend.class.getName());
                if (fragmentSend == null) {
                    fragmentSend = new FragmentSend();
                }

                Bundle arguments = new Bundle();
                arguments.putSerializable(EXTRA_CRYPTO_REQUEST, request);
                fragmentSend.setArguments(arguments);
                if (!fragmentSend.isAdded()) {
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(0, 0, 0, R.animator.plain_300)
                            .add(android.R.id.content, fragmentSend, FragmentSend.class.getName())
                            .addToBackStack(FragmentSend.class.getName()).commit();
                }
            }
        }, SEND_SHOW_DELAY);

    }

    private BigDecimal convertSats(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            return amount.divide(new BigDecimal(100000000), 8, RoundingMode.DOWN);
        } else {
            // This prevents 0 E-8 notation for 0.00000000
            return BigDecimal.ZERO;
        }
    }
}
