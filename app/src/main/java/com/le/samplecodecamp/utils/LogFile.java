package com.le.samplecodecamp.utils;

import com.le.samplecodecamp.App;
import com.le.samplecodecamp.common.logger.Log;
import com.le.samplecodecamp.common.logger.LogNode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhangjiahao on 17-3-2.
 */

public class LogFile implements LogNode {

    private final ExecutorService mThreadPool = Executors.newSingleThreadExecutor();

    private LogNode mNext;

    public LogNode getNext() {
        return mNext;
    }

    public void setNext(LogNode node) {
        mNext = node;
    }

    @Override
    public void println(int priority, String tag, String msg, Throwable tr) {
        if (priority == Log.ASSERT) {
            mThreadPool.execute(new WriteFileRunnable(tag, msg, tr));
        }

        if (mNext != null) {
            mNext.println(priority, tag, msg, tr);
        }
    }

    private class WriteFileRunnable implements Runnable {

        private static final String TAG = "WriteFileRunnable";
        private final String mTag;
        private final String mMsg;
        private final Throwable mTr;

        public WriteFileRunnable(String tag, String msg, Throwable tr) {
            this.mTag = tag;
            this.mMsg = msg;
            this.mTr = tr;
        }

        @Override
        public void run() {
            Calendar calendar = Calendar.getInstance();

            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
            String formattedFileName = format.format(calendar.getTime());
            File file = new File(App.getApplication().getFilesDir(), formattedFileName + ".log");
            if (!file.exists()) {
                try {
                    boolean result = file.createNewFile();
                    if (!result) {
                        Log.w(TAG, String.format("fail to create file %s", file.getAbsolutePath()));
                        return;
                    }
                } catch (IOException e) {
                    Log.w(TAG, String.format("fail to create file %s", file.getAbsolutePath()), e);
                    return;
                }
            }

            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            String formattedTime = format.format(calendar.getTime());
            String useMsg = mMsg;
            if (mMsg == null) {
                useMsg = "";
            }
            if (mTr != null) {
                useMsg += "\n" + android.util.Log.getStackTraceString(mTr);
            }
            String formattedMsg = String.format("[%s] [%s] %s", formattedTime, mTag, useMsg);

            try (BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
                 BufferedReader in = new BufferedReader(new StringReader(formattedMsg))) {
                String line;
                while ((line = in.readLine()) != null) {
                    out.write(line);
                    out.newLine();
                    out.flush();
                }
            } catch (IOException e) {
                Log.w(TAG, String.format("fail to write %s into file %s", useMsg, file.getAbsolutePath()), e);
            }
        }

    }

}
