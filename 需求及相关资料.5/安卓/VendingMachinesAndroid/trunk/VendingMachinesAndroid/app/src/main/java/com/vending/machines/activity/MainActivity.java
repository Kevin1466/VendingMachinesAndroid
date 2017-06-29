package com.vending.machines.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.dwin.dwinapi.Dwin;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.BridgeWebViewClient;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.vending.machines.AppManager;
import com.vending.machines.R;
import com.vending.machines.receiver.NetWorkChangeBroadcastReceiver;
import com.vending.machines.serial.GoodsSerial;
import com.vending.machines.serial.RequestHandler;
import com.vending.machines.serial.SweepCodeGunSerial;
import com.vending.machines.serial.message.DeliveryProgressType;
import com.vending.machines.serial.message.StockStatusType;
import com.vending.machines.util.Constant;
import com.vending.machines.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    boolean isWebviewLoadSuccess;
    ImageView imageView;
    BridgeWebView webView;

    BroadcastReceiver netReceiver;
    boolean isLoadNoWifiPage = false;

    private FrameLayout frameLayout = null;
    private WebChromeClient chromeClient = null;
    private WebChromeClient.CustomViewCallback myCallBack = null;
    private View myView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getAppManager().addActivity(this);

        setContentView(R.layout.activity_main);
        frameLayout = (FrameLayout) findViewById(R.id.framelayout);
        imageView = (ImageView) findViewById(R.id.imageView);
        webView = (BridgeWebView) findViewById(R.id.webView);

        chromeClient = new MyChromeClient();

        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        // H5 缓存
        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        String databasePath = getApplicationContext().getDir("databases", Context.MODE_PRIVATE).getPath();
        webSettings.setDatabasePath(databasePath);
        // 使用缓存：
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);


        // Utils.isNetWorkAvailable() does not work
        if (Utils.isNetworkConnected(this)) {
            webView.loadUrl("file:///android_asset/index.html");
        } else {
            isLoadNoWifiPage = true;
            webView.loadUrl("file:///android_asset/index.html#/nowifi");
        }

        webView.setWebViewClient(new BridgeWebViewClient(webView) {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "onPageFinished: " + url);
                super.onPageFinished(view, url);
                if (!isWebviewLoadSuccess) {
                    isWebviewLoadSuccess = true;
                    webView.setVisibility(View.VISIBLE);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    imageView.setVisibility(View.GONE);
                }

            }
        });


        webView.setWebChromeClient(chromeClient);


        /**
         * 用handler去处理异步线程的返回结果
         */
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.arg1) {
                    case Constant.HandlerArgs1_AutoUpdateHandler: //自动更新handle
                        //AutoUpdateHandler.checkServerAppVersion(MainActivity.this);
                        break;
                    case Constant.HandleArgs1_JPush_OrderStatus: //极光push的订单状态handler
                        String orderNum = msg.getData().getString("data");
                        Log.i(Constant.LOGTAG, "[HandleArgs1_JPush_OrderStatus]: " + orderNum);
                        webView.callHandler("invokeSdkRegisterOrderStatusEvent", orderNum, null);
                        break;
                    case Constant.HandlerArgs1_QrcodeDeviceStatusChange: //二维码扫码枪线程 状态变化
                        String data1 = msg.getData().getString("data");
                        Log.i(Constant.LOGTAG, "[HandlerArgs1_QrcodeDeviceStatusChange]: " + data1);
                        webView.callHandler("onQrcodeDeviceStateChangeEvent", data1, null);
                        break;
                    case Constant.HandlerArgs1_TakeGoodsStatusChange: //发货串口异步通知线程 异步通知发货是否成功
                        String data2 = msg.getData().getString("data");
                        Log.i(Constant.LOGTAG, "[HandlerArgs1_TakeGoodsStatusChange]: " + data2);
                        webView.callHandler("onTakeGoodsStatusChangeEvent", data2, null);
                        break;
                    case Constant.HandlerArgs1_CheckGoodsStatusChange: //检查当前设备中的大米是否够量
                        String data3 = msg.getData().getString("data");
                        Log.i(Constant.LOGTAG, "[HandlerArgs1_CheckGoodsStatusChange]: " + data3);
                        webView.callHandler("onCheckGoodsStatusChangeEvent", data3, null);
                        break;
                }
            }
        };

        /*JPushReceiver.setOnReceivePaySuccesse(new JPushReceiver.OnReceivePaySuccess() {
            @Override
            public void onReceive(String orderNum) {
                Log.i(Constant.LOGTAG, "[JPushReceiver:onReceive]: " + orderNum);
                sendMessageByHandler(handler, Constant.HandleArgs1_JPush_OrderStatus, orderNum);
            }
        });*/

        /**
         * 打开底部菜单栏
         */
        webView.registerHandler("invokeSdkShowNavigation", new BridgeHandler() {
            @Override
            public void handler(String s, CallBackFunction callBackFunction) {
                Log.i(Constant.LOGTAG, "[invokeSdkShowNavigation]: " + s);
                Dwin.getInstance().showNavigation();
                if (null != callBackFunction) {
                    callBackFunction.onCallBack("");
                }
            }
        });


        /**
         * 关闭底部菜单
         */
        webView.registerHandler("invokeSdkHideNavigation", new BridgeHandler() {
            @Override
            public void handler(String s, CallBackFunction callBackFunction) {
                Log.i(Constant.LOGTAG, "[invokeSdkHideNavigation]: " + s);
                Dwin.getInstance().hideNavigation();
                if (null != callBackFunction) {
                    callBackFunction.onCallBack("");
                }
            }
        });

        /**
         * native toast消息提示
         */
        webView.registerHandler("invokeSdkToast", new BridgeHandler() {
            @Override
            public void handler(String s, CallBackFunction callBackFunction) {
                Log.i(Constant.LOGTAG, "[invokeSdkToast]: " + s);
                Utils.shortToast(MainActivity.this, s);
            }
        });

        /**
         * 获取当前网络状态
         */
        webView.registerHandler("invokeSdkGetNetState", new BridgeHandler() {
            @Override
            public void handler(String s, CallBackFunction callBackFunction) {
                boolean state = Utils.isNetworkConnected(MainActivity.this) && Utils.isNetWorkAvailable();
                Log.i(Constant.LOGTAG, "[invokeSdkGetNetState]: " + state);
                if (null != callBackFunction) {
                    callBackFunction.onCallBack(String.valueOf(state));
                }
            }
        });

        /**
         * 设置网络
         */
        webView.registerHandler("invokeSdkSetupNetwork", new BridgeHandler() {
            @Override
            public void handler(String s, CallBackFunction callBackFunction) {
                Log.i(Constant.LOGTAG, "[invokeSdkSetupNetwork]");
                Utils.setupNetwork(MainActivity.this);
            }
        });

        /**
         * 获取当前包版本信息
         */
        webView.registerHandler("invokeSdkGetAppVersion", new BridgeHandler() {
            @Override
            public void handler(String s, CallBackFunction callBackFunction) {
                String versionName = Utils.getVersionNumber(MainActivity.this);
                Log.i(Constant.LOGTAG, "[invokeSdkGetAppVersion]: " + versionName);
                if (null != callBackFunction) {
                    callBackFunction.onCallBack(versionName);
                }
            }
        });

        /**
         * 获取初始化数据
         */
        webView.registerHandler("invokeSdkGetInitData", new BridgeHandler() {
            @Override
            public void handler(String s, CallBackFunction callBackFunction) {
                String appVersion = Utils.getVersionNumber(MainActivity.this);
                String imei = Utils.getSharedPreferences(MainActivity.this, "imei", "000000");
                String initData = "{}";
                try {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("appVersion", appVersion);
                    jsonObj.put("imei", imei);
                    initData = jsonObj.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i(Constant.LOGTAG, "[获取到的初始化信息为]: " + initData);
                if (null != callBackFunction) {
                    callBackFunction.onCallBack(initData);
                }
            }
        });

        webView.registerHandler("invokeSdkGetRegistrationID", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                String registrationID = JPushInterface.getRegistrationID(MainActivity.this);
                if (null != function) {
                    function.onCallBack(registrationID);
                }
            }
        });

        /**
         * 设置imei
         */
        webView.registerHandler("invokeSdkSetImei", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Utils.setSharedPreferences(MainActivity.this, "imei", data);
                if (null != function) {
                    function.onCallBack("true");
                }
            }
        });

        /**
         * 打开扫码枪
         * { status: 0, desc: '扫码成功', qrcode: ''}
         * { status: 1, desc: '扫码超时'}
         * { status: 2, desc: '扫码失败'}
         */
        webView.registerHandler("invokeSdkOpenQrcodeDevice", new BridgeHandler() {
            @Override
            public void handler(String data, final CallBackFunction function) {
                SweepCodeGunSerial.instance().beginSweepCodeGunSerial(new RequestHandler<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Log.i(Constant.LOGTAG, "[invokeSdkOpenQrcodeDevice: onSuccess]");
                        try {
                            final JSONObject json = new JSONObject();
                            json.put("status", 0);
                            json.put("desc", "扫码成功");
                            json.put("qrcode", s);

                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (null != function) {
                                        function.onCallBack(json.toString());
                                    }
                                }
                            });

                            webView.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (null != function) {
                                        function.onCallBack(json.toString());
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onTimeout() {
                        Log.i(Constant.LOGTAG, "[invokeSdkOpenQrcodeDevice: onTimeout]");
                        try {
                            final JSONObject json = new JSONObject();
                            json.put("status", 1);
                            json.put("desc", "扫码超时");

                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (null != function) {
                                        function.onCallBack(json.toString());
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.i(Constant.LOGTAG, "[invokeSdkOpenQrcodeDevice: onError]");
                        try {
                            final JSONObject json = new JSONObject();
                            json.put("status", 2);
                            json.put("desc", "扫码失败");

                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (null != function) {
                                        function.onCallBack(json.toString());
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, 30000);
            }
        });

        /**
         * 检查米是否充足
         * { status: 0, desc: '检查设备成功', stock: ''}
         * { status: 1, desc: '检查设备超时'}
         * { status: 2, desc: '检查设备失败'}
         */
        webView.registerHandler("invokeSdkCheckGoodsStatus", new BridgeHandler() {
            @Override
            public void handler(String data, final CallBackFunction function) {
                GoodsSerial.instance().queryInventory(new RequestHandler<StockStatusType>() {
                    @Override
                    public void onSuccess(StockStatusType stockStatusType) {
                        Log.i(Constant.LOGTAG, "[invokeSdkCheckGoodsStatus: onSuccess]");
                        if (null != function) {
                            try {
                                final JSONObject json = new JSONObject();
                                json.put("status", 0);
                                json.put("desc", "检查设备成功");
                                json.put("stock", stockStatusType.getCode());

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        function.onCallBack(json.toString());
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onTimeout() {
                        Log.i(Constant.LOGTAG, "[invokeSdkCheckGoodsStatus: onTimeout]");
                        if (null != function) {
                            try {
                                final JSONObject json = new JSONObject();
                                json.put("status", 1);
                                json.put("desc", "检查设备超时");

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        function.onCallBack(json.toString());
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.i(Constant.LOGTAG, "[invokeSdkCheckGoodsStatus: onError]");
                        if (null != function) {
                            try {
                                final JSONObject json = new JSONObject();
                                json.put("status", 2);
                                json.put("desc", "检查设备失败");

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        function.onCallBack(json.toString());
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, 120000);
            }
        });

        /**
         * 发货
         * { status: 0, desc: '发货成功', progress: ''}
         * { status: 1, desc: '发货超时'}
         * { status: 2, desc: '发货失败'}
         */
        webView.registerHandler("invokeSdkTakeGoods", new BridgeHandler() {
            @Override
            public void handler(String data, final CallBackFunction function) {
                try {
                    JSONObject object = new JSONObject(data);
                    String orderNum = object.getString("orderNum");
                    GoodsSerial.instance().delivery(orderNum, new RequestHandler<DeliveryProgressType>() {
                        @Override
                        public void onSuccess(DeliveryProgressType deliveryProgressType) {
                            Log.i(Constant.LOGTAG, "[invokeSdkTakeGoods: onSuccess]");
                            try {
                                final JSONObject json = new JSONObject();
                                json.put("status", 0);
                                json.put("desc", "发货成功");
                                json.put("progress", deliveryProgressType.toString());

                                sendMessageByHandler(handler, Constant.HandlerArgs1_TakeGoodsStatusChange, json.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onTimeout() {
                            Log.i(Constant.LOGTAG, "[invokeSdkTakeGoods: onTimeout]");
                            try {
                                final JSONObject json = new JSONObject();
                                json.put("status", 1);
                                json.put("desc", "发货超时");

                                sendMessageByHandler(handler, Constant.HandlerArgs1_TakeGoodsStatusChange, json.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Log.i(Constant.LOGTAG, "[invokeSdkTakeGoods: onError]");
                            try {
                                final JSONObject json = new JSONObject();
                                json.put("status", 2);
                                json.put("desc", "发货失败");

                                sendMessageByHandler(handler, Constant.HandlerArgs1_TakeGoodsStatusChange, json.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 30000);
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (null != function) {
                        function.onCallBack("false");
                    }
                }

            }
        });

        Log.e(TAG, "network is available? == " + Utils.isNetWorkAvailable());
        Log.e(TAG, "network is connected? == " + Utils.isNetworkConnected(this));
        //启动网络监听receiver
        initReceiver();

        //启动定时更新线程
        //new Thread(new AutoUpdateRunner(handler)).start();
    }

    private void initReceiver() {
        netReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Utils.isNetworkConnected(MainActivity.this)) {
                    Log.i("isNetworkConnected", "true");
                    isLoadNoWifiPage = false;
                    webView.loadUrl("javascript:window.location.hash='#/index'");
                }
            }
        };
        IntentFilter filter = new IntentFilter(NetWorkChangeBroadcastReceiver.NET_CHANGE);
        registerReceiver(netReceiver, filter);
    }

    /**
     * 通过handler去处理异步线程中的消息
     */
    public void sendMessageByHandler(Handler handler, int arg1, String value) {
        Message message = handler.obtainMessage();
        message.arg1 = arg1;
        Bundle data = new Bundle();
        data.putString("data", value);
        message.setData(data);
        handler.sendMessage(message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
//        Dwin.getInstance().showNavigation();
        Dwin.getInstance().hideNavigation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getAppManager().finishActivity(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
        Dwin.getInstance().showNavigation();
    }

    // added by rgk
    @Override
    public void onBackPressed() {
        if(myView == null){
            super.onBackPressed();
        } else {
            chromeClient.onHideCustomView();
        }
    }

    public class MyChromeClient extends WebChromeClient{

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if(myView != null){
                callback.onCustomViewHidden();
                return;
            }
            frameLayout.removeView(webView);
            frameLayout.addView(view);
            myView = view;
            myCallBack = callback;
        }

        @Override
        public void onHideCustomView() {
            if(myView == null){
                return;
            }
            frameLayout.removeView(myView);
            myView = null;
            frameLayout.addView(webView);
            myCallBack.onCustomViewHidden();
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.i(Constant.LOGTAG, "[控制台信息]:" + consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            Log.i(Constant.LOGTAG, "[alert message]: " + message);
            return super.onJsAlert(view, url, message, result);
        }
    }

}
