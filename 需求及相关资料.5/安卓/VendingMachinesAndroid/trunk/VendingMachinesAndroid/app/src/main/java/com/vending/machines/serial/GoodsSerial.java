package com.vending.machines.serial;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.vending.machines.AppContext;
import com.vending.machines.AppManager;
import com.vending.machines.model.OrderInfo;
import com.vending.machines.model.OrderStatus;
import com.vending.machines.model.RestResponse;
import com.vending.machines.serial.message.BaseMessage;
import com.vending.machines.serial.message.DeliveryConfirmMessage;
import com.vending.machines.serial.message.DeliveryProgressType;
import com.vending.machines.serial.message.DeliveryRequestMessage;
import com.vending.machines.serial.message.DeliveryResponseMessage;
import com.vending.machines.serial.message.MessageCodec;
import com.vending.machines.serial.message.QueryInventoryConfirmMessage;
import com.vending.machines.serial.message.QueryInventoryRequestMessage;
import com.vending.machines.serial.message.QueryInventoryResponseMessage;
import com.vending.machines.serial.message.StockStatusType;
import com.vending.machines.util.ByteUtil;
import com.vending.machines.util.Constant;
import com.vending.machines.util.Helper;
import com.vending.machines.util.Utils;
import cz.msebera.android.httpclient.Header;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class GoodsSerial {
    private static final String TAG = "GoodsSerial";
    public static final String PORT = "S0";
    public static final int BOUND_RATE = 9600;
    public static final int MAX_RETRYTIMES = 4;
    private static GoodsSerial instance;
    private MachinesSerialPort serailPortOpt;
    AsyncHttpClient client = new AsyncHttpClient();
    AtomicInteger failure = new AtomicInteger(0);
    private ConcurrentHashMap<String, Boolean> reportsDeliveryMap = new ConcurrentHashMap<String, Boolean>();

    private List<RequestDetail<StockStatusType>> queryInventoryRequestDetails = new CopyOnWriteArrayList<>();
    private List<RequestDetail<DeliveryProgressType>> deliveryRequestDetails = new CopyOnWriteArrayList<>();

    private static final int TIME = 60 * 1000;
    private StockStatusType stockStatusType;


    Handler handler=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, TIME);

            if(!Utils.isNetWorkAvailable()){
                return;
            }

            if(stockStatusType == null) {
                queryInventory(new RequestHandler<StockStatusType>() {
                    @Override
                    public void onSuccess(StockStatusType stockStatusType) {

                    }

                    @Override
                    public void onTimeout() {

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }
                }, 20000);
            }

            final RequestParams requestParams = new RequestParams();
            requestParams.add("imei", Helper.getImei());

            requestParams.add("stockStatus", stockStatusType == null ? StockStatusType.UNKNOWN.getCode() : stockStatusType.getCode());
            requestParams.add("deviceStatus", failure.get() >= 5 ? "BREAKDOWN" : "ON_LINE");
            requestParams.add("longitude", "-1");
            requestParams.add("latitude", "-1");
            requestParams.add("APKVersion", Utils.getVersionNumber(AppContext.instance()));
            requestParams.add("HardVerion", Constant.My_HD_Version);

            Activity activity = AppManager.getAppManager().currentActivity();

            if(activity != null){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        client.post(Constant.REPORT_STATUS, requestParams, new TextHttpResponseHandler("utf-8") {

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                Log.e(TAG,"上报失败");
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                                Log.e(TAG,"上报成功" + responseString);
                            }
                        });
                    }
                });
            }
        }
    };




    private Thread readThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (GoodsSerial.this.available()) {
                    try {
                        byte[] bytes = serailPortOpt.receiveData();
                        if (bytes != null && bytes.length > 0) {
                            try {
                                Log.i(TAG, "收到的bytes是" + ByteUtil.outputHexofByte(bytes));
                                BaseMessage baseMessage = MessageCodec.decode(bytes);
                                Log.i(TAG, "解码的消息是" + baseMessage);
                                if (baseMessage instanceof QueryInventoryResponseMessage) {
                                    serailPortOpt.sendData(MessageCodec.encode(new QueryInventoryConfirmMessage()));
                                    handle((QueryInventoryResponseMessage) baseMessage);
                                } else if (baseMessage instanceof DeliveryResponseMessage) {
                                    handle((DeliveryResponseMessage) baseMessage);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "解码失败", e);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                checkTimeOut();
                try {
                    if (queryInventoryRequestDetails.isEmpty() && deliveryRequestDetails.isEmpty()) {
                        sendLeftReportsDelivery();
                        Thread.sleep(500);
                    } else {
                        Thread.sleep(50);
                    }
                } catch (Exception e) {
                }
            }
        }
    });

    private void checkTimeOut() {
        try {
            if (!queryInventoryRequestDetails.isEmpty()) {
                ArrayList<RequestDetail<StockStatusType>> requestDetails = new ArrayList<>();
                requestDetails.addAll(queryInventoryRequestDetails);

                for (RequestDetail<StockStatusType> requestDetail : requestDetails) {
                    if (System.currentTimeMillis() - requestDetail.getTime() >= requestDetail.getTimeout()) {
                        failure.incrementAndGet();
                        requestDetail.fireTimeout();
                        reportsDelivery((String) requestDetail.attr("orderNum"), false);
                        queryInventoryRequestDetails.remove(requestDetail);
                    }else if(System.currentTimeMillis() - requestDetail.getRetryDate() >= 2 * 1000){
                        if(requestDetail.getRetryTimes() - 1 < MAX_RETRYTIMES) {
                            try {
                                serailPortOpt.sendData(MessageCodec.encode(new QueryInventoryRequestMessage()));
                                Log.e(TAG, "send retry");
                            }catch (Exception e){}
                            finally {
                                requestDetail.updateRetryDate();
                                requestDetail.incrRetryTimes();
                            }
                        }else if(requestDetail.getRetryTimes() - 1 == MAX_RETRYTIMES){
                            failure.incrementAndGet();
                            requestDetail.fireTimeout();
                            reportsDelivery((String) requestDetail.attr("orderNum"), false);
                            queryInventoryRequestDetails.remove(requestDetail);
                        }
                    }
                }
            }

            if (!deliveryRequestDetails.isEmpty()) {
                ArrayList<RequestDetail<DeliveryProgressType>> requestDetails = new ArrayList<>();
                requestDetails.addAll(deliveryRequestDetails);
                for (RequestDetail<DeliveryProgressType> requestDetail : requestDetails) {
                    Boolean begin = requestDetail.attr("begin");

                    if (System.currentTimeMillis() - requestDetail.getTime() >= requestDetail.getTimeout()) {
                        requestDetail.fireTimeout();
                        failure.incrementAndGet();
                        deliveryRequestDetails.remove(requestDetail);
                    }else if(System.currentTimeMillis() - requestDetail.getRetryDate() >= 2 * 1000){
                        if((!Boolean.TRUE.equals(begin)) && requestDetail.getRetryTimes() - 1 < MAX_RETRYTIMES) {
                            try {
                                int type = requestDetail.attr("type");
                                int number = requestDetail.attr("number");
                                serailPortOpt.sendData(MessageCodec.encode(new DeliveryRequestMessage(type, number)));
                            }catch (Exception e){}
                            finally {
                                requestDetail.updateRetryDate();
                                requestDetail.incrRetryTimes();
                            }
                        }else if(requestDetail.getRetryTimes() - 1 >= MAX_RETRYTIMES){
                            failure.incrementAndGet();
                            requestDetail.fireTimeout();
                            queryInventoryRequestDetails.remove(requestDetail);
                        }
                    }else if((!Boolean.TRUE.equals(begin)) && Boolean.TRUE.equals(begin) && System.currentTimeMillis() - requestDetail.getTime() >= 6 * 1000){
                        requestDetail.fireTimeout();
                        failure.incrementAndGet();
                        deliveryRequestDetails.remove(requestDetail);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendLeftReportsDelivery() {
        try {
            for (String key : reportsDeliveryMap.keySet()) {
                reportsDelivery(key, reportsDeliveryMap.get(key));
            }
        } catch (Exception e) {
        }
    }

    private void reportsDelivery(final String orderNum, final boolean success) {
        final RequestParams requestParams = new RequestParams();
        requestParams.add("orderNum", orderNum);
        requestParams.add("imei", Helper.getImei());
        requestParams.add("success", Boolean.valueOf(success).toString());

        Activity activity = AppManager.getAppManager().currentActivity();
        if(activity != null){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    client.post(Constant.reportsDelivery, requestParams, new TextHttpResponseHandler("utf-8") {

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            reportsDeliveryMap.put(orderNum, success);
                            try {
                                Utils.setSharedPreferences(AppContext.instance(), "reportsDeliveryMap", JSON.toJSONString(reportsDeliveryMap));
                            }catch (Exception e){}
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String responseString) {
                            try {
                                RestResponse<Boolean> restResponse = JSON.parseObject(responseString, new TypeReference<RestResponse<Boolean>>() {
                                });
                                if ("0".equals(restResponse.getCode())) {
                                    reportsDeliveryMap.remove(orderNum);
                                } else {
                                    reportsDeliveryMap.put(orderNum, success);
                                }

                                Utils.setSharedPreferences(AppContext.instance(),"reportsDeliveryMap",JSON.toJSONString(reportsDeliveryMap));
                            } catch (Exception e) {
                                Log.d(TAG, "reportsDelivery error", e);
                            }
                        }
                    });
                }
            });
        }

    }


    private void handle(QueryInventoryResponseMessage queryInventoryResponseMessage) {
        failure.set(0);
        stockStatusType = queryInventoryResponseMessage.getStockStatusType();
        if (!queryInventoryRequestDetails.isEmpty()) {
            ArrayList<RequestDetail<StockStatusType>> requestDetails = new ArrayList<>();
            requestDetails.addAll(queryInventoryRequestDetails);

            for (RequestDetail<StockStatusType> requestDetail : requestDetails) {
                requestDetail.fireSuccess(queryInventoryResponseMessage.getStockStatusType());
                queryInventoryRequestDetails.remove(requestDetail);
            }
        }
    }

    private void handle(DeliveryResponseMessage deliveryResponseMessage) {
        failure.set(0);
        if (!deliveryRequestDetails.isEmpty()) {
            DeliveryProgressType deliveryProgressType = deliveryResponseMessage.getDeliveryProgressType();
            if (DeliveryProgressType.Delivery_Failure.equals(deliveryProgressType)) {
                ArrayList<RequestDetail<DeliveryProgressType>> requestDetails = new ArrayList<>();
                requestDetails.addAll(deliveryRequestDetails);
                for (RequestDetail<DeliveryProgressType> requestDetail : requestDetails) {
                    requestDetail.fireError(null);
                    reportsDelivery((String) requestDetail.attr("orderNum"), false);
                    deliveryRequestDetails.remove(requestDetail);
                }
            } else if (DeliveryProgressType.Delivery_Complete.equals(deliveryProgressType)) {
                serailPortOpt.sendData(MessageCodec.encode(new DeliveryConfirmMessage()));
                ArrayList<RequestDetail<DeliveryProgressType>> requestDetails = new ArrayList<>();
                requestDetails.addAll(deliveryRequestDetails);
                for (RequestDetail<DeliveryProgressType> requestDetail : requestDetails) {
                    requestDetail.fireSuccess(deliveryProgressType);
                    reportsDelivery((String) requestDetail.attr("orderNum"), true);
                    requestDetail.addAttr("begin",true);
                    deliveryRequestDetails.remove(requestDetail);
                }
            } else if (DeliveryProgressType.Delivery_Ing.equals(deliveryProgressType)) {
                ArrayList<RequestDetail<DeliveryProgressType>> requestDetails = new ArrayList<>();
                requestDetails.addAll(deliveryRequestDetails);
                for (RequestDetail<DeliveryProgressType> requestDetail : requestDetails) {
                    requestDetail.fireSuccess(deliveryProgressType);
                    requestDetail.setTime(System.currentTimeMillis());
                    requestDetail.addAttr("begin",true);
                }
            }
        }
    }


    GoodsSerial() {
        readThread.setDaemon(true);
        readThread.start();
        handler.postDelayed(runnable, 10 * 1000);
        open();
    }

    public void init(Context context){
        try {
            String reportsDeliveryMapString = Utils.getSharedPreferences(context, "reportsDeliveryMap");
            if (reportsDeliveryMapString == null || reportsDeliveryMapString.isEmpty()) {
                return;
            }

            JSONObject jsonObject = JSON.parseObject(reportsDeliveryMapString);
            for (String key : jsonObject.keySet()) {
                reportsDeliveryMap.put(key, Boolean.valueOf(String.valueOf(jsonObject.get(key))));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static GoodsSerial instance() {
        GoodsSerial instance = GoodsSerial.instance;
        if (instance == null) {
            synchronized (GoodsSerial.class) {
                instance = GoodsSerial.instance;
                if (instance == null) {
                    instance = new GoodsSerial();
                    GoodsSerial.instance = instance;
                }
            }
        }
        return instance;
    }


    public void delivery(final String orderNum, final RequestHandler<DeliveryProgressType> requestHandler, int timeOut) {

        RequestParams requestParams = new RequestParams();
        requestParams.add("orderNum", orderNum);
        requestParams.add("imei", Helper.getImei());

        final RequestDetail<DeliveryProgressType> requestDetail = new RequestDetail<>(requestHandler, timeOut);
        requestDetail.addAttr("orderNum", orderNum);
        client.post(Constant.queryOrderUrl, requestParams, new TextHttpResponseHandler("utf-8") {

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i(TAG, "请求到的数据json为 " + responseString);
                requestDetail.fireError(throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    Log.i(TAG, "请求到的数据json为 " + responseString);
                    RestResponse<OrderInfo> restResponse = JSON.parseObject(responseString, new TypeReference<RestResponse<OrderInfo>>() {
                    });
                    Log.i(TAG, "请求到的数据为 " + restResponse);
                    if ("0".equals(restResponse.getCode()) && OrderStatus.PAY_SUCCESS.equals(restResponse.getData().getOrderStatus())) {
                        check();
                        if (!available()) {
                            open();
                        }
                        final int type = restResponse.getData().getGoodsId().intValue();
                        final int number = restResponse.getData().getGoodsNum();
                        requestDetail.addAttr("type", type);
                        requestDetail.addAttr("number", number);
                        serailPortOpt.sendData(MessageCodec.encode(new DeliveryRequestMessage(type, number)));
                        deliveryRequestDetails.add(requestDetail);
                    } else {
                        Log.d(TAG, "ErroOrderStatus");
                        requestDetail.fireError(null);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "delivery error", e);
                    requestDetail.fireError(e);
                }
            }
        });
    }

    public void queryInventory(RequestHandler<StockStatusType> requestHandler, int timeOut) {
        queryInventoryRequestDetails.add(new RequestDetail<StockStatusType>(requestHandler, timeOut));
        serailPortOpt.sendData(MessageCodec.encode(new QueryInventoryRequestMessage()));

        check();
        if (!available()) {
            open();
        }
    }


    private boolean available() {
        return serailPortOpt != null && serailPortOpt.isOpen();
    }

    private void open() {
        synchronized (this) {
            if (serailPortOpt != null) {
                serailPortOpt.closeSerial();
            }
            serailPortOpt = new MachinesSerialPort(PORT, BOUND_RATE, 8, 1, 'n');
        }
    }

    private void check() {
        if (!available()) {
            open();
        }
    }
}
