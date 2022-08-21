package com.jarry.dynamicresource1;

import android.content.Context;

import com.jarry.mypluginlibrary.IDynamic;


public class Dynamic implements IDynamic {

    @Override
    public String getStringForResId(Context context) {
        return context.getResources().getString(R.string.plugin_name);
    }
}
