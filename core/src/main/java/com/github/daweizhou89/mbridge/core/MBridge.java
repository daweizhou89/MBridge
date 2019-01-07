package com.github.daweizhou89.mbridge.core;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daweizhou89 on 2017/8/30.
 */

public class MBridge {

    private static Map<String, IMBridgeService> sBridgeServiceMap = new HashMap<>();

    private static List<IMBridge> sBridgeList = new ArrayList<>();

    public static <T extends IMBridge> RegisterWrapper register(Class<T> bridgeClass) {
        registerInternal(bridgeClass);
        return new RegisterWrapper();
    }

    private static <T extends IMBridge> void registerInternal(Class<T> bridgeClass) {
        IMBridge bridge = getInstance(bridgeClass);
        if (bridge != null) {
            sBridgeList.add(bridge);
            Class serviceClass = bridge.getMBridgeServiceClass();
            IMBridgeService intentActions = (IMBridgeService) getInstance(serviceClass);
            if (intentActions != null) {
                sBridgeServiceMap.put(bridge.getModuleName(), intentActions);
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

    public static <T> T invoke(String moduleName, String action, Object... params) {
        return invoke2(moduleName, action, null, params);
    }

    public static <T> T invoke2(String moduleName, String action, Type type, Object... params) {
        IMBridgeService intentActions = sBridgeServiceMap.get(moduleName);
        if (intentActions != null) {
            return (T) intentActions.invoke(action, type, params);
        } else {
            throw new RuntimeException("Module " + moduleName + " is not found in " + Utils.getAllModuleNames(sBridgeServiceMap));
        }
    }

    public static DestinationWrapper intent() {
        return new DestinationWrapper();
    }

    public static DestinationWrapper intent(String path) {
        return new DestinationWrapper().path(path);
    }

    protected static int prepare(Intent intent, Context substitute, String path) {
        for (IMBridge bridge : sBridgeList) {
            int type = bridge.prepare(intent, substitute, path);
            if (type != DestinationWrapper.TYPE_NULL) {
                return type;
            }
        }
        return DestinationWrapper.TYPE_NULL;
    }

    public static boolean inject(Object target) {
        for (IMBridge bridge : sBridgeList) {
            if (bridge.inject(target)) {
                return true;
            }
        }
        return false;
    }

    public static class RegisterWrapper {
        public <T extends IMBridge> RegisterWrapper register(Class<T> bridgeClass) {
            registerInternal(bridgeClass);
            return this;
        }
    }

}
