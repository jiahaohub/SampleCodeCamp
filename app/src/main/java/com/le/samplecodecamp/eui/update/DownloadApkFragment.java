package com.le.samplecodecamp.eui.update;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import com.le.samplecodecamp.R;
import com.le.samplecodecamp.utils.LogUtils;
import com.letv.shared.widget.LeBottomSheet;

import java.io.File;
import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class DownloadApkFragment extends DialogFragment {

    private static final String TAG = "DownloadApkFragment";
    private static final String ARG_APP_INFO = "arg_app_info";
    private final Handler mMainHandler;

    private AppInfo mAppInfo;
    private UpdateListener mListener;
    private DownloadTask mRunningTask;

    public DownloadApkFragment() {
        mMainHandler = new Handler();
    }

    public static DownloadApkFragment newInstance(AppInfo appInfo) {
        DownloadApkFragment fragment = new DownloadApkFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_APP_INFO, appInfo);
        fragment.setArguments(args);
        fragment.setCancelable(false);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAppInfo = getArguments().getParcelable(ARG_APP_INFO);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LeUpgradeProgressDialogBuilder dialogBuilder = new LeUpgradeProgressDialogBuilder(getContext());
        dialogBuilder.setContent(getString(R.string.le_toast_string_start_download_patch))
                .setCancellationListener(getString(R.string.le_btn_string_cancel),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mRunningTask != null) {
                                    mRunningTask.cancel(true);
                                    mRunningTask = null;
                                }
                                if (mAppInfo.isForce()) {
                                    AnnouncementFragment.getInstance(mAppInfo)
                                            .show(getFragmentManager(), mAppInfo.packageName);
                                }
                                dismissAllowingStateLoss();
                            }
                        });
        return dialogBuilder.build();
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
    public void onStart() {
        super.onStart();
//        if (!mAppInfo.isForce()) {
//            getDialog().hide();
//        }
        mRunningTask = new DownloadTask();
        mRunningTask.execute();
    }

    private class DownloadTask extends AsyncTask<Void, Long, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            final File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            final File temp = new File(dir, "update/" + mAppInfo.fileMd5 + ".temp");
            final File apk = new File(dir, "update/" + mAppInfo.fileMd5 + ".apk");
            if (temp.exists()) {
                temp.delete();
            }
            if (apk.exists()) {
                apk.delete();
            }

            Request request = new Request.Builder()
                    .url(mAppInfo.fileUrl)
                    .build();

            final ProgressListener progressListener = new ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    LogUtils.i(TAG, "total size %d, current size %d, is done %s",
                            contentLength, bytesRead, done);
                    if (done) {
                        if (temp.renameTo(apk)) {
                            InstallFragment fragment = InstallFragment.
                                    getInstance(Uri.fromFile(apk), mAppInfo);
                            getFragmentManager().
                                    beginTransaction().
                                    add(fragment, mAppInfo.packageName).
                                    commitAllowingStateLoss();
                            dismissAllowingStateLoss();
                        }
                    } else {
                        if (isAdded()) {
                            ProgressBar pb = ((LeBottomSheet) getDialog()).getProgressBar();
                            pb.setMax((int) contentLength);
                            pb.setProgress((int) bytesRead);
                        }
                    }
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addNetworkInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Response originalResponse = chain.proceed(chain.request());
                            return originalResponse.newBuilder()
                                    .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                                    .build();
                        }
                    })
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("bad request, code is " + response.code());
                }
                try (BufferedSink sink = Okio.buffer(Okio.sink(temp))) {
                    long totalBytesRead = sink.writeAll(response.body().source());
                    LogUtils.i(TAG, "total read %d bytes.", totalBytesRead);
                }
            } catch (final IOException e) {
                LogUtils.e(TAG, e, "download fail");
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String message = e.getMessage();
                        if (!"thread interrupted".equals(message) && mListener != null) {
                            mListener.onFail(mAppInfo.packageName, 1);
                        } else {
                            dismissAllowingStateLoss();
                        }
                    }
                });
            }
            return null;
        }
    }

    private static class ProgressResponseBody extends ResponseBody {

        private final ResponseBody responseBody;
        private final ProgressListener progressListener;
        private BufferedSource bufferedSource;

        public ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener) {
            this.responseBody = responseBody;
            this.progressListener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                    return bytesRead;
                }
            };
        }
    }

    interface ProgressListener {
        void update(long bytesRead, long contentLength, boolean done);
    }
}
