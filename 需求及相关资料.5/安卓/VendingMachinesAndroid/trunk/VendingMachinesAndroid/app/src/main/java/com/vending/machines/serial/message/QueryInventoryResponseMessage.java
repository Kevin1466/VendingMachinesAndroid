package com.vending.machines.serial.message;

public class QueryInventoryResponseMessage extends BaseMessage {
    private static final byte[] RESERVED = {0x00};
    private StockStatusType stockStatusType;

    @Override
    public byte getCommand() {
        return 0x01;
    }

    @Override
    protected byte[] encode() {
        throw new RuntimeException("NOT IMPLEMENT");
    }

    @Override
    protected void decode(byte[] data) {
        int type = data[BaseMessage.START.length + 1] & 0xff;
        this.stockStatusType = StockStatusType.of(type);
    }

    public StockStatusType getStockStatusType() {
        return stockStatusType;
    }

    public void setStockStatusType(StockStatusType stockStatusType) {
        this.stockStatusType = stockStatusType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QueryInventoryResponseMessage{");
        sb.append("stockStatusType=").append(stockStatusType);
        sb.append('}');
        return sb.toString();
    }
}
