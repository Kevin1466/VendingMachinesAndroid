package com.vending.machines.serial.message;


public enum DeliveryProgressType {
    Delivery_Unknown((byte)0xFF),Delivery_Failure((byte)0x00),Delivery_Ing((byte)0x01),Delivery_Complete((byte)0x02),;

    private byte type;

    DeliveryProgressType(byte type) {
        this.type = type;
    }

    public static DeliveryProgressType of(int type){
        switch (type){
            case 0 : return Delivery_Failure;
            case 1 : return Delivery_Ing;
            case 17 : return Delivery_Complete;
            case 2 : return Delivery_Complete;
        }

        return Delivery_Unknown;
    }

    public int type() {
        return type & 0xff;
    }
}
