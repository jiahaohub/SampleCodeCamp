package com.le.samplecodecamp.eui.domain;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;

import com.le.samplecodecamp.utils.LogUtils;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangjiahao on 17-3-13.
 */

public class LogContent {

    public static final List<LogItem> ITEMS = new ArrayList<>();

    public static class LogItem {
        private static final String TAG = "LogItem";
        public final String name;
        public final long size;
        public final Uri mUri;

        public LogItem(String name, long size, Uri uri) {
            this.name = name;
            this.size = size;
            this.mUri = uri;
        }

        @Override
        public String toString() {
            return name + ":" + mUri.toString();
        }

        public String showMeDetail(Context context, CancellationSignal cancellationSignal) {
            try (ParcelFileDescriptor inputPFD = context.getContentResolver()
                    .openFileDescriptor(mUri, "r", cancellationSignal)) {
                if (inputPFD != null) {
                    FileDescriptor fd = inputPFD.getFileDescriptor();
                    try (BufferedReader in = new BufferedReader(new FileReader(fd));
                         StringWriter out = new StringWriter()) {
                        String line;
                        while ((line = in.readLine()) != null) {
                            out.write(line);
                            out.write("\n\n");
                            out.flush();
                        }
                        return out.toString();
                    }
                }
            } catch (IOException e) {
                LogUtils.w(TAG, e, "read detail fail");
            }
            return "";
        }

        public void registerContentObserver(Context context, ContentObserver observer) {
            LogUtils.i(TAG, "register observer for %s", mUri);
            context.getContentResolver().registerContentObserver(mUri, false, observer);
        }

        public void unregisterContentObserver(Context context, ContentObserver observer) {
            LogUtils.i(TAG, "unregister observer for %s", mUri);
            context.getContentResolver().unregisterContentObserver(observer);
        }
    }

}
