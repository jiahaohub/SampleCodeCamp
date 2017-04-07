package com.le.samplecodecamp.eui.update;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.ProgressBar;

import com.le.samplecodecamp.R;
import com.le.samplecodecamp.utils.LogUtils;
import com.letv.shared.widget.LeBottomSheet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DownloadApkFragment extends DialogFragment {

    private static final String TAG = "DownloadApkFragment";

    private static final Uri BASE_URI =
            Uri.parse("content://downloads/my_downloads");
    private static final String PACKAGE_MIME_TYPE = "application/vnd.android.package-archive";

    private static final String ARG_APP_INFO = "arg_app_info";
    private static final String ARG_ALLOW_PAID_NETWORK = "arg_allow_paid_network";
    private static final String STATE_TASK_ID = "state_task_id";

    //    private final DownloadStatusObserver mStatusObserver;
    private final CompleteReceiver mCompleteReceiver;
    private final DownloadWatcher mWatcher;
    private final ScheduledExecutorService mScheduleThreadPool;
    private DownloadManager mDownloadManager;
    private AppInfo mAppInfo;
    private boolean mAllowRoaming;

    private UpdateListener mListener;
    private volatile long mTaskId;
    private volatile boolean isRunning;

    public DownloadApkFragment() {
//        mStatusObserver = new DownloadStatusObserver();
        mCompleteReceiver = new CompleteReceiver();
        mWatcher = new DownloadWatcher();
        mScheduleThreadPool = Executors.newScheduledThreadPool(1);
    }

    public static DownloadApkFragment newInstance(AppInfo appInfo, boolean allowRoaming) {
        DownloadApkFragment fragment = new DownloadApkFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_APP_INFO, appInfo);
        args.putBoolean(ARG_ALLOW_PAID_NETWORK, allowRoaming);
        fragment.setArguments(args);
        fragment.setCancelable(false);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAppInfo = getArguments().getParcelable(ARG_APP_INFO);
            mAllowRoaming = getArguments().getBoolean(ARG_ALLOW_PAID_NETWORK, false);
        }
        if (savedInstanceState != null) {
            mTaskId = savedInstanceState.getLong(STATE_TASK_ID);
        }
        getContext().registerReceiver(mCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        mScheduleThreadPool.scheduleAtFixedRate(mWatcher, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mAppInfo.isForce()) {
            LeUpgradeProgressDialogBuilder dialogBuilder = new LeUpgradeProgressDialogBuilder(getContext());
            dialogBuilder.setContent(getContext()
                    .getString(R.string.le_toast_string_start_download_patch));
            return dialogBuilder.build();
        }
        return super.onCreateDialog(savedInstanceState);
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
        mDownloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mTaskId != 0) {
            outState.putLong(STATE_TASK_ID, mTaskId);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mTaskId == 0) {
            enqueueDownloadTask();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(mCompleteReceiver);
        mScheduleThreadPool.shutdownNow();
    }

    private void enqueueDownloadTask() {
        new AsyncTask<Void, Void, Boolean>(){
            @Override
            protected Boolean doInBackground(Void... params) {
                // 查询之前下载完成的或者暂停下载的任务，根据url查询uri字段，根据md5查询hint字段
                // 下载成功，status=200，_data有内容
                List<DownloadInfo> infos = new ArrayList<>();
                try (Cursor c = getContext().getContentResolver().query(BASE_URI, BASIC_COLUMN,
                        "uri = ?",
                        new String[]{mAppInfo.fileUrl}, null)) {
                    if (c != null) {
                        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                            DownloadInfo info = new DownloadInfo();
                            info.restore(c);
                            infos.add(info);
                        }
                    }
                }

                long taskId = -1;
                if (!infos.isEmpty()) {
                    for (DownloadInfo info : infos) {
                        // 已经下载完成
                        if (info.status == 200 &&
                                !TextUtils.isEmpty(info.filepath)) {
                            File file = new File(info.filepath);
                            if (file.exists()) {
                                installApk(Uri.fromFile(file));
                                taskId = info.id;
                                continue;
                            } else {
                                mDownloadManager.remove(info.id);
                            }
                        }
                        // 移除多余的下载任务和文件
                        if (info.status != 200 || taskId != -1) {
                            mDownloadManager.remove(info.id);
                        }
                    }
                }
                if (taskId != -1) {
                    mTaskId = taskId;
                    return true;
                }

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mAppInfo.fileUrl));
                // 设置网络条件
                int networkTypes = DownloadManager.Request.NETWORK_WIFI;
                if (mAllowRoaming) {
                    networkTypes |= DownloadManager.Request.NETWORK_MOBILE;
                }
                request.setAllowedNetworkTypes(networkTypes)
                        .setAllowedOverRoaming(mAllowRoaming);
                // 设置保存文件
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "update/" + mAppInfo.fileMd5 + ".apk")
                        .allowScanningByMediaScanner();
                // 设置通知栏标题和内容
                request.setTitle("自升级更新")
                        .setDescription(mAppInfo.packageName);
                // 设置mime类型
                request.setMimeType(PACKAGE_MIME_TYPE);

                mTaskId = mDownloadManager.enqueue(request);
                isRunning = true;
                return false;
            }

            @Override
            protected void onPostExecute(Boolean finish) {
                if (finish != null && finish) {
                    finishFragment();
                }
            }
        }.execute();

    }

    private int[] getBytesAndStatus() {
        int[] bytesAndStatus = new int[]{-1, -1, 0};
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(mTaskId);
        try (Cursor c = mDownloadManager.query(query)) {
            if (c != null && c.moveToFirst()) {
                bytesAndStatus[0] = c.getInt(
                        c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                bytesAndStatus[1] = c.getInt(
                        c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                bytesAndStatus[2] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
            }
        }
        return bytesAndStatus;
    }

    private void installApk(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, PACKAGE_MIME_TYPE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }

    private void notifyProgress(int totalSize, int currentSize) {
        if (mAppInfo.isForce() && isAdded()) {
            ProgressBar pb = ((LeBottomSheet) getDialog()).getProgressBar();
            pb.setMax(totalSize);
            pb.setProgress(currentSize);
        }
    }

    private void finishFragment() {
        if (isAdded()) {
            getFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
        }
    }

    private class DownloadWatcher implements Runnable {

        @Override
        public void run() {
            if (isRunning) {
                int[] bytesAndStatus = getBytesAndStatus();
                int currentSize = bytesAndStatus[0];
                int totalSize = bytesAndStatus[1];
                int status = bytesAndStatus[2];

                LogUtils.i(TAG, "download status %d, total size %d, current size %d",
                        status, totalSize, currentSize);

                if (status == DownloadManager.STATUS_RUNNING ||
                        status == DownloadManager.STATUS_SUCCESSFUL) {
                    if (totalSize != -1 && currentSize != -1) {
                        notifyProgress(totalSize, currentSize);
                    }
                }
            }
        }
    }

    private class CompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.i(TAG, "receive broadcast, %s", intent.toString());
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                final long completeTaskId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                new AsyncTask<Void, Void, Uri>() {
                    @Override
                    protected Uri doInBackground(Void... params) {
                        if (completeTaskId == mTaskId) {
                            isRunning = false;
                            ContentResolver cr = getContext().getContentResolver();
                            try (Cursor c = cr.query(
                                    BASE_URI, BASIC_COLUMN, "_id = ?",
                                    new String[]{mTaskId + ""}, null)) {
                                if (c != null && c.moveToFirst()) {
                                    String filePath = c.getString(1);
                                    if (!TextUtils.isEmpty(filePath)) {
                                        return Uri.fromFile(new File(filePath));
                                    }
                                }
                            }
                        } else {
                            mDownloadManager.remove(completeTaskId);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Uri uri) {
                        if (uri != null) {
                            installApk(uri);
                        }
                        finishFragment();
                    }
                }.execute();
            }
        }
    }

    public static final String[] BASIC_COLUMN = new String[]{
            "_id",
            "_data",
            "uri",
            "hint",
            "mimetype",
            "status",
            "total_bytes",
            "current_bytes",
            "mediaprovider_uri",
    };

    private class DownloadInfo {
        long id;
        String filepath;
        String download_uri;
        String filehint;
        String mimetype;
        int status;
        long total_bytes;
        long current_bytes;
        String mediaprovider_uri;

        public void restore(Cursor cursor) {
            this.id = cursor.getLong(0);
            this.filepath = cursor.getString(1);
            this.download_uri = cursor.getString(2);
            this.filehint = cursor.getString(3);
            this.mimetype = cursor.getString(4);
            this.status = cursor.getInt(5);
            this.total_bytes = cursor.getLong(6);
            this.current_bytes = cursor.getLong(7);
            this.mediaprovider_uri = cursor.getString(8);
        }
    }
}
