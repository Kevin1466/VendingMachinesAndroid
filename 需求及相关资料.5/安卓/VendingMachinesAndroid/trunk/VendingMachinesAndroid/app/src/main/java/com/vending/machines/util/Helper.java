package com.vending.machines.util;

import com.vending.machines.AppContext;


public class Helper {
    public static String getImei(){
        String imei = Utils.getSharedPreferences(AppContext.instance(), Constant.IMEI_KEY);
        if(imei == null || imei.equals("")){
            return "000000";
        }

        return imei;
    }
}
