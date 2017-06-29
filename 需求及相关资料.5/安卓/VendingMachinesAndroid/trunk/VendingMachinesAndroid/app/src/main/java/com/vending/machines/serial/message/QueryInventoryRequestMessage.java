package com.vending.machines.serial.message;


import com.vending.machines.util.ByteUtil;

public class QueryInventoryRequestMessage extends BaseMessage{
    private static final byte[] RESERVED = {0x00};

    @Override
    public byte getCommand() {
        return 0x01;
    }

    @Override
    protected byte[] encode() {
        byte[] data = ByteUtil.byteArraysMerge(BaseMessage.START,new byte[]{getCommand()},RESERVED);
        return ByteUtil.byteArraysMerge(data,new byte[]{makeChecksum(data)},END);
    }

    @Override
    protected void decode(byte[] data) {
        throw new RuntimeException("NOT IMPLEMENT");
    }
}
