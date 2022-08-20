package com.jarry.repluginhelper.test01;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.jarry.repluginhelper.StubActivity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ActivityStartHandler implements InvocationHandler {
    public static final String EXTRA_TARGET_INTENT = "EXTRA_TARGET_INTENT";


    Object object;

    public ActivityStartHandler(Object object) {
        this.object = object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.e(AMSHookHelper.TAG, "startHandler method:" + method.getName());
        if ("startActivity".equals(method.getName())) {
            Intent intent = null;
            int index = 0;
            int len = args.length;
            for (int i = 0; i < len; i++) {
                if (args[i] instanceof Intent) {
                    index = i;
                    break;
                }
            }
            intent = (Intent) args[index];
            final String packageName = intent.getComponent().getPackageName();
            Intent newIntent = new Intent();
            ComponentName componentName = new ComponentName(packageName, StubActivity.class.getName());
            newIntent.setComponent(componentName);
            newIntent.putExtra(EXTRA_TARGET_INTENT, intent);
            args[index] = newIntent;

        }
        return method.invoke(object, args);
    }
}
