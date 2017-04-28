package com.le.samplecodecamp.index.activity;

import android.content.Intent;
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
        LeUpdate.check(this, "com.eui.sdk.upgrade.aar.example", this);
        LogUtils.i(TAG, "start check");
    }

    public void showToast(View view) {
        Toast.makeText(this, "boom boom boom", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, PerformUpgradeActivity.class));
    }

    @Override
    public void onAction(String pkg, int code, AppInfo appInfo) {
        switch (code) {
            case UpdateListener.ACTION_VERSION_RESULT:
                boolean expired = appInfo != null;
                Toast.makeText(this, expired ? "out-of-date" : "up-to-date", Toast.LENGTH_SHORT).show();
                break;
            case UpdateListener.ACTION_START_DOWNLOAD:
                Toast.makeText(this, "开始下载", Toast.LENGTH_SHORT).show();
                break;
            case UpdateListener.ACTION_START_INSTALL:
                Toast.makeText(this, "开始安装", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onFail(String pkg, int code) {
        switch (code) {
            case UpdateListener.ERROR_CODE_NETWORK:
                Toast.makeText(this, "网络问题", Toast.LENGTH_SHORT).show();
                break;
            case UpdateListener.ERROR_CODE_DOWNLOAD:
                Toast.makeText(this, "下载失败", Toast.LENGTH_SHORT).show();
                break;
            case UpdateListener.ERROR_CODE_INSTALL:
                Toast.makeText(this, "安装失败", Toast.LENGTH_SHORT).show();

                break;
        }
    }

    @Override
    public void onExit() {
        finish();
    }
}
