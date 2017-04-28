package com.le.samplecodecamp.eui.update;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.widget.ProgressBar;

import com.le.samplecodecamp.utils.LogUtils;
import com.letv.shared.widget.LeBottomSheet;

public class UpdateActivity extends Activity implements
        UpdateContract.View, AnnouncementFragment.DialogListener,
        NetworkConfirmFragment.DialogListener, DownloadApkFragment.DialogListener {

    private static final String TAG = "UpdateActivity";
    private static final String FRAGMENT_TAG = "aar.update.mProgressDialog";
    private static final String PACKAGE_MIME_TYPE = ApkManager.PACKAGE_MIME_TYPE;
    private static final String APK_URI_ACTION = "android.intent.action.REQUEST_APK_URI";
    private static final String APK_INSTALL_ACTION = "android.intent.action.INSTALL_APK";
    private static final String TARGET_PACKAGE = "com.eui.sdk.appupgrade";

    private static final int REQ_INSTALL_CODE = 0x01;
    private static final int REQ_APK_URI_CODE = 0x02;

    private static final int INSTALL_FAIL = 0x03;

    private AppInfo mAppInfo;
    private UpdateContract.Presenter mPresenter;
    private DownloadApkFragment mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getIntent().hasExtra("app_info")) {
            finish();
            return;
        }

        mAppInfo = getIntent().getParcelableExtra("app_info");

        new UpdatePresenter(this, this, mAppInfo);

        mPresenter.start();
    }

    @Override
    public void setPresenter(UpdateContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showUpdateAnnouncement() {
        AnnouncementFragment.newInstance(mAppInfo).show(getFragmentManager(), FRAGMENT_TAG);
    }

    @Override
    public void onAcceptUpdate() {
        mPresenter.update(true);
    }

    @Override
    public void onRejectUpdate() {
        mPresenter.update(false);
    }

    @Override
    public void showNetworkConfirm() {
        NetworkConfirmFragment.newInstance(mAppInfo).show(getFragmentManager(), FRAGMENT_TAG);
    }

    @Override
    public void requestDownloadUri() {
        Intent intent = new Intent(APK_URI_ACTION);
        intent.putExtra("md5", mAppInfo.fileMd5);
        if (getPackageManager().resolveActivity(intent, 0) == null) {
            LogUtils.w(TAG, "not found activity to provide uri.");
            mPresenter.receivedUri(null);
            return;
        }
        startActivityForResult(intent, REQ_APK_URI_CODE);
    }

    @Override
    public void showProgress() {
        mProgressDialog = DownloadApkFragment.newInstance(mAppInfo);
        mProgressDialog.show(getFragmentManager(), FRAGMENT_TAG);
    }

    @Override
    public void progress(long total, long current) {
        if (mProgressDialog == null) {
            showProgress();
        } else {
            if (mProgressDialog.isAdded()) {
                ProgressBar pb = ((LeBottomSheet) mProgressDialog.getDialog()).getProgressBar();
                pb.setMax((int) total);
                pb.setProgress((int) current);
            }
        }
    }

    @Override
    public void onAcceptDownload() {
        mPresenter.download(true);
    }

    @Override
    public void onRejectDownload() {
        mPresenter.download(false);
    }

    @Override
    public void onCancel() {
        mPresenter.cancelDownload();
    }

    @Override
    public void dismiss() {
        Fragment fragment = getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment == null) {
            return;
        }
        if (fragment instanceof DialogFragment) {
            ((DialogFragment) fragment).dismiss();
        }
    }

    @Override
    public void finishSelf() {
        finish();
    }

    @Override
    public void installApk(Uri uri) {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setDataAndType(uri, PACKAGE_MIME_TYPE);
////        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivityForResult(intent, REQ_CODE);
        LeUpdate.get().installApk(uri);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.i(TAG, "request code %d, result code %d, data %s", requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_INSTALL_CODE:
                if (resultCode == RESULT_CANCELED) {
                    mPresenter.installed(false);
                }
                break;
            case REQ_APK_URI_CODE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    mPresenter.receivedUri(uri);
                } else {
                    mPresenter.receivedUri(null);
                }
                break;
        }
    }

}
