package com.electraproject.presenter.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;

import com.electraproject.R;
import com.electraproject.presenter.activities.settings.BaseSettingsActivity;
import com.electraproject.presenter.customviews.BaseTextView;
import com.electraproject.presenter.entities.BRSettingsItem;
import com.electraproject.tools.adapter.SettingsAdapter;
import com.electraproject.wallet.WalletsMaster;
import com.electraproject.wallet.abstracts.BaseWalletManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by byfieldj on 2/5/18.
 */

public class CurrencySettingsActivity extends BaseSettingsActivity {

    private BaseTextView mTitle;
    private ListView mListView;
    public List<BRSettingsItem> mItems;

    @Override
    public int getLayoutId() {
        return R.layout.activity_currency_settings;
    }

    @Override
    public int getBackButtonId() {
        return R.id.back_button;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitle = findViewById(R.id.title);
        mListView = findViewById(R.id.settings_list);

        final BaseWalletManager wm = WalletsMaster.getInstance(this).getCurrentWallet(this);

        mTitle.setText(String.format("%s %s", wm.getName(), CurrencySettingsActivity.this.getString(R.string.Settings_title)));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mItems == null) {
            mItems = new ArrayList<>();
        }
        mItems.clear();
        BaseWalletManager walletManager = WalletsMaster.getInstance(this).getCurrentWallet(this);
        mItems.addAll(walletManager.getSettingsConfiguration().getSettingsList());
        View view = new View(this);
        mListView.addFooterView(view, null, true);
        mListView.addHeaderView(view, null, true);
        mListView.setAdapter(new SettingsAdapter(this, R.layout.settings_list_item, mItems));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

}
