package com.github.daweizhou89.mbridge;

import android.app.Application;
import com.github.daweizhou89.mbridge.core.MBridge;
import com.github.daweizhou89.mbridge.core.MBridge_app;
import com.github.daweizhou89.mbridge.core.MBridge_modulea;
import com.github.daweizhou89.mbridge.core.MBridge_moduleb;

/**
 * Created by zhoudawei on 2018/12/18.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MBridge.register(MBridge_app.class)
                .register(MBridge_modulea.class)
                .register(MBridge_moduleb.class);
    }
}
