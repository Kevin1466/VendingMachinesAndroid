package com.vending.machines.util;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Date;

/**
 * Created by lishichao on 16/7/8.
 */
public class AutoUpdateRunner implements Runnable {

    public static final int delay = 1000 * 60 * 10; //10分钟

    public static final String startHour = "00:00:00";
    public static final String endHour = "00:15:00";
    private Handler handler;

    public AutoUpdateRunner(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        while (true) {
            Log.i(Constant.LOGTAG, "执行一个循环");
            if (isTimeOk()) {
                checkServerAppVersion();
            }

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断当前时间是否可以更新
     */
    public boolean isTimeOk() {
        return Utils.isInDate(new Date(), startHour, endHour);
    }

    public void checkServerAppVersion() {
        Message message = handler.obtainMessage();
        message.arg1 = Constant.HandlerArgs1_AutoUpdateHandler;
        handler.sendMessage(message);
    }
}
