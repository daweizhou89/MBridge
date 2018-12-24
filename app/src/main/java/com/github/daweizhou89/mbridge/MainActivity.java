package com.github.daweizhou89.mbridge;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.github.daweizhou89.mbridge.annotation.MBridgeDestination;
import com.github.daweizhou89.mbridge.core.MBridge;
import com.github.daweizhou89.modulea.AActivity_MBridge;
import com.github.daweizhou89.modulea.ModelA;

@MBridgeDestination(path = "main")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_a_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AActivity_MBridge.intent().modelA(new ModelA(1, "a"))
                        .num(1)
                        .start(MainActivity.this);

            }
        });

        findViewById(R.id.btn_a_activity_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MBridge.intent("://a?num=2")
                        .start(MainActivity.this);

            }
        });

        findViewById(R.id.btn_b_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MBridge.intent("b")
                        .putExtra("modelB", new ModelMain(2, "b"))
                        .start(MainActivity.this);

            }
        });
    }
}
