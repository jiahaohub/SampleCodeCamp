package com.le.samplecodecamp.eui.update;

import android.content.ContentProviderClient;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;

import com.le.samplecodecamp.utils.LogUtils;

import java.io.File;

/**
 * Created by zhangjiahao on 17-4-19.
 */

public class ApkManager {

    private static final String TAG = "ApkManager";

    public static final String PACKAGE_MIME_TYPE = "application/vnd.android.package-archive";
    public static final String AUTHORITY = "com.eui.sdk.install.provider";
    public static final Uri sUri = Uri.parse("content://" + AUTHORITY);

    public static File isApkExists(String md5) {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File apk = new File(dir, "update/" + md5 + ".apk");
        if (apk.exists()) {
            return apk;
        }
        return null;
    }

    public static File getTempFile(String md5) {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(dir, "update/" + md5 + ".temp");
        if (file.exists()) {
            file.delete();
        }
        return file;
    }

    public static Uri createApkFile(Context context, String md5) {
        ContentProviderClient client = null;
        try {
            client = context.getContentResolver().acquireContentProviderClient(sUri);
            if (client != null) {
                Bundle bundle = client.call("createApkFile", md5, null);
                if (bundle != null) {
                    return bundle.getParcelable("uri");
                }
            }
        } catch (RemoteException e) {
            LogUtils.w(TAG, e, "fail to get uri for apk.");
        } finally {
            if (client != null) {
                client.release();
            }
        }
        return null;
//        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        File file = new File(dir, "update/" + md5 + ".apk");
//        if (file.exists()) {
//            file.delete();
//        }
//        return file;
    }
}
