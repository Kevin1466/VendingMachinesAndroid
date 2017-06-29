package com.vending.machines.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.vending.machines.util.Constant;
import com.vending.machines.util.Utils;


/**
 * Created by lishichao on 16/7/8.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean connected = Utils.isNetworkConnected(context);
        Log.i(Constant.LOGTAG, connected ? "网络已连接" : "网络已断开");
        Toast.makeText(context, (connected ? "网络已连接" : "网络已断开"), Toast.LENGTH_SHORT).show();
    }
}
