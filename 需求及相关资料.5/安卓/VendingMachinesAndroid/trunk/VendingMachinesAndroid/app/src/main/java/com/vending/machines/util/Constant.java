package com.vending.machines.util;

/**
 * Created by lishichao on 16/7/9.
 */
public interface Constant {
    public static final String baseurl="http://test2.haokaishi365.com";

    public static final String  My_HD_Version="1.1";

    public static final String LOGTAG = "rice_app";

    public static final String IMEI_KEY = "imei";

    /**
     * handler args1
     */
    /*包自动更新线程*/
    public static final int HandlerArgs1_AutoUpdateHandler = 100;

    public static final int HandleArgs1_JPush_OrderStatus = 101;

    /* 二维码扫码枪线程 状态变化 */
    public static final int HandlerArgs1_QrcodeDeviceStatusChange = 102;

    /* 发货串口异步通知线程 异步通知发货是否成功 */
    public static final int HandlerArgs1_TakeGoodsStatusChange = 103;

    /* 检查当前设备中的大米是否够量 */
    public static final int HandlerArgs1_CheckGoodsStatusChange = 104;

    /**
     * server url
     */

    public static final String checkVersionUrl = baseurl + "/api/apk/latest?HardVerion=" + My_HD_Version;
    public static final String queryOrderUrl = baseurl + "/api/order/queryOrder";
    public static final String reportsDelivery = baseurl + "/api/order/reports/delivery";
    public static final String REPORT_STATUS = baseurl + "/api/device/report/status";


}
