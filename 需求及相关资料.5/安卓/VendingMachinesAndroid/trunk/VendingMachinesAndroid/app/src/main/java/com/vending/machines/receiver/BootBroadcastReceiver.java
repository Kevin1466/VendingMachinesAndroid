package com.vending.machines.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.vending.machines.activity.MainActivity;

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_BOOT)) {
            Intent ootStartIntent = new Intent(context, MainActivity.class);
            ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(ootStartIntent);
        }
    }
}
