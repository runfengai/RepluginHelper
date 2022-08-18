package com.jarry.repluginhelper.reflect;

import android.content.Context;
import android.content.pm.PackageManager;

import java.lang.reflect.Proxy;

public class ReflectHelper {
    /**
     * hook ActivityManagerNative
     */
    public static void hookAMN() {
        try {
            //获取AMN的gDefault单例gDefault，gDefault是静态的
            Object gDefault = RefInvoke.getStaticFieldObject("android.app.ActivityManagerNative", "gDefault");

            // gDefault是一个 android.util.Singleton对象; 我们取出这个单例里面的mInstance字段，IActivityManager类型
            Object rawIActivityManager = RefInvoke.getFieldObject(
                    "android.util.Singleton",
                    gDefault, "mInstance");


            // 创建一个这个对象的代理对象iActivityManagerInterface, 然后替换这个字段, 让我们的代理对象帮忙干活
            Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");
            Object proxy = Proxy.newProxyInstance(
                    Thread.currentThread().getContextClassLoader(),
                    new Class<?>[]{iActivityManagerInterface},
                    new HookHandler(rawIActivityManager));

            //把Singleton的mInstance替换为proxy
            RefInvoke.setFieldObject("android.util.Singleton", gDefault, "mInstance", proxy);

        } catch (Exception e) {
            throw new RuntimeException("Hook Failed", e);
        }
    }

    /**
     * 实际无法直接hook pms,在每个应用中有个ApplicationPackageManager,内部有个mPm字段（IPackageManager的实例）
     */
    public static void hookPMS(Context context) {
        try {
            // 获取全局的ActivityThread对象
            Object currentActivityThread = RefInvoke.getStaticFieldObject("android.app.ActivityThread", "sCurrentActivityThread");

            // 获取ActivityThread里面原始的 sPackageManager
            Object sPackageManager = RefInvoke.getFieldObject(currentActivityThread, "sPackageManager");


            // 准备好代理对象, 用来替换原始的对象
            Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
            Object proxy = Proxy.newProxyInstance(iPackageManagerInterface.getClassLoader(),
                    new Class<?>[]{iPackageManagerInterface},
                    new HookHandler(sPackageManager));

            // 1. 替换掉ActivityThread里面的 sPackageManager 字段
            RefInvoke.setFieldObject(currentActivityThread, "sPackageManager", proxy);

            // 2. 替换 ApplicationPackageManager里面的 mPm对象
            PackageManager pm = context.getPackageManager();
            RefInvoke.setFieldObject(pm, "mPM", proxy);

        } catch (Exception e) {
            throw new RuntimeException("hook failed", e);
        }


    }
}
