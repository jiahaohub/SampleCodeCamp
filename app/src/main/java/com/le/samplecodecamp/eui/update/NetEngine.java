package com.le.samplecodecamp.eui.update;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.eui.sdk.independent.net.HttpHelper;
import com.eui.sdk.independent.net.ResponseWrapper;
import com.eui.sdk.independent.util.PhoneUtil;
import com.eui.sdk.independent.util.UpdateUtil;
import com.eui.sdk.independent.util.UserAgent;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by spring on 16-9-8.
 */
public class NetEngine {

    private final String mOtaHost;

    public NetEngine(Context context, String host) {
        //设置UserAgent
        String userAgent = UserAgent.ensureUserAgent(context);
        HttpHelper.setUrlParameter().setUserAgent(userAgent).setIsLogOn(true);
        mOtaHost = host;
    }

    /**
     * 单个app请求单个apk信息
     *
     * @param context
     * @param packageName
     * @param apkVersion
     * @return
     */
    public ResponseWrapper requestApkInfo(Context context, String packageName, String apkVersion) {
        //请求数据
        HashMap<String, String> params = new HashMap<>();
        params.put("packageName", packageName);
        params.put("apkVersion", apkVersion);
        params.put("deviceType", "phone");

        String imei = PhoneUtil.getIMEI(context);
        if (TextUtils.isEmpty(imei)) {//imei
            params.put("deviceId", "");
        } else {
            params.put("deviceId", imei);
        }

//        params.put("deviceId", "869552020035176");
        params.put("model", Build.MODEL);//手机型号
        params.put("region", UpdateUtil.getRegion(context));
        params.put("user-prefer-language", UpdateUtil.getLanguage(context));
        params.put("ui", SystemProperties.getEUIVersion("ro.letv.release.version"));

        //加密
        String url;
        if (true) {
            url = Url.GET_SINGLE_APK_INFO_URL_TESTSERVER + "?" + INetEncryptImpl.getInstance().encrypt(params);
        } else {
//            url = Url.GET_SINGLE_APK_INFO_URL + "?" + INetEncryptImpl.getInstance().encrypt(params);
            String httpUrl = new Uri.Builder()
                    .scheme("http")
                    .authority(mOtaHost)
                    .appendEncodedPath(Url.PATH_UPGRADE)
                    .toString();
            url = httpUrl + "?" + INetEncryptImpl.getInstance().encrypt(params);
        }

        //请求数据
        Request request = new Request.Builder()
                .url(url)
                .build();
        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request originalRequest = chain.request();
                        return chain.proceed(
                                originalRequest.newBuilder()
                                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                                        .build());
                    }
                })
                .build();
//        client.newCall(request).execute();

        ResponseWrapper response = HttpHelper.doGet(url);

        return response;
    }

}
