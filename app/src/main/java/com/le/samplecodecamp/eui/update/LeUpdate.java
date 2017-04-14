package com.le.samplecodecamp.eui.update;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.pm.ActivityInfo;

import com.le.samplecodecamp.utils.LogUtils;

/**
 * Created by zhangjiahao on 17-4-13.
 */

public class LeUpdate {

    private static final String TAG = "LeUpdate";

    public static void check(Activity activity, String pkg) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        FragmentManager fm = activity.getFragmentManager();
        Fragment fragment = fm.findFragmentByTag(pkg);
        if (fragment == null) {
            fragment = VersionCheckFragment.newInstance(pkg);
            fm.beginTransaction().add(fragment, pkg).commitAllowingStateLoss();
        } else {
            LogUtils.i(TAG, "already running...");
        }
    }
}
