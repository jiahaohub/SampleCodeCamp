package com.le.samplecodecamp.eui.update;

import android.content.Context;
import android.net.Uri;

import com.le.samplecodecamp.utils.LogUtils;

import java.io.File;

/**
 * Created by zhangjiahao on 17-4-19.
 */

public class UpdatePresenter implements UpdateContract.Presenter {

    private static final String TAG = "UpdatePresenter";
    private final Context mContext;
    private final UpdateContract.View mView;
    private final AppInfo mAppInfo;
    private final UpdateListener mListener;
    private ApkDownloadTask mDownloadTask;

    public UpdatePresenter(Context context, UpdateContract.View view, AppInfo appInfo) {
        mContext = context;
        mView = view;
        mView.setPresenter(this);
        mAppInfo = appInfo;
        mListener = LeUpdate.get().getListener();
    }

    @Override
    public void start() {
        // 提示有新版本
        mView.showUpdateAnnouncement();
    }

    @Override
    public void update(boolean accept) {
        mView.dismiss();
        if (accept) {
            // 确定升级，检测apk文件是否存在
            File apk = ApkManager.isApkExists(mAppInfo.fileMd5);
            if (apk != null) {
                mView.installApk(Uri.fromFile(apk));
                mView.finishSelf();
                LeUpdate.get().finish();
            } else {
                if (NetStatusUtils.isNetWorkAvailable(mContext)) {
                    if (NetStatusUtils.isWifi(mContext)) {
                        // wifi，直接下载文件，获取下载文件uri
                        mView.requestDownloadUri();
                    } else {
                        // 弹是否下载确认框
                        mView.showNetworkConfirm();
                    }
                } else {
                    // 提示没网
                    LogUtils.w(TAG, "network is unavailable.");
                    mListener.onFail(mAppInfo.packageName, UpdateListener.ERROR_CODE_NETWORK);
                }
            }
        } else {
            // 不升级，如果是强制升级，需要退出应用
            if (mAppInfo.isForce()) {
                mListener.onExit();
            }
            mView.finishSelf();
            LeUpdate.get().finish();
        }
    }

    @Override
    public void download(boolean accept) {
        mView.dismiss();
        if (accept) {
            // 确认下载，获取下载文件uri
            mView.requestDownloadUri();
        } else {
            // 不想使用流量下载
            if (mAppInfo.isForce()) {
                mListener.onExit();
            }
            mView.finishSelf();
            LeUpdate.get().finish();
        }
    }

    @Override
    public void cancelDownload() {
        if (mDownloadTask != null) {
            mDownloadTask.cancel(true);
            mDownloadTask = null;
        }
    }

    @Override
    public void receivedUri(Uri uri) {
        if (uri == null) {
            LogUtils.w(TAG, "don't received download uri.");
            mListener.onFail(mAppInfo.packageName, UpdateListener.ERROR_CODE_DOWNLOAD);
            mView.finishSelf();
            LeUpdate.get().finish();
        } else {
            performDownload(uri);
        }
    }

    private void performDownload(Uri uri) {
        // 开始下载apk
        mListener.onAction(mAppInfo.packageName, UpdateListener.ACTION_START_DOWNLOAD, mAppInfo);
        if (mAppInfo.isForce()) {
            mView.showProgress();
        }
        mDownloadTask = new ApkDownloadTask(mContext, mAppInfo, uri, new ApkDownloadTask.DownloadListener() {
            @Override
            public void onComplete(Uri uri) {
                mListener.onAction(mAppInfo.packageName, UpdateListener.ACTION_START_INSTALL, mAppInfo);
                mView.dismiss();
                mView.installApk(uri);
                mView.finishSelf();
            }

            @Override
            public void onError() {
                mView.dismiss();
                LogUtils.w(TAG, "error when downloading.");
                mListener.onFail(mAppInfo.packageName, UpdateListener.ERROR_CODE_DOWNLOAD);
                if (mAppInfo.isForce()) {
                    mView.showUpdateAnnouncement();
                } else {
                    mView.finishSelf();
                    LeUpdate.get().finish();
                }
            }

            @Override
            public void onCancelled() {
                mView.dismiss();
                if (mAppInfo.isForce()) {
                    mView.showUpdateAnnouncement();
                } else {
                    mView.finishSelf();
                    LeUpdate.get().finish();
                }
            }

            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
                mView.progress(contentLength, bytesRead);
            }
        });
        mDownloadTask.execute();
    }

    @Override
    public void installed(boolean success) {
        if (!success) {
            LogUtils.w(TAG, "fail install.");
            mListener.onFail(mAppInfo.packageName, UpdateListener.ERROR_CODE_INSTALL);
        }
        mView.finishSelf();
        LeUpdate.get().finish();
    }

}
