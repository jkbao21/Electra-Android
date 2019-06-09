package com.electraproject.presenter.activities.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.electraproject.R;
import com.electraproject.presenter.activities.InputWordsActivity;
import com.electraproject.presenter.activities.util.BRActivity;
import com.electraproject.tools.animation.UiUtils;


public class UnlinkActivity extends BRActivity {
    private Button nextButton;
    private ImageButton close;
    public static boolean appVisible = false;
    private static UnlinkActivity app;

    public static UnlinkActivity getApp() {
        return app;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore);

        nextButton = findViewById(R.id.send_button);
        close = findViewById(R.id.close_button);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UiUtils.isClickAllowed()) return;
                Intent intent = new Intent(UnlinkActivity.this, InputWordsActivity.class);
                intent.putExtra(InputWordsActivity.EXTRA_UNLINK, true);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                if (!UnlinkActivity.this.isDestroyed()) finish();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UiUtils.isClickAllowed()) return;
                onBackPressed();
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
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

}
