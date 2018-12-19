package com.github.daweizhou89.mbridge.compiler;

import com.squareup.javapoet.ClassName;

/**
 * Created by daweizhou89 on 2017/7/21.
 */

public class Consts {

    public static final String APT_CORE_PACKAGENAME = "com.github.daweizhou89.mbridge.core";

    public static final ClassName CLASS_NAME_OBJECT = ClassName.get("java.lang", "Object");

    public static final ClassName CLASS_NAME_STRING = ClassName.get("java.lang", "String");

    public static final ClassName CLASS_NAME_INTENT = ClassName.get("android.content", "Intent");

    public static final ClassName CLASS_NAME_BUNDLE = ClassName.get("android.os", "Bundle");

    public static final ClassName CLASS_NAME_ACTIVITY = ClassName.get("android.app", "Activity");

    public static final ClassName CLASS_NAME_FRAGMENT_V4 = ClassName.get("android.support.v4.app", "Fragment");

    public static final ClassName CLASS_NAME_CONTEXT = ClassName.get("android.content", "Context");

    public static final ClassName CLASS_NAME_GSON = ClassName.get("com.google.gson", "Gson");

    public static final ClassName CLASS_NAME_TYPE_TOKEN = ClassName.get("com.google.gson.reflect", "TypeToken");

    public static final ClassName CLASS_NAME_LOG = ClassName.get("android.util", "Log");

    public static final ClassName CLASS_NAME_URI = ClassName.get("android.net", "Uri");

    public static final ClassName CLASS_NAME_URI_UTILS = ClassName.get(APT_CORE_PACKAGENAME, "UriUtils");

    public static final ClassName CLASS_NAME_GSON_UTILS = ClassName.get(APT_CORE_PACKAGENAME, "GsonUtils");

    public static final ClassName CLASS_NAME_DESTINATION_WRAPPER = ClassName.get(APT_CORE_PACKAGENAME, "DestinationWrapper");

    public static final ClassName CLASS_NAME_RUNTIME_EXCEPTION = ClassName.get("java.lang", "RuntimeException");

    // System interface
    public static final String INTENT = "android.content.Intent";
    public static final String CONTEXT = "android.content.Context";
    public static final String ACTIVITY = "android.app.Activity";
    public static final String FRAGMENT = "android.app.Fragment";
    public static final String FRAGMENT_V4 = "android.support.v4.app.Fragment";
    public static final String SERVICE = "android.app.Service";
    public static final String BUNDLE = "android.os.Bundle";
    public static final String PARCELABLE = "android.os.Parcelable";

    // Java type
    private static final String LANG = "java.lang";
    public static final String BYTE = LANG + ".Byte";
    public static final String SHORT = LANG + ".Short";
    public static final String INTEGER = LANG + ".Integer";
    public static final String INTEGER_WRAPPERS = "int";
    public static final String LONG = LANG + ".Long";
    public static final String LONG_WRAPPERS = "long";
    public static final String FLOAT = LANG + ".Float";
    public static final String FLOAT_WRAPPERS = "float";
    public static final String DOUBEL = LANG + ".Double";
    public static final String BOOLEAN = LANG + ".Boolean";
    public static final String BOOLEAN_WRAPPERS = "boolean";
    public static final String STRING = LANG + ".String";
    public static final String CHARSEQUENCE = LANG + ".CharSequence";
    public static final String THROWABLE = LANG + ".Throwable";

    public static final String KEY_MODULE_NAME = "moduleName";

    public static final String CLASS_SIMPLE_NAME_DESTINATION_WRAPPER = "DestinationWrapper";
    public static final String CLASS_SIMPLE_NAME_IMBRIDGE = "IMBridge";
    public static final String CLASS_SIMPLE_NAME_IMBRIDGE_SERVICE = "IMBridgeService";
    public static final String CLASS_PREFIX_MBRIDGE = "MBridge_";
    public static final String CLASS_PREFIX_MBRIDGE_SERVICE = "MBridgeService_";
    public static final String CLASS_SUFFIX_MBRIDGE = "_MBridge";
}
