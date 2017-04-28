package com.le.samplecodecamp.eui.update;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.eui.sdk.independent.net.ResponseWrapper;
import com.eui.sdk.independent.util.UpdateUtil;
import com.le.samplecodecamp.eui.domain.LeDomainManager;
import com.le.samplecodecamp.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by zhangjiahao on 17-4-18.
 */

public class VersionCheckTask extends AsyncTask<Void, Void, VersionCheckTask.Version> {

    private static final String TAG = "VersionCheckTask";
    private final Activity mActivity;
    private final String mPackageName;
    private final VersionListener mListener;

    private int mErrorCode;

    public interface VersionListener {

        void onError(int code);

        void onCancel();

        void onVersion(AppInfo appInfo);
    }

    public VersionCheckTask(Activity context, String pkg, VersionListener listener) {
        this.mActivity = context;
        this.mPackageName = pkg;
        this.mListener = listener;
    }

    @Override
    protected Version doInBackground(Void... params) {
        // 获取imei
        TelephonyManager tm = (TelephonyManager) mActivity
                .getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String imei = ReflectUtils.getLEUIDeviceId(tm);
        if (TextUtils.isEmpty(imei)) {
            Log.w(TAG, "Invalid imei :" + imei);
            return null;
        }
        // 根据分组获取ota域名
        String ota;
        LeDomainManager leDomainManager = new LeDomainManager(mActivity.getContentResolver());
        try {
            ota = leDomainManager.blockingGetGroupDomain(imei, "ota").get("ota");
        } catch (IOException e) {
            Log.w(TAG, "Error get ota host, maybe no internet.", e);
            mErrorCode = UpdateListener.ERROR_CODE_NETWORK;
            return null;
        } catch (InterruptedException e) {
            Log.w(TAG, "thread is interrupted.", e);
            return null;
        }

        if (TextUtils.isEmpty(ota)) {
            Log.w(TAG, "Error get ota host, maybe no such label named ota.");
            return null;
        }

        // 请求网络
        NetEngine netEngine = new NetEngine(mActivity, ota);
        ResponseWrapper response = netEngine.requestApkInfo(mActivity, mPackageName,
                UpdateUtil.getVersion(mActivity.getApplicationContext(), mPackageName));
        if (response.status == -1) {
            // 没网
            mErrorCode = UpdateListener.ERROR_CODE_NETWORK;
            return null;
        }
        if (response.status != 200 || TextUtils.isEmpty(response.data)) {
            // 请求到错误数据
            return null;
        }

        // 解析
        try {
            JSONObject jsonObject = new JSONObject(response.data);
            int errno = jsonObject.optInt("errno");
            String errmsg = jsonObject.optString("errmsg");
            JSONObject data = jsonObject.optJSONObject("data");
            String fileUrl = data.optString("fileUrl");
            String packageName = data.optString("packageName");
            String md5 = data.optString("fileMd5");
            if (errno != 10000 || TextUtils.isEmpty(fileUrl)
                    || TextUtils.isEmpty(packageName)
                    || TextUtils.isEmpty(md5)) {
                return new Version();
            }
            AppInfo appInfo = new AppInfo();
            appInfo.fileUrl = fileUrl;
            appInfo.packageName = packageName;
            appInfo.fileMd5 = md5;
            appInfo.apkVersion = data.optString("apkVersion");
            appInfo.description = data.optString("description");
            appInfo.upgradeType = data.optInt("upgradeType");
            Version version = new Version();
            version.mAppInfo = appInfo;
            return version;
        } catch (JSONException e) {
            Log.w(TAG, "parse json error", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Version result) {
        if (result == null) {
            mListener.onError(mErrorCode);
            return;
        }

        mListener.onVersion(result.mAppInfo);
    }

    @Override
    protected void onCancelled() {
        LogUtils.i(TAG, "cancelled check version for %s", mPackageName);
        mListener.onCancel();
    }

    public class Version {
        AppInfo mAppInfo;
    }

}
