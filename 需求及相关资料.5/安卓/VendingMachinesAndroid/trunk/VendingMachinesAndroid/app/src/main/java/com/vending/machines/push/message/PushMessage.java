package com.vending.machines.push.message;


public class PushMessage<T>{
    private PushMessageType messageType;
    private T data;

    public PushMessage(PushMessageType messageType, T data) {
        this.messageType = messageType;
        this.data = data;
    }

    public PushMessage() {
    }

    public PushMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(PushMessageType messageType) {
        this.messageType = messageType;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "PushMessage{" +
                "messageType=" + messageType +
                ", data=" + data +
                '}';
    }
}
