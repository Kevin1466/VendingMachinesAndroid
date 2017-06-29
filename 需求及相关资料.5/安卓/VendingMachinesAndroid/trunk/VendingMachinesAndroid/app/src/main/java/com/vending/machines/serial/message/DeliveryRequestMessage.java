package com.vending.machines.serial.message;

import com.vending.machines.util.ByteUtil;

public class DeliveryRequestMessage extends BaseMessage{
    private int type;
    private int number;

    @Override
    public byte getCommand() {
        return 0x02;
    }

    public DeliveryRequestMessage(int type, int number) {
        this.type = type;
        this.number = number;
    }

    public DeliveryRequestMessage() {
    }

    @Override
    protected byte[] encode() {
        byte[] data = ByteUtil.byteArraysMerge(BaseMessage.START,new byte[]{getCommand(),(byte)(type & 0xff),(byte)(number & 0xff)});
        return ByteUtil.byteArraysMerge(data,new byte[]{makeChecksum(data)},END);
    }

    @Override
    protected void decode(byte[] data) {
        throw new RuntimeException("NOT IMPLEMENT");
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeliveryRequestMessage{");
        sb.append("type=").append(type);
        sb.append(", number=").append(number);
        sb.append('}');
        return sb.toString();
    }
}
