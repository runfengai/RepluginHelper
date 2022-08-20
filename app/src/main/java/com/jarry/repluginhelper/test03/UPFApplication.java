package com.jarry.repluginhelper.test03;

import android.app.Application;
import android.content.Context;

import com.jarry.repluginhelper.test03.classloder_hook.BaseDexClassLoaderHookHelper;

import java.io.File;



/**
 * 这个类只是为了方便获取全局Context的.
 *
 * @author weishu
 * @date 16/3/29
 */
public class UPFApplication extends Application {

    String apkName = "receivertest.apk";
    String dexName = "receivertest.dex";

    private static Context sContext;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sContext = base;

        //解压到本地
        Utils.extractAssets(this, apkName);

        File dexFile = getFileStreamPath(apkName);
        File optDexFile = getFileStreamPath(dexName);

        try {
            BaseDexClassLoaderHookHelper.patchClassLoader(getClassLoader(), dexFile, optDexFile);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        File testPlugin = getFileStreamPath(apkName);
        ReceiverHelper.preLoadReceiver(this, testPlugin);
    }

    public static Context getContext() {
        return sContext;
    }
}
