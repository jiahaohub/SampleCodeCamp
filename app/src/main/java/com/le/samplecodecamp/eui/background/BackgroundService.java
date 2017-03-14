package com.le.samplecodecamp.eui.background;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.le.samplecodecamp.utils.LogUtils;

import java.util.List;

/**
 *  use cmd: adb shell am startservice -a com.le.samplecodecamp.backgraound
 *  check the package whether in foreground
 */
public class BackgroundService extends Service {

    private static final String TAG = "BackgroundService";

    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "BackgroundService onStartCommand");
        isForeground(this, "com.android.launcher3");
        stopSelf(startId);
        return super.onStartCommand(intent, flags, startId);
    }

    public static boolean isForeground(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            LogUtils.i(TAG, "isForeground packageName is null ");
            return false;
        }
        ActivityManager am = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return false;
        }
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (tasks != null && tasks.size() > 0) {
            ComponentName topActivity = tasks.get(0).topActivity;
            LogUtils.i(TAG, "topActivity : %s", topActivity.getPackageName());
            if (packageName.equals(topActivity.getPackageName())) {
                LogUtils.i(TAG, "%s is in foreground", packageName);
                return true;
            } else {
                LogUtils.i(TAG, "%s isn't in foreground", packageName);
                return false;
            }
        } else {
            LogUtils.i(TAG, "isForeground tasks is null or empty");
        }
        return false;
    }
}
