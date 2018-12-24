package com.github.daweizhou89.modulea;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.github.daweizhou89.mbridge.annotation.Autowired;
import com.github.daweizhou89.mbridge.annotation.MBridgeDestination;
import com.github.daweizhou89.mbridge.core.MBridge;

@MBridgeDestination(path = "a")
public class AActivity extends AppCompatActivity {

    @Autowired
    int num;

    @Autowired
    ModelA modelA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a);
        MBridge.inject(this);
        Log.e("dawei", "AActivity: num = " + num + ", modelA = " + modelA);
        MBridge.invoke("moduleb", "a");
        MBridge.invoke("moduleb", "b");
        findViewById(R.id.btn_main_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MBridge.intent("main").start(AActivity.this);
            }
        });
    }
}
