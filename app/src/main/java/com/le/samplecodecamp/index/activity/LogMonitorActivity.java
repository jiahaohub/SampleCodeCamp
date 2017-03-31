package com.le.samplecodecamp.index.activity;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.OperationCanceledException;
import android.support.v7.app.AppCompatActivity;

import com.le.samplecodecamp.R;
import com.le.samplecodecamp.eui.domain.DetailFragment;
import com.le.samplecodecamp.eui.domain.LogContent;
import com.le.samplecodecamp.eui.domain.LogFragment;
import com.le.samplecodecamp.utils.LogUtils;

import java.util.ArrayList;

public class LogMonitorActivity extends AppCompatActivity
        implements LogFragment.OnListFragmentInteractionListener {

    private static final String TAG = "LogMonitorActivity";
    private static final int REQUEST_CODE = 0x11;
    private CancellationSignal mCancellationSignal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_monitor);

        Intent intent = new Intent("com.letv.domain.requestlog");
//        intent.putExtra(Intent.EXTRA_ASSIST_PACKAGE, getPackageName());
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
//            ArrayList<Uri> uris = data.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            ClipData clipData = data.getClipData();
            if (clipData == null) {
                LogUtils.w(TAG, "clip data is null.");
                showEmptyView();
                return;
            }
            ArrayList<Uri> uris = new ArrayList<>();
            for (int i = 0; i < clipData.getItemCount(); i++) {
                ClipData.Item item = clipData.getItemAt(i);
                uris.add(item.getUri());
            }
            if (uris.isEmpty()) {
                showEmptyView();
            } else {
                showMeLog(uris);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        synchronized (this) {
            if (mCancellationSignal != null) {
                mCancellationSignal.cancel();
            }
        }
    }

    private void showEmptyView() {
    }

    private void showMeLog(ArrayList<Uri> uris) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, LogFragment.newInstance(1, uris), "LOG_FRAGMENT")
                .commit();
    }

    @Override
    public void onListFragmentInteraction(final LogContent.LogItem item) {
        synchronized (this) {
            if (mCancellationSignal != null) {
                mCancellationSignal.cancel();
            }
        }
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                synchronized (LogMonitorActivity.this) {
                    if (isCancelled()) {
                        throw new OperationCanceledException();
                    }
                    mCancellationSignal = new CancellationSignal();
                }
                try {
                    return item.showMeDetail(getApplicationContext(), mCancellationSignal);
                } catch (OperationCanceledException e) {
                    if (!isCancelled()) {
                        throw e;
                    }
                    LogUtils.i(TAG, "Canceled read detail in background.");
                    return null;
                } finally {
                    synchronized (LogMonitorActivity.this) {
                        mCancellationSignal = null;
                    }
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (s != null) {
                    DetailFragment.newInstance(s).show(getFragmentManager(), "DETAIL_FRAGMENT");
                }
            }

        }.execute();
    }
}
