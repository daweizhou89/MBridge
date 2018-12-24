package com.github.daweizhou89.moduleb;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import com.github.daweizhou89.mbridge.annotation.Autowired;
import com.github.daweizhou89.mbridge.annotation.MBridgeDestination;
import com.github.daweizhou89.mbridge.core.MBridge;

@MBridgeDestination(path = "b")
public class BActivity extends AppCompatActivity {

    @Autowired
    ModelB modelB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b);
        MBridge.inject(this);
        Log.e("dawei", "BActivity:modelB = " + modelB);
        MBridge.invoke("modulea", "a");
        MBridge.invoke("modulea", "b");
        findViewById(R.id.btn_main_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MBridge.intent("main").start(BActivity.this);
            }
        });
    }
}
