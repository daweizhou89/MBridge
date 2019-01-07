package com.github.daweizhou89.mbridge.core;

import java.lang.reflect.Type;

/**
 * Created by daweizhou89 on 2017/8/30.
 */

public interface IMBridgeService {
    Object invoke(String action, Type type, Object... params);
}
