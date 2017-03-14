package com.le.samplecodecamp;

import android.app.Application;

/**
 * Created by zhangjiahao on 17-3-2.
 */

public class App extends Application {

    private static Application mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Application getApplication() {
        return mContext;
    }
}
