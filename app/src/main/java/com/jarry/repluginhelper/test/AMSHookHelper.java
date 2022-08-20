package com.jarry.repluginhelper.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;

import com.jarry.repluginhelper.reflect.RefInvoke;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 1.启动一个没有注册过的Activity
 * 1.hook amn
 * 2.hook mH.mCallback
 * 3.hook instrumentation
 */
public class AMSHookHelper {
    public static final String TAG = "AMSHookHelper";

    /**
     * ================>>>>>1<<<<<<=============================
     * <p>
     * {@link android.app.ActivityManager}
     * Singleton
     * <a href=https://android.googlesource.com/platform/frameworks/base/+/742a67127366c376fdf188ff99ba30b27d3bf90c/core/java/android/util/Singleton.java
     * AMN
     * <a href="https://android.googlesource.com/platform/frameworks/base/+/742a67127366c376fdf188ff99ba30b27d3bf90c/core/java/android/app/ActivityManagerNative.java"/>
     * AMS
     * <a href="https://android.googlesource.com/platform/frameworks/base/+/refs/heads/froyo-release/services/java/com/android/server/am/ActivityManagerService.java"/>
     */
    public static void hookAMN() {
        Object obj = null;
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
//            obj = RefInvoke.getStaticFieldObject("android.app.ActivityManager", "IActivityManagerSingleton");
//        } else {
        obj = RefInvoke.getStaticFieldObject("android.app.ActivityManagerNative", "gDefault");
//        }
        final Object fieldObject = RefInvoke.getFieldObject("android.util.Singleton", obj, "mInstance");
        Class<?> clazz = null;
        try {
            clazz = Class.forName("android.app.IActivityManager");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Object proxy = Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{clazz},
//                new AMSHandler(fieldObject)
                new ActivityStartHandler(fieldObject)
        );
        RefInvoke.setFieldObject("android.util.Singleton", obj, "mInstance", proxy);
    }

    static class AMSHandler implements InvocationHandler {
        Object obj;

        public AMSHandler(Object obj) {
            this.obj = obj;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Log.e(TAG, "hook了AMN, IActivityManager: " + method.getName());

            return method.invoke(obj, args);
        }
    }

    /**
     * ================>>>>>2.<<<<<<=============================
     * hook mH.mCallback
     *
     * @link android.app.ActivityThread
     */
    public static void hookCallBack() {

        Object obj = RefInvoke.getStaticFieldObject("android.app.ActivityThread", "sCurrentActivityThread");
        final Handler mH = (Handler) RefInvoke.getFieldObject(obj, "mH");
        RefInvoke.setFieldObject(Handler.class, mH, "mCallback", new MyHandlerCallback(mH));
    }

    static class MyHandlerCallback implements Handler.Callback {
        Handler mHandler;

        public MyHandlerCallback(Handler mHandler) {
            this.mHandler = mHandler;
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    handleStartAct(msg);
                    break;
            }
            return false;
        }

        private void handleStartAct(Message msg) {
            Object obj = msg.obj;
            try {
                Intent intent = (Intent) RefInvoke.getFieldObject(obj, "intent");
                Intent target = intent.getParcelableExtra(ActivityStartHandler.EXTRA_TARGET_INTENT);
                intent.setComponent(target.getComponent());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * ================>>>>>3.<<<<<<=============================
     * hook ActivityThread.instrumentation
     */

    public static void hookInstrumentation() {
        /**
         * 静态方法获取ActivityThread
         */
        final Object activityThread = RefInvoke.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread");
        final Instrumentation instrumentation = (Instrumentation) RefInvoke.getFieldObject(activityThread, "mInstrumentation");
        RefInvoke.setFieldObject(activityThread, "mInstrumentation", new MyInstrumentation(instrumentation));
    }

    static class MyInstrumentation extends Instrumentation {
        Instrumentation ins;

        public MyInstrumentation(Instrumentation instrumentation) {
            this.ins = instrumentation;
        }

        @Override
        public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
            /**
             * 替身恢复真身
             */
            final Intent rawIntent = intent.getParcelableExtra(ActivityStartHandler.EXTRA_TARGET_INTENT);
            if (rawIntent == null) {
                return ins.newActivity(cl, className, intent);
            }
            final String newClassName = rawIntent.getComponent().getClassName();
            return ins.newActivity(cl, newClassName, rawIntent);
        }
    }
}
