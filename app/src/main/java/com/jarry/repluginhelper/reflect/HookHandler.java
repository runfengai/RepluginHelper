package com.jarry.repluginhelper.reflect;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class HookHandler implements InvocationHandler {
    private Object mBase;

    public HookHandler(Object mBase) {
        this.mBase = mBase;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.e("", "hook");
        return method.invoke(mBase, args);
    }
}
