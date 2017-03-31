package com.le.samplecodecamp.eui.update;

/**
 * Created by spring on 16-9-8.
 */
public class Url {
    /** HOST */
    public static final String HOST_PRODUCTOR = "ota.scloud.letv.com";
    public static final String HOST_TEST = "test.tvapi.letv.com";

    /** SCHEME */
    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";

    public static final String PATH_UPGRADE = "apk/api/v1/getUpgradeInfo";

    /** GET SINGLE APK INFO */
    public static final String GET_SINGLE_APK_INFO_URL = HTTP + HOST_PRODUCTOR + "/apk/api/v1/getUpgradeInfo";
    public static final String GET_SINGLE_APK_INFO_URL_TESTSERVER = HTTP + HOST_TEST + "/apk/api/v1/getUpgradeInfo";
}
