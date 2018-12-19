package com.github.daweizhou89.mbridge.core;

import android.content.Context;
import android.content.Intent;

/**
 * Created by daweizhou89 on 2017/8/30.
 */

public interface IMBridge {
    boolean inject(Object target);
    int prepare(Intent intent, Context substitute, String path);
    String getModuleName();
    Class getMBridgeServiceClass();
}
