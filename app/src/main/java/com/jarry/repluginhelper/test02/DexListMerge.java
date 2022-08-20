package com.jarry.repluginhelper.test02;

import androidx.lifecycle.ViewModelProvider;

import com.jarry.repluginhelper.reflect.RefInvoke;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

public class DexListMerge {
    /**
     * 合并classLoader
     * @param cl
     * @param apkFile
     * @param optDexFile
     */
    public static void patchClassLoader(ClassLoader cl, File apkFile, File optDexFile) {
        //获取BaseDexClassLoader.pathList
        final Object pathListObj = RefInvoke.getFieldObject(DexClassLoader.class.getSuperclass(), cl, "pathList");
        Object[] dexElements = (Object[]) RefInvoke.getFieldObject(pathListObj, "dexElements");
        final Class<?> elementClass = dexElements.getClass().getComponentType();
        Object[] newInstances = (Object[]) Array.newInstance(elementClass, dexElements.length + 1);
        //构造插件Element(File dir, boolean isDirectory, File zip, DexFile dexFile) 高版本已弃用
        Class[] clazz = {File.class, boolean.class, File.class, DexFile.class};
        try {
            Object[] vals = {apkFile, false, optDexFile, DexFile.loadDex(apkFile.getCanonicalPath(), optDexFile.getAbsolutePath(), 0)};
            final Object object = RefInvoke.createObject(elementClass, clazz, vals);
            Object[] toAddElement = new Object[]{object};
            System.arraycopy(dexElements, 0, newInstances, 0, dexElements.length);
            System.arraycopy(toAddElement, 0, newInstances, dexElements.length, toAddElement.length);
            RefInvoke.setFieldObject(pathListObj, "dexElement", newInstances);//热修复类似
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
