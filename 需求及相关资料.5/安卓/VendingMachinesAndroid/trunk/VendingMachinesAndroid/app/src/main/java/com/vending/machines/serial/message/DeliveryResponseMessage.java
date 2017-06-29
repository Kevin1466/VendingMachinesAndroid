package com.vending.machines.serial.message;

public class DeliveryResponseMessage extends BaseMessage{
    private DeliveryProgressType deliveryProgressType;

    @Override
    public byte getCommand() {
        return 0x02;
    }

    @Override
    protected byte[] encode() {
        throw new RuntimeException("NOT IMPLEMENT");
    }

    @Override
    protected void decode(byte[] data) {
        int type = data[BaseMessage.START.length + 1] & 0xff;
        this.deliveryProgressType = DeliveryProgressType.of(type);
    }

    public DeliveryProgressType getDeliveryProgressType() {
        return deliveryProgressType;
    }

    public void setDeliveryProgressType(DeliveryProgressType deliveryProgressType) {
        this.deliveryProgressType = deliveryProgressType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeliveryResponseMessage{");
        sb.append("deliveryProgressType=").append(deliveryProgressType);
        sb.append('}');
        return sb.toString();
    }
}
