package com.vending.machines.serial.message;

import com.vending.machines.util.ByteUtil;

public class DeliveryConfirmMessage extends BaseMessage{
    private static final byte[] RESERVED = {0x00,0x00};

    @Override
    public byte getCommand() {
        return 0x00;
    }

    @Override
    protected byte[] encode() {
        byte[] data = ByteUtil.byteArraysMerge(BaseMessage.START, new byte[]{getCommand()},RESERVED);
        return ByteUtil.byteArraysMerge(data,new byte[]{(byte) 0xFF}, BaseMessage.END);
    }

    @Override
    protected void decode(byte[] data) {
        throw new RuntimeException("NOT IMPLEMENT");
    }
}
