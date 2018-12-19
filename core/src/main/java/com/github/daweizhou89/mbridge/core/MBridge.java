package com.github.daweizhou89.mbridge.core;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daweizhou89 on 2017/8/30.
 */

public class MBridge {

    private static Map<String, IMBridgeService> sIntentActionsMap = new HashMap<>();

    private static List<IMBridge> sIntentHelperList = new ArrayList<>();

    public static <T extends IMBridge> RegisterWrapper register(Class<T> bridgeClass) {
        registerInternal(bridgeClass);
        return new RegisterWrapper();
    }

    private static <T extends IMBridge> void registerInternal(Class<T> bridgeClass) {
        IMBridge bridge = getInstance(bridgeClass);
        if (bridge != null) {
            sIntentHelperList.add(bridge);
            Class serviceClass = bridge.getMBridgeServiceClass();
            IMBridgeService intentActions = (IMBridgeService) getInstance(serviceClass);
            if (intentActions != null) {
                sIntentActionsMap.put(bridge.getModuleName(), intentActions);
            }
        }
    }

    private static <T> T getInstance(Class<T> clazz) {
        if (clazz == null) {
            return null;
        }
        try {
            Constructor constructor = clazz.getConstructor();
            return (T) constructor.newInstance();
        } catch (Exception e) {
            Log.e(MBridge.class.toString(), "getInstance:" + clazz, e);
        }
        return null;
    }

    public static void invoke(String moduleName, String action, Object... params) {
        IMBridgeService intentActions = sIntentActionsMap.get(moduleName);
        if (intentActions != null) {
            intentActions.invoke(action, params);
        } else {
            throw new RuntimeException("Module " + moduleName + " is not found.");
        }
    }

    public static DestinationWrapper intent() {
        return new DestinationWrapper();
    }

    public static DestinationWrapper intent(String path) {
        return new DestinationWrapper().path(path);
    }

    protected static int prepare(Intent intent, Context substitute, String path) {
        for (IMBridge bridge : sIntentHelperList) {
            int type = bridge.prepare(intent, substitute, path);
            if (type != DestinationWrapper.TYPE_NULL) {
                return type;
            }
        }
        return DestinationWrapper.TYPE_NULL;
    }

    public static boolean inject(Object target) {
        for (IMBridge bridge : sIntentHelperList) {
            if (bridge.inject(target)) {
                return true;
            }
        }
        return false;
    }

    public static class RegisterWrapper {
        public <T extends IMBridge> RegisterWrapper register(Class<T> bridgeClass) {
            registerInternal(bridgeClass);
            return new RegisterWrapper();
        }
    }

}
