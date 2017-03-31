package com.le.samplecodecamp.eui.update;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by spring on 16-9-28.
 */
public class SystemProperties {

    public static String getEUIVersion(String key) {
        String value = "";
        Class<?> cls = null;

        try {
            cls = Class.forName("android.os.SystemProperties");
            Method hideMethod = cls.getMethod("get", String.class);
            Object object = cls.newInstance();
            value = (String) hideMethod.invoke(object, key);
        } catch (ClassNotFoundException e) {
            Log.e("ClassNotFoundException", "get error() ", e);
        } catch (NoSuchMethodException e) {
            Log.e("NoSuchMethodException", "get error() ", e);
        } catch (InstantiationException e) {
            Log.e("InstantiationException", "get error() ", e);
        } catch (IllegalAccessException e) {
            Log.e("IllegalAccessException", "get error() ", e);
        } catch (IllegalArgumentException e) {
            Log.e("ArgumentException", "get error() ", e);
        } catch (InvocationTargetException e) {
            Log.e("InvocationException", "get error() ", e);
        }

        return value;
    }
}
