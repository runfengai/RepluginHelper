package com.jarry.repluginhelper.reflect;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * 对Activity.startActivity()进行hook
 * P127
 */
public class ActivityHook {
    static final String TAG = "ActivityHook";

    /**
     * 替换callback，
     */
    public static void hookHandlerCallBack() {
        // 获取全局的ActivityThread对象
        try {
            Object currentActivityThread = RefInvoke.getStaticFieldObject("android.app.ActivityThread", "sCurrentActivityThread");
            final Object mH = RefInvoke.getFieldObject("android.app.ActivityThread", currentActivityThread, "mH");
            RefInvoke.setFieldObject(Handler.class, mH, "mCallBack", new MyCallBack(mH));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class MyCallBack implements Handler.Callback {
        private Object mBase;

        public MyCallBack(Object mBase) {
            this.mBase = mBase;
        }

        @Override
        public boolean handleMessage(@NonNull Message msg) {
            Log.e(TAG, ">>>>>hook ！！！" + msg.what);
            switch (msg.what) {
                case 100:
                    Log.e(TAG, ">>>>>hook 100！！！");
                    break;
            }
            if (mBase instanceof Handler) {
                ((Handler) mBase).handleMessage(msg);
            }
            return false;
        }
    }

    /**
     * 新建一个代理Instrumentation类，替换Activity中的mInstrumentation
     */
    public static class EvilInstrumentation extends Instrumentation {
        private Instrumentation mBase;

        public EvilInstrumentation(Instrumentation mBase) {
            this.mBase = mBase;
        }

        /**
         * hook了execStartActivity方法
         *
         * @param who
         * @param contextThread
         * @param token
         * @param target
         * @param intent
         * @param requestCode
         * @param options
         * @return
         */
        public ActivityResult execStartActivity(
                Context who, IBinder contextThread, IBinder token, Activity target,
                Intent intent, int requestCode, Bundle options) {
            Log.e("AAA", "<<<hook了！！！！！！！！>>>");
            Class[] p1 = {Context.class, IBinder.class,
                    IBinder.class, Activity.class,
                    Intent.class, int.class, Bundle.class};
            Object[] v1 = {who, contextThread, token, target,
                    intent, requestCode, options};
            return (ActivityResult) RefInvoke.invokeInstanceMethod(mBase, "execStartActivity", p1, v1);
        }
    }
}
