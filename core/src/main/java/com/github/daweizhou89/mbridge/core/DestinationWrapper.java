package com.github.daweizhou89.mbridge.core;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by daweizhou89 on 2017/8/30.
 */

public class DestinationWrapper {

    public static final int TYPE_NULL = 0;

    public static final int TYPE_ACTIVITY = 1;

    public static final int TYPE_SERVICE = 2;

    public static final int TYPE_FRAGMENT_V4 = 3;

    protected Intent intent = new Intent();
    protected String path;
    protected int type = TYPE_NULL;

    public DestinationWrapper path(String path) {
        this.path = path;
        return this;
    }

    public DestinationWrapper action(String action) {
        intent.setAction(action);
        return this;
    }

    public DestinationWrapper data(Uri uri) {
        intent.setData(uri);
        return this;
    }

    public DestinationWrapper packageName(String packageName) {
        intent.setPackage(packageName);
        return this;
    }

    public DestinationWrapper component(ComponentName component) {
        intent.setComponent(component);
        return this;
    }

    public DestinationWrapper className(String packageName, String className) {
        intent.setClassName(packageName, className);
        return this;
    }

    public DestinationWrapper addCategory(String category) {
        intent.addCategory(category);
        return this;
    }

    public DestinationWrapper setFlags(int flags) {
        intent.setFlags(flags);
        return this;
    }

    public DestinationWrapper addFlags(int flags) {
        intent.addFlags(flags);
        return this;
    }

    public DestinationWrapper putExtra(String name, boolean value) {
        intent.putExtra(name, value);
        return this;
    }

    public DestinationWrapper putExtra(String name, byte value) {
        intent.putExtra(name, value);
        return this;
    }

    public DestinationWrapper putExtra(String name, short value) {
        intent.putExtra(name, value);
        return this;
    }

    public DestinationWrapper putExtra(String name, int value) {
        intent.putExtra(name, value);
        return this;
    }

    public DestinationWrapper putExtra(String name, long value) {
        intent.putExtra(name, value);
        return this;
    }

    public DestinationWrapper putExtra(String name, float value) {
        intent.putExtra(name, value);
        return this;
    }

    public DestinationWrapper putExtra(String name, double value) {
        intent.putExtra(name, value);
        return this;
    }

    public DestinationWrapper putExtra(String name, String value) {
        intent.putExtra(name, value);
        return this;
    }

    public DestinationWrapper putExtra(String name, CharSequence value) {
        intent.putExtra(name, value);
        return this;
    }

    public DestinationWrapper putExtra(String name, Parcelable value) {
        intent.putExtra(name, value);
        return this;
    }

    public DestinationWrapper putExtra(String name, Object value) {
        if (value != null) {
            String json = null;
            try {
                json = new Gson().toJson(value);
            } catch (Exception e) {
                Log.e(DestinationWrapper.class.toString(), "putExtra", e);
            }
            intent.putExtra(name, json);
        }
        return this;
    }

    public DestinationWrapper putParcelableArrayListExtra(String name, ArrayList<? extends Parcelable> value) {
        intent.putParcelableArrayListExtra(name, value);
        return this;
    }

    public Intent getIntent(Context substitute) {
        prepare(substitute);
        return intent;
    }

    public <T> T start(Context substitute) {
        prepare(substitute);
        switch (type) {
            case TYPE_ACTIVITY:
                substitute.startActivity(intent);
                break;
            case TYPE_FRAGMENT_V4:
                Class fragmentMeta = getFragmentMeta(intent);
                if (fragmentMeta != null) {
                    try {
                        Object instance = fragmentMeta.getConstructor().newInstance();
                        if (instance instanceof android.support.v4.app.Fragment) {
                            android.support.v4.app.Fragment fragment = (android.support.v4.app.Fragment) instance;
                            if (intent != null) {
                                fragment.setArguments(intent.getExtras());
                            }
                        }
                        return (T) instance;
                    } catch (Exception e) {
                        Log.e(DestinationWrapper.class.toString(), "start", e);
                    }
                }
                break;
            case TYPE_SERVICE:
            default:
                break;
        }
        return null;
    }

    public void start(Activity substitute, int requestCode) {
        prepare(substitute);
        substitute.startActivityForResult(intent, requestCode);
    }

    public void start(Fragment substitute) {
        prepare(substitute.getContext());
        substitute.startActivity(intent);
    }

    public void start(Fragment substitute, int requestCode) {
        prepare(substitute.getContext());
        substitute.startActivityForResult(intent, requestCode);
    }

    protected void prepare(Context substitute) {
        if (!TextUtils.isEmpty(path)) {
            if (path.contains("://")) {
                prepareOfScheme(substitute);
            } else {
                type = MBridge.prepare(intent, substitute, path);
            }
        }
    }

    private void prepareOfScheme(Context substitute) {
        if (path.startsWith("http")) {
            intent.putExtra("url", path);
            String newPath = "web/view";
            type = MBridge.prepare(intent, substitute, newPath);
            return;
        }
        try {
            Uri uri = Uri.parse(path);
            String newPath = uri.getHost() + uri.getPath();
            intent.setData(uri);
            type = MBridge.prepare(intent, substitute, newPath);
        } catch (Exception e) {
            Log.e(DestinationWrapper.class.toString(), "prepareOfScheme", e);
        }
    }

    public static int getType(Class clazz) {
        if (clazz == null) {
            return TYPE_NULL;
        }
        if (Activity.class.isAssignableFrom(clazz)) {
            return TYPE_ACTIVITY;
        } else if (Service.class.isAssignableFrom(clazz)) {
            return TYPE_SERVICE;
        } else if (Fragment.class.isAssignableFrom(clazz)) {
            return TYPE_FRAGMENT_V4;
        }
        return TYPE_NULL;
    }

    public static Class getFragmentMeta(Intent intent) {
        if (intent.getComponent() == null) {
            return null;
        }
        try {
            return Class.forName(intent.getComponent().getClassName());
        } catch (Exception e) {
            return null;
        }
    }

}
