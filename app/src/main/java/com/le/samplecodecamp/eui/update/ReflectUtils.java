package com.le.samplecodecamp.eui.update;

import android.telephony.TelephonyManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by zhangjiahao on 16-12-19.
 */

public class ReflectUtils {

    public static String getLEUIDeviceId(TelephonyManager telephonyManager) {
        try {
            Method method = telephonyManager.getClass()
                    .getDeclaredMethod("getLEUIDeviceId");
            return (String) method.invoke(telephonyManager);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return telephonyManager.getDeviceId();
    }

    public static boolean isDebugEnv() {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getMethod("getBoolean", String.class, boolean.class);
            return (boolean) method.invoke(null, "debug.upgrade.aar", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
