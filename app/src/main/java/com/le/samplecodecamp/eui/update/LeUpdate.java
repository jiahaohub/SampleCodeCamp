package com.le.samplecodecamp.eui.update;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;

import com.le.samplecodecamp.utils.LogUtils;

/**
 * Created by zhangjiahao on 17-4-13.
 */

public class LeUpdate implements Handler.Callback {

    private static final String TAG = "LeUpdate";
    private static final String APK_INSTALL_ACTION = "android.intent.action.INSTALL_APK";
    private static final String TARGET_PACKAGE = "com.eui.sdk.appupgrade";

    private static final int INSTALL_FAIL = 0x03;

    private static LeUpdate INSTANCE = new LeUpdate();
    private final Handler mHandler;

    private Activity mActivity;
    private String mPackageName;
    private UpdateListener mListener;
    private boolean isRunning;

    private LeUpdate() {
        mHandler = new Handler(Looper.getMainLooper(), this);
    }

    static LeUpdate get() {
        return INSTANCE;
    }

    public static void check(Activity activity, String pkg, UpdateListener listener) {
        LeUpdate leUpdate = LeUpdate.get();
        if (!leUpdate.isRunning) {
            leUpdate.isRunning = true;
            leUpdate.mActivity = activity;
            leUpdate.mPackageName = pkg;
            leUpdate.mListener = listener;
            leUpdate.begin();
        }
    }

    public UpdateListener getListener() {
        return mListener;
    }

    public void show(Activity activity, AppInfo appInfo) {
        Intent intent = new Intent(activity, UpdateActivity.class);
        intent.putExtra("app_info", appInfo);
        activity.startActivity(intent);
    }

    public void installApk(Uri uri) {
        Messenger messenger = new Messenger(mHandler);
        Intent intent = new Intent(APK_INSTALL_ACTION);
        intent.setPackage(TARGET_PACKAGE);
        intent.putExtra("uri", uri);
        intent.putExtra("messenger", messenger);
        if (mActivity.getPackageManager().resolveService(intent, 0) == null) {
            mListener.onFail(mPackageName, UpdateListener.ERROR_CODE_INSTALL);
            finish();
            return;
        }
        mActivity.startService(intent);
    }

    void begin() {
        new VersionCheckTask(mActivity, mPackageName, new VersionCheckTask.VersionListener() {
            @Override
            public void onError(int code) {
                mListener.onFail(mPackageName, code);
                finish();
            }

            @Override
            public void onCancel() {
                finish();
            }

            @Override
            public void onVersion(AppInfo appInfo) {
                mListener.onAction(mPackageName, UpdateListener.ACTION_VERSION_RESULT, appInfo);
                if (appInfo != null) {
                    show(mActivity, appInfo);
                } else {
                    finish();
                }
            }
        }).execute();
    }

    public void finish() {
        isRunning = false;
        mActivity = null;
        mListener = null;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == INSTALL_FAIL) {
            int code = msg.arg1;
            LogUtils.w(TAG, "install failed, reason is %d", code);
            mListener.onFail(mPackageName, UpdateListener.ERROR_CODE_INSTALL);
            finish();
            return true;
        }
        return false;
    }
}
