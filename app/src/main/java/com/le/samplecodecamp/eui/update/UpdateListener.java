package com.le.samplecodecamp.eui.update;

/**
 * Created by zhangjiahao on 17-3-31.
 */

public interface UpdateListener {

    void onCheckResult(String pkg, AppInfo appInfo);

    void onFail(String pkg, int code);

    void onExit();
}
