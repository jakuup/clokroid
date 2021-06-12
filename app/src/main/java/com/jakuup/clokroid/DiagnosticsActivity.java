package com.jakuup.clokroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DiagnosticsActivity extends AppCompatActivity {
    private static final int REQ_CODE_SHOW_LOGS = 1000;

    private final View.OnClickListener onClickListener = view -> {
        if (view == findViewById(R.id.buttonShowLogs)) {
            Intent showLogs = new Intent(this, ScrolledString.class);
            Bundle params = new Bundle();

            params.putString("textData", ClokroidApplication.getLogger().dump());
            params.putBoolean("clearEnabled", true);
            showLogs.putExtras(params);

            startActivityForResult(showLogs, REQ_CODE_SHOW_LOGS);
        }
        else if (view == findViewById(R.id.buttonStartBackgroudActs)) {
            Alarms.setNetworkAlarm(this, null);
        }
        else if (view == findViewById(R.id.buttonExitDiagnostics)) {
            finish();
        }
        else if (view == findViewById(R.id.buttonTestConnection)) {
            Switch sw = new Switch("192.168.1.71", 50505);

            sw.connect();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostics);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.buttonShowLogs).setOnClickListener(onClickListener);
        findViewById(R.id.buttonStartBackgroudActs).setOnClickListener(onClickListener);
        findViewById(R.id.buttonExitDiagnostics).setOnClickListener(onClickListener);
        findViewById(R.id.buttonTestConnection).setOnClickListener(onClickListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SHOW_LOGS) {
            if (resultCode == ScrolledString.RESULT_CLEAR) {
                ClokroidApplication.getLogger().clear();
            }
        }
    }
}
