package com.electraproject.presenter.activities.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.electraproject.R;
import com.electraproject.presenter.customviews.BRDialogView;
import com.electraproject.presenter.entities.CurrencyEntity;
import com.electraproject.presenter.interfaces.BRAuthCompletion;
import com.electraproject.tools.animation.BRDialog;
import com.electraproject.tools.manager.BRSharedPrefs;
import com.electraproject.tools.security.AuthManager;
import com.electraproject.tools.security.BRKeyStore;
import com.electraproject.tools.sqlite.RatesDataSource;
import com.electraproject.tools.util.Utils;
import com.electraproject.wallet.wallets.bitcoin.WalletBitcoinManager;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class FingerprintActivity extends BaseSettingsActivity {
    private static final String TAG = FingerprintActivity.class.getName();

    public RelativeLayout layout;
    public static boolean appVisible = false;
    private static FingerprintActivity app;
    private TextView limitExchange;
    private TextView limitInfo;

    private ToggleButton toggleButton;
    private Button mLimitButton;

    public static FingerprintActivity getApp() {
        return app;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);
        toggleButton = findViewById(R.id.toggleButton);
        limitExchange = findViewById(R.id.limit_exchange);
        limitInfo = findViewById(R.id.limit_info);
        setOnClickListeners();

        toggleButton.setChecked(BRSharedPrefs.getUseFingerprint(this));

        limitExchange.setText(getLimitText());

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Activity app = FingerprintActivity.this;
                if (isChecked && !Utils.isFingerprintEnrolled(app)) {
                    BRDialog.showCustomDialog(app, getString(R.string.TouchIdSettings_disabledWarning_title_android),
                            getString(R.string.TouchIdSettings_disabledWarning_body_android), getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                                @Override
                                public void onClick(BRDialogView brDialogView) {
                                    brDialogView.dismissWithAnimation();
                                }
                            }, null, null, 0);
                    buttonView.setChecked(false);
                } else {
                    BRSharedPrefs.putUseFingerprint(app, isChecked);
                }

            }
        });
        SpannableString ss = new SpannableString(getString(R.string.TouchIdSettings_customizeText_android));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {

                AuthManager.getInstance().authPrompt(FingerprintActivity.this, null, getString(R.string.VerifyPin_continueBody), true, false, new BRAuthCompletion() {
                    @Override
                    public void onComplete() {
                        Intent intent = new Intent(FingerprintActivity.this, SpendLimitActivity.class);
                        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onCancel() {

                    }
                });


            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        //start index of the last space (beginning of the last word)
       // int indexOfSpace = limitInfo.getText().toString().lastIndexOf(" ");
        // make the whole text clickable if failed to select the last word
       // ss.setSpan(clickableSpan, indexOfSpace == -1 ? 0 : indexOfSpace, limitInfo.getText().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        limitInfo.setText(ss);
        limitInfo.setMovementMethod(LinkMovementMethod.getInstance());
        limitInfo.setHighlightColor(Color.TRANSPARENT);

    }

    private String getLimitText() {
        String iso = BRSharedPrefs.getPreferredFiatIso(this);
        //amount in satoshis

        WalletBitcoinManager wm = WalletBitcoinManager.getInstance(this);

        BigDecimal cryptoLimit = BRKeyStore.getSpendLimit(this, wm.getCurrencyCode());
        //amount in user preferred ISO (e.g. USD)
        BigDecimal curAmount = wm.getFiatForSmallestCrypto(this, cryptoLimit, null);
        //formatted string for the label

        CurrencyEntity btcFiatRate = RatesDataSource.getInstance(this).getCurrencyByCode(app, "ECA", BRSharedPrefs.getPreferredFiatIso(this));

        BigDecimal total = wm.getFiatExchangeRate(this).multiply(new BigDecimal(btcFiatRate.rate)).multiply(cryptoLimit);
        total = total.setScale(2, RoundingMode.CEILING);
        return String.format(getString(R.string.TouchIdSettings_spendingLimit), cryptoLimit+"ECA", total+iso.toUpperCase());
    }

    private void setOnClickListeners() {
        Button mLimitButton = findViewById(R.id.button_limit);
        mLimitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthManager.getInstance().authPrompt(FingerprintActivity.this, null, getString(R.string.VerifyPin_continueBody), true, false, new BRAuthCompletion() {
                    @Override
                    public void onComplete() {
                        Intent intent = new Intent(FingerprintActivity.this, SpendLimitActivity.class);
                        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        app = this;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    @Override
    public void onPause() {
        super.onPause();
        appVisible = false;
    }
    @Override
    public int getLayoutId() {
        return R.layout.activity_share_data;
    }

    @Override
    public int getBackButtonId() {
        return R.id.back_button;
    }
}
