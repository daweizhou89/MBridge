package com.github.daweizhou89.mbridge.core;

import java.util.Map;
import java.util.Set;

/**
 * Created by zhoudawei on 2019/1/7.
 */
public class Utils {

    public static String getAllModuleNames(Map<String, IMBridgeService> bridgeServiceMap) {
        StringBuilder allModuleNames = new StringBuilder();
        allModuleNames.append('[');
        Set<String> moduleNameSet = bridgeServiceMap.keySet();
        if (moduleNameSet != null && !moduleNameSet.isEmpty()) {
            int count = moduleNameSet.size();
            for (String moduleName : moduleNameSet) {
                allModuleNames.append(moduleName);
                count--;
                if (count > 0) {
                    allModuleNames.append(", ");
                }
            }
        }
        allModuleNames.append(']');
        return allModuleNames.toString();
    }

}
