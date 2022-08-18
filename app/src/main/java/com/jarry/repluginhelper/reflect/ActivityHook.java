package com.jarry.repluginhelper.reflect;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * 对Activity.startActivity()进行hook
 * P127
 */
public class ActivityHook {
    /***
     * 新建一个代理Instrumentation类，替换Activity中的mInstrumentation
     */
    public static void hook() {

    }


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
