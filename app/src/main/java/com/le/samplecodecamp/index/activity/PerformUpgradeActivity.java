package com.le.samplecodecamp.index.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.le.samplecodecamp.R;
import com.le.samplecodecamp.eui.update.AppInfo;
import com.le.samplecodecamp.eui.update.LeUpdate;
import com.le.samplecodecamp.eui.update.UpdateListener;
import com.le.samplecodecamp.utils.LogUtils;

public class PerformUpgradeActivity extends AppCompatActivity implements UpdateListener {

    private static final String TAG = "PerformUpgradeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perform_upgrade);
    }

    public void upgrade(View view) {
        LeUpdate.check(this, "com.eui.sdk.upgrade.aar.example");
        LogUtils.i(TAG, "start check");
    }

    public void showToast(View view) {
        Toast.makeText(this, "boom boom boom", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCheckResult(String pkg, AppInfo appInfo, boolean isExpired) {
        LogUtils.i(TAG, isExpired ? "out-of-date" : "up-to-date");
        Toast.makeText(this, isExpired ? "out-of-date" : "up-to-date", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFail(String pkg, int code) {
        LogUtils.i(TAG, "fail code %d", code);
        Toast.makeText(this, "fail code " + code, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onExit() {
        finish();
    }
}
