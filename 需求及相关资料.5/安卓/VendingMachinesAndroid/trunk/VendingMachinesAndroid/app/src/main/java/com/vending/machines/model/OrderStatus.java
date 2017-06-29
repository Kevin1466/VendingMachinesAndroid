package com.vending.machines.model;

public enum OrderStatus {
    NEW, PAY_SUCCESS, PAY_FAILURE, TAKEGOODS_SUCCESS, TAKEGOODS_FAILURE, TIME_OUT_CLOSE,//超时关闭;
    Received //送货上门 已签收
}
