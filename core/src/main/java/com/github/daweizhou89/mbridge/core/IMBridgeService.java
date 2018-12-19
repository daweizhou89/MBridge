package com.github.daweizhou89.mbridge.core;

/**
 * Created by daweizhou89 on 2017/8/30.
 */

public interface IMBridgeService {
    void invoke(String action, Object... params);
}
