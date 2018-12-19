package com.github.daweizhou89.moduleb;

import android.util.Log;
import com.github.daweizhou89.mbridge.annotation.Action;
import com.github.daweizhou89.mbridge.annotation.MBridgeService;

/**
 * Created by zhoudawei on 2018/12/18.
 */
@MBridgeService
public class BService {

    @Action(action = "a")
    public void a() {
        Log.e("dawei", "BService:a");
    }

    @Action(action = "b")
    public void b() {
        Log.e("dawei", "BService:b");
    }

}
