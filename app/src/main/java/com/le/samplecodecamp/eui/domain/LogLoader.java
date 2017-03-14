package com.le.samplecodecamp.eui.domain;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.OperationCanceledException;
import android.provider.OpenableColumns;

import com.le.samplecodecamp.utils.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zhangjiahao on 17-3-13.
 */

public class LogLoader extends AsyncTaskLoader<List<LogContent.LogItem>> {

    private static final String TAG = "LogLoader";
    private final List<Uri> mUris;

    final ForceLoadContentObserver mObserver;
    List<LogContent.LogItem> mData;
    CancellationSignal mCancellationSignal;

    public LogLoader(Context context, List<Uri> uris) {
        super(context);
        this.mUris = uris;
        mObserver = new ForceLoadContentObserver();
    }

    @Override
    public List<LogContent.LogItem> loadInBackground() {
        if (mUris == null || mUris.isEmpty()) {
            LogUtils.i(TAG, "uri is null or empty.");
            return null;
        }
        synchronized (this) {
            if (isLoadInBackgroundCanceled()) {
                throw new OperationCanceledException();
            }
            mCancellationSignal = new CancellationSignal();
        }
        ArrayList<LogContent.LogItem> logItems = new ArrayList<>();
        try {
            for (Uri uri : mUris) {
                if (isLoadInBackgroundCanceled()) {
                    LogUtils.i(TAG, "canceled load in background, break it.");
                    return null;
                }
                LogUtils.i(TAG, "load uri %s", uri.toString());
                try (Cursor cursor = getContext().getContentResolver()
                        .query(uri, null, null, null, null, mCancellationSignal)) {
                    if (cursor == null) {
                        LogUtils.w(TAG, "cursor is null when load uri %s", uri.toString());
                        continue;
                    }
                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                        String name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        long size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
//                        String detail = readDetail(uri);
                        logItems.add(new LogContent.LogItem(name, size, uri));
                        // sort by filename
                        Collections.sort(logItems, FILENAME_COMPARATOR);
                        LogUtils.d(TAG, "load file %s, size is %d", name, size);
                    }
                }
            }
        } finally {
            synchronized (this) {
                mCancellationSignal = null;
            }
        }
        register(logItems);
        return logItems;
    }

    @Override
    public void cancelLoadInBackground() {
        super.cancelLoadInBackground();

        synchronized (this) {
            if (mCancellationSignal != null) {
                mCancellationSignal.cancel();
            }
        }
    }

    private void register(List<LogContent.LogItem> data) {
        if (data != null && !data.isEmpty()) {
            for (LogContent.LogItem item : data) {
                item.registerContentObserver(getContext(), mObserver);
            }
        }
    }

    private void unregister(List<LogContent.LogItem> data) {
        if (data != null && !data.isEmpty()) {
            for (LogContent.LogItem item : data) {
                item.unregisterContentObserver(getContext(), mObserver);
            }
        }
    }

    @Override
    public void deliverResult(List<LogContent.LogItem> data) {
        if (isReset()) {
            if (data != null) {
                unregister(data);
            }
            return;
        }
        mData = data;

        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        LogUtils.i(TAG, "onStartLoading");
        super.onStartLoading();
        if (mData != null) {
            deliverResult(mData);
        }
        if (takeContentChanged() || mData == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(List<LogContent.LogItem> data) {
        unregister(data);
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();

        if (mData != null && !mData.isEmpty()) {
            unregister(mData);
        }
        mData = null;
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        LogUtils.i(TAG, "onContentChanged");
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        LogUtils.i(TAG, "onForceLoad");
    }

    public static final Comparator<LogContent.LogItem> FILENAME_COMPARATOR = new Comparator<LogContent.LogItem>() {
        @Override
        public int compare(LogContent.LogItem o1, LogContent.LogItem o2) {
            return o1.name.compareTo(o2.name);
        }
    };

}
