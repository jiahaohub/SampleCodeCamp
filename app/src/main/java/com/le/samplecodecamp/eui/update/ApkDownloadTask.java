package com.le.samplecodecamp.eui.update;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;

import com.le.samplecodecamp.utils.LogUtils;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

/**
 * Created by zhangjiahao on 17-4-19.
 */

public class ApkDownloadTask extends AsyncTask<Void, Long, Uri> {

    private static final String TAG = "ApkDownloadTask";

    private final Context mContext;
    private final AppInfo mAppInfo;
    private final Uri mUri;
    private final DownloadListener mListener;

    public interface DownloadListener extends ProgressListener {

        void onComplete(Uri uri);

        void onError();

        void onCancelled();
    }

    public ApkDownloadTask(Context context, AppInfo appInfo, Uri uri, DownloadListener listener) {
        mContext = context;
        mAppInfo = appInfo;
        mUri = uri;
        mListener = listener;
    }

    @Override
    protected Uri doInBackground(Void... params) {
        Request request = new Request.Builder()
                .url(mAppInfo.fileUrl)
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response originalResponse = chain.proceed(chain.request());
                        return originalResponse.newBuilder()
                                .body(new ProgressResponseBody(originalResponse.body(), mListener))
                                .build();
                    }
                })
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("bad request, code is " + response.code());
            }
            if (!ApkManager.PACKAGE_MIME_TYPE.equals(response.body().contentType().toString())) {
                throw new IOException("bad request, content type is " + response.body().contentType());
            }
            try (ParcelFileDescriptor pfd = mContext.getContentResolver().openFileDescriptor(mUri, "w")) {
                if (pfd != null) {
                    try (BufferedSink sink = Okio.buffer(Okio.sink(
                            new FileOutputStream(pfd.getFileDescriptor())))) {
                        long totalBytesRead = sink.writeAll(response.body().source());
                        LogUtils.i(TAG, "total read %d bytes.", totalBytesRead);
                        return mUri;
                    }
                }
            }
        } catch (final IOException e) {
            LogUtils.e(TAG, e, "download fail");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Uri uri) {
        LogUtils.i(TAG, "on result %s", uri);
        if (uri == null) {
            mListener.onError();
        } else {
            mListener.onComplete(uri);
        }
    }

    @Override
    protected void onCancelled() {
        LogUtils.i(TAG, "on cancelled");
        mListener.onCancelled();
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
