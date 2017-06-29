package com.vending.machines.serial.message;


import com.vending.machines.serial.exp.CodecException;

import java.util.HashMap;
import java.util.Map;

public class MessageCodec {
    private static final Map<Integer,Class<? extends BaseMessage>> responseMap = new HashMap<>();
    private static final Map<Integer,BaseMessage> confirmMap = new HashMap<>();

    static {
        responseMap.put(0x01, QueryInventoryResponseMessage.class);
        responseMap.put(0x02, DeliveryResponseMessage.class);

        confirmMap.put(0x01, new QueryInventoryConfirmMessage());
        confirmMap.put(0x02, new DeliveryConfirmMessage());
    }
    public static byte[] encode(BaseMessage baseMessage){
        try {
            return baseMessage.encode();
        }catch (Exception e){
            throw new CodecException(e);
        }
    }

    public static BaseMessage decode(byte[] data){
        try {
            int command = data[BaseMessage.START.length] & 0xff;
            BaseMessage baseMessage = responseMap.get(command).newInstance();
            baseMessage.decode(data);
            return baseMessage;
        }catch (Exception e){
            throw new CodecException(e);
        }
    }

    public static byte[] confirm(BaseMessage message){
        try {
            return encode(confirmMap.get(Integer.valueOf(message.getCommand())));
        }catch (CodecException e){
            throw e;
        }catch (Exception e){
            throw new CodecException(e);
        }
    }
}
