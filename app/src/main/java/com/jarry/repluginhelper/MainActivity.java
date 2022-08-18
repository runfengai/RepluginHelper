package com.jarry.repluginhelper;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.jarry.repluginhelper.reflect.ActivityHook;
import com.jarry.repluginhelper.reflect.RefInvoke;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //todo
        Instrumentation mInstrumentation = (Instrumentation) RefInvoke.getFieldObject(Activity.class, this, "mInstrumentation");
        Instrumentation evilInstrumentation = new ActivityHook.EvilInstrumentation(mInstrumentation);

        RefInvoke.setFieldObject(Activity.class, this, "mInstrumentation", evilInstrumentation);

        Button tv = new Button(this);
        tv.setText("测试界面");
        setContentView(tv);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                startActivity(intent);
            }
        });
    }
}