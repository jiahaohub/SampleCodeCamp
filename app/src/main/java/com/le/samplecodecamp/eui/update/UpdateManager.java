package com.le.samplecodecamp.eui.update;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.pm.ActivityInfo;

/**
 * Created by zhangjiahao on 17-3-31.
 */

public class UpdateManager {

    private static final String TAG = "UpdateManager";

    public static boolean enqueue(Activity activity, String pkg) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        FragmentManager fm = activity.getFragmentManager();
        Fragment fragment = fm.findFragmentByTag(pkg);
        if (fragment == null) {
            fragment = VersionCheckFragment.newInstance(pkg);
            fm.beginTransaction().add(fragment, pkg).commitAllowingStateLoss();
            return true;
        }
        return false;
    }

}
