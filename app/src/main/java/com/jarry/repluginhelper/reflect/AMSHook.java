package com.jarry.repluginhelper.reflect;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class AMSHook {
    public static final String TAG = "AMSHook";

    /**
     * hook ActivityManagerNative,在Android 10新版本不太可行。
     */
    public static final String EXTRA_TARGET_INTENT = "extra_target_intent";

    public static void hookAMN() throws ClassNotFoundException,
            NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, NoSuchFieldException {

        //获取AMN的gDefault单例gDefault，gDefault是final静态的
        Object gDefault = RefInvoke.getStaticFieldObject("android.app.ActivityManagerNative", "gDefault");

        // gDefault是一个 android.util.Singleton<T>对象; 我们取出这个单例里面的mInstance字段
        Object mInstance = RefInvoke.getFieldObject("android.util.Singleton", gDefault, "mInstance");

        // 创建一个这个对象的代理对象MockClass1, 然后替换这个字段, 让我们的代理对象帮忙干活
        Class<?> classB2Interface = Class.forName("android.app.IActivityManager");
        Object proxy = Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{classB2Interface},
                new MockClass1(mInstance));

        //把gDefault的mInstance字段，修改为proxy
        RefInvoke.setFieldObject("android.util.Singleton", gDefault, "mInstance", proxy);
    }

    public static class MockClass1 implements InvocationHandler {
        private Object mBase;

        public MockClass1(Object mBase) {
            this.mBase = mBase;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("startActivity".equals(method.getName())) {
                Log.e(TAG, "hook了 AMS相关的mInstance！！！！！");
            }
            return method.invoke(mBase, args);
        }
    }
}


