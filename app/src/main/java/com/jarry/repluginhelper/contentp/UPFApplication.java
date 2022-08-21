package com.jarry.repluginhelper.contentp;

import android.app.Application;
import android.content.Context;

import com.jarry.repluginhelper.contentp.hook.BaseDexClassLoaderHookHelper;

import java.io.File;

/**
 * 一定需要Application，并且在attachBaseContext里面Hook
 * 因为provider的初始化非常早，比Application的onCreate还要早
 * 在别的地方hook都晚了。
 */
public class UPFApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        try {
            File apkFile = getFileStreamPath("plugin2.apk");
            if (!apkFile.exists()) {
                Utils.extractAssets(base, "plugin2.apk");
            }

            File odexFile = getFileStreamPath("plugin2.odex");

            // Hook ClassLoader, 让插件中的类能够被成功加载
            BaseDexClassLoaderHookHelper.patchClassLoader(getClassLoader(), apkFile, odexFile);

            //安装插件中的Providers
            ProviderHelper.installProviders(base, getFileStreamPath("plugin2.apk"));
        } catch (Exception e) {
            throw new RuntimeException("hook failed", e);
        }
    }
}
