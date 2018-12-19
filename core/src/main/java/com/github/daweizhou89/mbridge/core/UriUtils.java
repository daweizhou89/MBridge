package com.github.daweizhou89.mbridge.core;

import android.net.Uri;

/**
 * Created by daweizhou89 on 2017/6/14.
 */

public class UriUtils {

    public static int getIntQueryParameter(Uri uri, String key, int defaultValue) {
        try {
            String value = uri.getQueryParameter(key);
            if (value == null) {
                return defaultValue;
            }
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static long getLongQueryParameter(Uri uri, String key, long defaultValue) {
        try {
            String value = uri.getQueryParameter(key);
            if (value == null) {
                return defaultValue;
            }
            return Long.parseLong(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean getBooleanQueryParameter(Uri uri, String key, boolean defaultValue) {
        try {
            String value = uri.getQueryParameter(key);
            if (value == null) {
                return defaultValue;
            }
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static short getShortQueryParameter(Uri uri, String key, short defaultValue) {
        try {
            String value = uri.getQueryParameter(key);
            if (value == null) {
                return defaultValue;
            }
            return Short.parseShort(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static float getFloatQueryParameter(Uri uri, String key, float defaultValue) {
        try {
            String value = uri.getQueryParameter(key);
            if (value == null) {
                return defaultValue;
            }
            return Float.parseFloat(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static double getDoubleQueryParameter(Uri uri, String key, double defaultValue) {
        try {
            String value = uri.getQueryParameter(key);
            if (value == null) {
                return defaultValue;
            }
            return Double.parseDouble(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String getStringQueryParameter(Uri uri, String key) {
        try {
            String value = uri.getQueryParameter(key);
            return value;
        } catch (Exception e) {
            return null;
        }
    }

}
