package com.github.daweizhou89.mbridge.core;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Created by daweizhou89 on 2017/9/1.
 */

public class GsonUtils {

    public static <T> T getParam(Uri uri, String key, Type type) {
        String param = UriUtils.getStringQueryParameter(uri, key);
        if (TextUtils.isEmpty(param)) {
            return null;
        }
        try {
            return new Gson().fromJson(param, type);
        } catch (Throwable t) {
            Log.e("GsonUtils", key + "-" + param, t);
        }
        return null;
    }

    public static <T> T getExtra(Bundle extras, String key, Type type) {
        String json = extras.getString(key);
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        try {
            return new Gson().fromJson(json, type);
        } catch (Throwable t) {
            Log.e("GsonUtils", key + "-" + json, t);
        }
        return null;
    }

    public static void putExtra(Intent intent, String key, Object value) {
        if (value == null) {
            return;
        }
        try {
            String json = new Gson().toJson(value);
            intent.putExtra(key, json);
        } catch (java.lang.Throwable t) {
            Log.e("GsonUtils", key, t);
        }
    }

    public static <T> T getValue(Object value, Type type, Class<T> clazz) {
        if (value == null || type == null) {
            return null;
        }
        T newValue = null;
        try {
            Gson gson = new Gson();
            String json = gson.toJson(value);
            Log.d("GsonUtils", "value:" + value + ", type:" + type);
            newValue = gson.fromJson(json, type);
        } catch (java.lang.Throwable t) {
            Log.e("GsonUtils", "value:" + value + ", type:" + type, t);
        }
        return newValue;
    }

}
