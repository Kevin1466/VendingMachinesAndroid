package com.vending.machines.serial;

import java.util.HashMap;
import java.util.Map;

public class RequestDetail<T> {
    private long time = System.currentTimeMillis();
    private RequestHandler requestHandler;
    private int timeout;
    private int retryTimes = 0;
    private Map<String,Object> context = new HashMap<>();
    private long retryDate = System.currentTimeMillis();

    public RequestDetail() {
    }

    public <T> void addAttr(String key, T value){
        context.put(key,value);
    }

    public <V> V attr(String key){
        Object o = context.get(key);

        if(o == null){
            return null;
        }
        return (V) o;
    }

    public RequestDetail(RequestHandler requestHandler, int timeout) {
        this.requestHandler = requestHandler;
        this.timeout = timeout;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void incrRetryTimes(){
        retryTimes += 1;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public RequestHandler getRequestHandler() {
        return requestHandler;
    }

    public void setRequestHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    public void fireSuccess(T t){
        if(requestHandler != null){
            requestHandler.onSuccess(t);
        }
    }

    public void fireTimeout(){
        if(requestHandler != null){
            requestHandler.onTimeout();
        }
    }

    public void fireError(Throwable throwable){
        if(requestHandler != null){
            requestHandler.onError(throwable);
        }
    }

    public long getRetryDate() {
        return retryDate;
    }

    public void updateRetryDate(){
        retryDate = System.currentTimeMillis();
    }
}
