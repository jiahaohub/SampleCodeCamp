package com.le.samplecodecamp.eui.update;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by zhangjiahao on 17-4-5.
 */

public class PrefManager {

    private static final String sPrefName = "update.pref";

    public static void putInt(Context context, String name, int value) {
        SharedPreferences sp = context.getSharedPreferences(sPrefName, Context.MODE_PRIVATE);
        sp.edit().putInt(name, value).apply();
    }

    public static int getInt(Context context, String name, int defValue) {
        SharedPreferences sp = context.getSharedPreferences(sPrefName, Context.MODE_PRIVATE);
        return sp.getInt(name, defValue);
    }
}
