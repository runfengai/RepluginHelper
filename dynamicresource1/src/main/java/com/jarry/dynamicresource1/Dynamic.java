package com.jarry.dynamicresource1;

import android.content.Context;



public class Dynamic implements com.jarry.pluginlibrary.IDynamic {

    @Override
    public String getStringForResId(Context context) {
        return context.getResources().getString(R.string.plugin_name);
    }
}
