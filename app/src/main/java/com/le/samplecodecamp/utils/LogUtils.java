package com.le.samplecodecamp.utils;

import android.text.TextUtils;

import com.le.samplecodecamp.common.logger.Log;
import com.le.samplecodecamp.common.logger.LogWrapper;

/**
 * Created by zhangjiahao on 17-3-2.
 */

public final class LogUtils {

    private static boolean DEBUG = false;

    /**
     * 过滤打印级别
     */
    private static int MAX_ENABLED_LOG_LEVEL = Log.VERBOSE;

    static {
        LogWrapper log = new LogWrapper();
        LogFile fileLog = new LogFile();
        fileLog.setNext(log);
        Log.setLogNode(fileLog);
    }

    /**
     * 设置要过滤的级别
     * @param level
     */
    public static void setMaxEnabledLogLevel(int level) {
        MAX_ENABLED_LOG_LEVEL = level;
    }

    private static boolean isLoggable(String tag, int level) {
        if (MAX_ENABLED_LOG_LEVEL > level || TextUtils.isEmpty(tag) || tag.length() >= 23) {
            return false;
        }

        return DEBUG || android.util.Log.isLoggable(tag, level);
    }

    public static void v(String tag, Throwable tr, String format, Object... args) {
        if (isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, format(format, args), tr);
        }
    }

    public static void v(String tag, String format, Object... args) {
        v(tag, null, format, args);
    }

    public static void d(String tag, Throwable tr, String format, Object... args) {
        if (isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, format(format, args), tr);
        }
    }

    public static void d(String tag, String format, Object... args) {
        d(tag, null, format, args);
    }

    public static void i(String tag, Throwable tr, String format, Object... args) {
        if (isLoggable(tag, Log.INFO)) {
            Log.i(tag, format(format, args), tr);
        }
    }

    public static void i(String tag, String format, Object... args) {
        i(tag, null, format, args);
    }

    public static void w(String tag, Throwable tr, String format, Object... args) {
        if (isLoggable(tag, Log.WARN)) {
            Log.w(tag, format(format, args), tr);
        }
    }

    public static void w(String tag, String format, Object... args) {
        w(tag, null, format, args);
    }

    public static void w(String tag, Throwable tr) {
        w(tag, null, tr);
    }

    public static void e(String tag, Throwable tr, String format, Object... args) {
        if (isLoggable(tag, Log.ERROR)) {
            Log.e(tag, format(format, args), tr);
        }
    }

    public static void e(String tag, String format, Object... args) {
        e(tag, null, format, args);
    }

    public static void wtf(String tag, Throwable tr, String format, Object... args) {
        if (isLoggable(tag, Log.ASSERT)) {
            Log.wtf(tag, format(format, args), tr);
        }
    }

    public static void wtf(String tag, String format, Object... args) {
        wtf(tag, null, format, args);
    }

    public static void wtf(String tag, Throwable tr) {
        wtf(tag, tr, null);
    }

    private static String format(String format, Object... args) {
        if (null == format) {
            return null;
        }
        if (args.length == 0) {
            return format;
        }
        return String.format(format, args);
    }
}
