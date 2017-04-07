package com.le.samplecodecamp.eui.update;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;

import com.eui.sdk.independent.net.ResponseWrapper;
import com.eui.sdk.independent.util.UpdateUtil;
import com.le.samplecodecamp.eui.domain.LeDomainManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class VersionCheckFragment extends Fragment {

    private static final String TAG = "VersionCheckFragment";

    private static final String ARG_PACKAGE_NAME = "arg_package_name";

    private String mPackageName;

    private UpdateListener mListener;
    private NewVersionCheckTask mRunningTask;

    public VersionCheckFragment() {
    }

    public static VersionCheckFragment newInstance(String pkg) {
        VersionCheckFragment fragment = new VersionCheckFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PACKAGE_NAME, pkg);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPackageName = getArguments().getString(ARG_PACKAGE_NAME);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UpdateListener) {
            mListener = (UpdateListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement UpdateListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mRunningTask == null) {
            mRunningTask = new NewVersionCheckTask();
            mRunningTask.execute();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRunningTask != null) {
            mRunningTask.cancel(true);
            mRunningTask = null;
        }
    }

    private class NewVersionCheckTask extends AsyncTask<Void, Void, BackgroundResult> {

        @Override
        protected BackgroundResult doInBackground(Void... params) {
            BackgroundResult result = new BackgroundResult();

            // 获取imei
            TelephonyManager tm = (TelephonyManager) getContext()
                    .getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            String imei = ReflectUtils.getLEUIDeviceId(tm);
            if (TextUtils.isEmpty(imei)) {
                Log.w(TAG, "Invalid imei :" + imei);
                return null;
            }
            // 根据分组获取ota域名
            String ota;
            LeDomainManager leDomainManager = new LeDomainManager(getContext().getContentResolver());
            try {
                ota = leDomainManager.blockingGetGroupDomain(imei, "ota").get("ota");
            } catch (IOException | InterruptedException e) {
                Log.w(TAG, "Error get ota host, maybe no internet.", e);
                result.mErrorCode = 1;
                return result;
            }
            if (TextUtils.isEmpty(ota)) {
                Log.w(TAG, "Error get ota host, maybe no such label named ota.");
                return null;
            }
            // 请求网络
            NetEngine netEngine = new NetEngine(getContext(), ota);
            ResponseWrapper response = netEngine.requestApkInfo(getContext(), mPackageName,
                    UpdateUtil.getVersion(getContext().getApplicationContext(), mPackageName));
            if (response.status == -1) {
                // 没网
                result.mErrorCode = 1;
            }
            if (response.status != 200 || TextUtils.isEmpty(response.data)) {
                // 请求到错误数据
                result.mErrorCode = 1;
                return result;
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
                    return null;
                }
                AppInfo appInfo = new AppInfo();
                appInfo.fileUrl = fileUrl;
                appInfo.packageName = packageName;
                appInfo.fileMd5 = md5;
                appInfo.apkVersion = data.optString("apkVersion");
                appInfo.description = data.optString("description");
                appInfo.upgradeType = data.optInt("upgradeType");
                result.mAppInfo = appInfo;
                return result;
            } catch (JSONException e) {
                Log.w(TAG, "parse json error", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(BackgroundResult result) {
            getFragmentManager().beginTransaction().remove(VersionCheckFragment.this).commitAllowingStateLoss();
            if (null == result) {
                if (mListener != null) {
                    mListener.onCheckResult(mPackageName, null);
                }
            } else if (result.mAppInfo != null) {
                if (mListener != null) {
                    mListener.onCheckResult(mPackageName, result.mAppInfo);
                }
                AnnouncementFragment.getInstance(result.mAppInfo).show(getFragmentManager(), mPackageName);
            } else {
                if (mListener != null) {
                    mListener.onFail(mPackageName, result.mErrorCode);
                }
            }
        }
    }

    private class BackgroundResult {
        int mErrorCode;
        AppInfo mAppInfo;
    }

}
