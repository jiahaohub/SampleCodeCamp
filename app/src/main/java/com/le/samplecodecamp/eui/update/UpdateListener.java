package com.le.samplecodecamp.eui.update;

/**
 * Created by zhangjiahao on 17-3-31.
 */

public interface UpdateListener {

    int ERROR_CODE_NETWORK = 0x11;

    int ERROR_CODE_DOWNLOAD = 0x12;

    int ERROR_CODE_INSTALL = 0x13;

    int ACTION_VERSION_RESULT = 0x21;

    int ACTION_START_DOWNLOAD = 0x22;

    int ACTION_START_INSTALL = 0x23;

    void onAction(String pkg, int code, AppInfo appInfo);

    void onFail(String pkg, int code);

    void onExit();
}
