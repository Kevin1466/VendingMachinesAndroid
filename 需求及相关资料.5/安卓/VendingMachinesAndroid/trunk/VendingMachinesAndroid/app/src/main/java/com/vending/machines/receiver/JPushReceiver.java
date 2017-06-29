package com.vending.machines.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.vending.machines.push.message.PushMessage;


public class JPushReceiver extends BroadcastReceiver {
    private static OnReceivePaySuccess onReceivePaySuccesse;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null)
            return;
        try {
            Bundle bundle = intent.getExtras();
            Log.i("JPushReceiver", "bundle: " + bundle);
            Log.i("JPushReceiver", "action: " + intent.getAction());
            if ("cn.jpush.android.intent.MESSAGE_RECEIVED".equals(intent.getAction())) {
                String msg = bundle.getString(JPushInterface.EXTRA_MESSAGE);
                Log.i("JPushReceiver", "msg: " + msg);
                PushMessage pushMessage = JSON.parseObject(msg, PushMessage.class);
                Log.i("JPushReceiver", "pushMessage: " + pushMessage);
                handle(pushMessage);
            } else if ("cn.jpush.android.intent.REGISTRATION".equals(intent.getAction())) {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handle(PushMessage pushMessage) {
        Log.i("JPushReceiver", "pushMessage.getMessageType(): " + pushMessage.getMessageType());
        switch (pushMessage.getMessageType()) {
            case PAY_SUCCESS:
                String orderNum = ((JSONObject) pushMessage.getData()).getString("orderNum");
                Log.i("JPushReceiver", "onReceivePaySuccesse: " + onReceivePaySuccesse);
                if (onReceivePaySuccesse != null) {
                    onReceivePaySuccesse.onReceive(orderNum);
                }
            default:
        }
    }


    public static interface OnReceivePaySuccess {
        public void onReceive(String orderNum);
    }

    public static OnReceivePaySuccess getOnReceivePaySuccesse() {
        return onReceivePaySuccesse;
    }

    public static void setOnReceivePaySuccesse(OnReceivePaySuccess onReceivePaySuccesse) {
        JPushReceiver.onReceivePaySuccesse = onReceivePaySuccesse;
    }
}
