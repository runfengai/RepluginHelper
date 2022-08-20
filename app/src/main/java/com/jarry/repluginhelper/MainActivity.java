package com.jarry.repluginhelper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.jarry.repluginhelper.test01.AMSHookHelper;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        try {
//            AMSHook.hookAMN();
//            ActivityHook.hookHandlerCallBack();
            AMSHookHelper.hookAMN();
            AMSHookHelper.hookCallBack();
            AMSHookHelper.hookInstrumentation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //对当前activity的mInstrumenttation进行hook
//        Instrumentation mInstrumentation = (Instrumentation) RefInvoke.getFieldObject(Activity.class, this, "mInstrumentation");
//        Instrumentation evilInstrumentation = new ActivityHook.EvilInstrumentation(mInstrumentation);

//        RefInvoke.setFieldObject(Activity.class, this, "mInstrumentation", evilInstrumentation);


    }

    public void jumpTo2(View view) {
        Intent intent = new Intent(MainActivity.this, MainActivity2.class);
        startActivity(intent);
    }

    public void jumpToNoRegister(View view) {
        Intent intent = new Intent(MainActivity.this, NoRegisterActivity.class);
        startActivity(intent);
    }
}