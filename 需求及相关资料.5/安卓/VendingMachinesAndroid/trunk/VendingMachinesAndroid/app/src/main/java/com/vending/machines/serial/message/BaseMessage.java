package com.vending.machines.serial.message;

public abstract class BaseMessage {
    public static final byte[] START = {(byte)0xAA, 0x55};
    public static final byte[] END = {0x55, (byte)0xAA};

    public BaseMessage() {
    }

    public abstract byte getCommand();

    protected abstract byte[] encode();

    protected abstract void decode(byte[] data);

    public  static byte makeChecksum(byte[] data) {
        int sum = 0;
        for(byte b : data){
            sum = sum + Integer.valueOf(b);
        }
        return (byte)(sum % 256);
    }

}