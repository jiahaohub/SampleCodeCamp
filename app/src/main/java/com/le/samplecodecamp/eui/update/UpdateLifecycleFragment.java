package com.le.samplecodecamp.eui.update;


import android.app.Fragment;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class UpdateLifecycleFragment extends Fragment {

    private final UpdateLifecycle mLifecycle;

    public UpdateLifecycleFragment() {
        mLifecycle = new UpdateLifecycle();
    }

    public Lifecycle getLifecycle() {
        return mLifecycle;
    }

    @Override
    public void onStart() {
        super.onStart();
        mLifecycle.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLifecycle.onDestroy();
    }

    private class UpdateLifecycle implements Lifecycle {

        private final Set<LifecycleListener> lifecycleListeners =
                Collections.newSetFromMap(new WeakHashMap<LifecycleListener, Boolean>());
        private boolean isStarted;
        private boolean isDestroyed;

        @Override
        public void addListener(LifecycleListener listener) {
            lifecycleListeners.add(listener);

            if (isDestroyed) {
                listener.onStop();
            } else if (isStarted) {
                listener.onStart();
            }
        }

        void onStart() {
            isStarted = true;
            for (LifecycleListener lifecycleListener : lifecycleListeners) {
                lifecycleListener.onStart();
            }
        }

        void onDestroy() {
            isDestroyed = true;
            for (LifecycleListener lifecycleListener : lifecycleListeners) {
                lifecycleListener.onStop();
            }
        }
    }

}
