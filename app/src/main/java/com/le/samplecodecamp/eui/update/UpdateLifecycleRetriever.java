package com.le.samplecodecamp.eui.update;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangjiahao on 17-4-18.
 */

public class UpdateLifecycleRetriever implements Handler.Callback {
    private static final String TAG = "ULR";
    static final String FRAGMENT_TAG = "aar.Update.lifecycle.fragment";

    /**
     * The singleton instance of RequestManagerRetriever.
     */
    private static final UpdateLifecycleRetriever INSTANCE = new UpdateLifecycleRetriever();

    private static final int ID_REMOVE_FRAGMENT_MANAGER = 1;

    // Visible for testing.
    /**
     * Pending adds for RequestManagerFragments.
     */
    final Map<FragmentManager, UpdateLifecycleFragment> pendingRequestManagerFragments =
            new HashMap<FragmentManager, UpdateLifecycleFragment>();

    /**
     * Main thread handler to handle cleaning up pending fragment maps.
     */
    private final Handler handler;

    /**
     * Retrieves and returns the RequestManagerRetriever singleton.
     */
    public static UpdateLifecycleRetriever get() {
        return INSTANCE;
    }

    // Visible for testing.
    UpdateLifecycleRetriever() {
        handler = new Handler(Looper.getMainLooper(), this /* VersionListener */);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Lifecycle get(Activity activity) {
        assertNotDestroyed(activity);
        android.app.FragmentManager fm = activity.getFragmentManager();
        return fragmentGet(fm);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static void assertNotDestroyed(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed()) {
            throw new IllegalArgumentException("You cannot start a load for a destroyed activity");
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    UpdateLifecycleFragment getRequestManagerFragment(final android.app.FragmentManager fm) {
        UpdateLifecycleFragment current = (UpdateLifecycleFragment) fm.findFragmentByTag(FRAGMENT_TAG);
        if (current == null) {
            current = pendingRequestManagerFragments.get(fm);
            if (current == null) {
                current = new UpdateLifecycleFragment();
                pendingRequestManagerFragments.put(fm, current);
                fm.beginTransaction().add(current, FRAGMENT_TAG).commitAllowingStateLoss();
                handler.obtainMessage(ID_REMOVE_FRAGMENT_MANAGER, fm).sendToTarget();
            }
        }
        return current;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    Lifecycle fragmentGet(android.app.FragmentManager fm) {
        UpdateLifecycleFragment current = getRequestManagerFragment(fm);
        return current.getLifecycle();
    }

    @Override
    public boolean handleMessage(Message message) {
        boolean handled = true;
        Object removed = null;
        Object key = null;
        switch (message.what) {
            case ID_REMOVE_FRAGMENT_MANAGER:
                android.app.FragmentManager fm = (android.app.FragmentManager) message.obj;
                key = fm;
                removed = pendingRequestManagerFragments.remove(fm);
                break;
            default:
                handled = false;
        }
        if (handled && removed == null && Log.isLoggable(TAG, Log.WARN)) {
            Log.w(TAG, "Failed to remove expected request manager fragment, manager: " + key);
        }
        return handled;
    }
}
