package com.vending.machines.model;

public class RestResponse<T> {

    private String code;
    private String msg;
    private T data;

    public RestResponse() {
    }

    public RestResponse(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public RestResponse(String code, String msg, T t) {
        this.code = code;
        this.msg = msg;
        this.data = t;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RestResponse{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
