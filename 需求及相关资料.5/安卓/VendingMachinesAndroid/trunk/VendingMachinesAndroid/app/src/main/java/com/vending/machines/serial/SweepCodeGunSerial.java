package com.vending.machines.serial;


import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SweepCodeGunSerial {
    private static final String TAG = "SweepCodeGunSerial";
//    public static final String PORT = "USB1";
    public static final String PORT = "S2";
    public static int BOUND_RATE = 115200;
    private static SweepCodeGunSerial instance;

    private MachinesSerialPort serailPortOpt;
    private List<RequestDetail<String>> requestDetails = new CopyOnWriteArrayList<>();

    private Thread readThread = new Thread(){
        private String mQrData = "";

        @Override
        public void run() {
            while (true){
                if(SweepCodeGunSerial.this.available()){
                    try {
                        byte[] bs = new byte[2];
                        String data = serailPortOpt.receiveGunData("ASCII", bs);
                        if (data != null && serailPortOpt != null) {
                            try {
                                mQrData += data;
                                Log.i(TAG,"收到数据" + data);
                                if (bs[0] == 13 && bs[1] == 10) {
                                    String code = mQrData;
                                    handle(code);
                                    mQrData = "";
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                checkTimeOut();
                try {
                    if(requestDetails.isEmpty()) {
                        Thread.sleep(500);
                    }else{
                        Thread.sleep(50);
                    }
                } catch (Exception e) {}
            }
        }
    };

    public SweepCodeGunSerial() {
        readThread.setDaemon(true);
        readThread.start();
        open();
    }

    public void init(){

    }

    private void handle(String string){
        if(!this.requestDetails.isEmpty()) {
            ArrayList<RequestDetail<String>> requestDetails = new ArrayList<>();
            requestDetails.addAll(this.requestDetails);
            for (RequestDetail<String> requestDetail : requestDetails) {
                requestDetail.fireSuccess(string);
                this.requestDetails.remove(requestDetail);
            }
        }
    }

    private void checkTimeOut(){
        try {
            if (!requestDetails.isEmpty()) {
                ArrayList<RequestDetail<String>> requestDetails = new ArrayList<>();
                requestDetails.addAll(this.requestDetails);

                for (RequestDetail<String> requestDetail : requestDetails) {
                    if (System.currentTimeMillis() - requestDetail.getTime() >= requestDetail.getTimeout()) {
                        requestDetail.fireTimeout();
                        this.requestDetails.remove(requestDetail);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static SweepCodeGunSerial instance(){
        SweepCodeGunSerial instance = SweepCodeGunSerial.instance;
        if(instance == null){
            synchronized (SweepCodeGunSerial.class){
                instance = SweepCodeGunSerial.instance;
                if(instance == null){
                    instance = new SweepCodeGunSerial();
                    SweepCodeGunSerial.instance = instance;
                }
            }
        }
        return instance;
    }

    public void beginSweepCodeGunSerial(RequestHandler<String> handler, int timeout){
        requestDetails.clear();
        requestDetails.add(new RequestDetail<String>(handler, timeout));
        check();
        if(!available()) {
            open();
        }
    }

    private boolean available(){
        return serailPortOpt != null && serailPortOpt.isOpen();
    }

    private void open(){
        synchronized (this) {
            if (serailPortOpt != null) {
                serailPortOpt.closeSerial();
            }
            serailPortOpt = new MachinesSerialPort(PORT, BOUND_RATE, 8, 1, 'n');
        }
    }

    private void check(){
        if (!available()) {
            open();
        }
    }
}
