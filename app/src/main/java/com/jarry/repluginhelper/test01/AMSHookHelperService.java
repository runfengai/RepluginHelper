package com.jarry.repluginhelper.test01;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jarry.repluginhelper.reflect.RefInvoke;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

/**
 * 1.启动一个没有注册过的Service
 * 1.hook amn
 * 2.hook mH.mCallback
 * 3.hook instrumentation
 */
public class AMSHookHelperService {
    public static final String TAG = "AMSHookHelper";
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

    static class MockClass1 implements InvocationHandler {

        private static final String TAG = "MockClass1";

        // 替身StubService的包名
        private static final String stubPackage = "com.jarry.activityhook1";

        Object mBase;

        public MockClass1(Object base) {
            mBase = base;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            Log.e(TAG, method.getName());

            if ("startService".equals(method.getName())) {
                // 只拦截这个方法
                // 替换参数, 任你所为;甚至替换原始StubService启动别的Service偷梁换柱

                // 找到参数里面的第一个Intent 对象
                int index = 0;
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof Intent) {
                        index = i;
                        break;
                    }
                }

                //get StubService form PluginApplication.pluginServices
                Intent rawIntent = (Intent) args[index];
                String rawServiceName = rawIntent.getComponent().getClassName();

                String stubServiceName = PluginApplication.pluginServices.get(rawServiceName);

                // replace Plugin Service of StubService
                ComponentName componentName = new ComponentName(stubPackage, stubServiceName);
                Intent newIntent = new Intent();
                newIntent.setComponent(componentName);

                // Replace Intent, cheat AMS
                args[index] = newIntent;

                Log.d(TAG, "hook success");
                return method.invoke(mBase, args);
            } else if ("stopService".equals(method.getName())) {
                // 只拦截这个方法
                // 替换参数, 任你所为;甚至替换原始StubService启动别的Service偷梁换柱

                // 找到参数里面的第一个Intent 对象
                int index = 0;
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof Intent) {
                        index = i;
                        break;
                    }
                }

                //get StubService form PluginApplication.pluginServices
                Intent rawIntent = (Intent) args[index];
                String rawServiceName = rawIntent.getComponent().getClassName();
                String stubServiceName = PluginApplication.pluginServices.get(rawServiceName);

                // replace Plugin Service of StubService
                ComponentName componentName = new ComponentName(stubPackage, stubServiceName);
                Intent newIntent = new Intent();
                newIntent.setComponent(componentName);

                // Replace Intent, cheat AMS
                args[index] = newIntent;

                Log.d(TAG, "hook success");
                return method.invoke(mBase, args);
            }

            return method.invoke(mBase, args);
        }
    }

    class MockClass2 implements Handler.Callback {

        Handler mBase;

        public MockClass2(Handler base) {
            mBase = base;
        }

        @Override
        public boolean handleMessage(Message msg) {

            Log.d(TAG, String.valueOf(msg.what));
            switch (msg.what) {
                // ActivityThread里面 "CREATE_SERVICE" 这个字段的值是114
                case 114:
                    handleCreateService(msg);
                    break;
            }

            mBase.handleMessage(msg);
            return true;
        }

        private void handleCreateService(Message msg) {
            // 这里简单起见,直接取出插件Servie

            Object obj = msg.obj;
            ServiceInfo serviceInfo = (ServiceInfo) RefInvoke.getFieldObject(obj, "info");

            String realServiceName = null;//预埋的service

            for (String key : PluginApplication.pluginServices.keySet()) {
                String value = PluginApplication.pluginServices.get(key);
                if (value.equals(serviceInfo.name)) {
                    realServiceName = key;
                    break;
                }
            }

            serviceInfo.name = realServiceName;
        }
    }

    static class PluginApplication extends Application {
        private static Context sContext;
        /**
         * 可以建立插件service和预埋的service的映射关系
         */
        public static HashMap<String, String> pluginServices;

        @Override
        protected void attachBaseContext(Context base) {
            super.attachBaseContext(base);
            sContext = base;

            pluginServices = new HashMap<String, String>();
        }

        public static Context getContext() {
            return sContext;
        }
    }

}
