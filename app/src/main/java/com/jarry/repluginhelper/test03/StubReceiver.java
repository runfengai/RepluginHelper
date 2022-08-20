package com.jarry.repluginhelper.test03;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StubReceiver extends BroadcastReceiver {
    public StubReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String newAction = intent.getAction();
        if(ReceiverManager.pluginReceiverMappings.containsKey(newAction)) {
            String oldAction = ReceiverManager.pluginReceiverMappings.get(newAction);
            context.sendBroadcast(new Intent(oldAction));
        }
    }
}
