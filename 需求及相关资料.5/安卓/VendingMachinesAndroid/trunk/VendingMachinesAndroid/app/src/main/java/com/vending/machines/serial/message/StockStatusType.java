package com.vending.machines.serial.message;

public enum StockStatusType {
    UNKNOWN("FF", "未知"),SUFFICIENT("00","充足"),INSUFFICIENT("01","不足"),PROMPT_REPLENISHMENT("02","提示补货"),FULL("03","满仓");

    private String code;
    private String title;

    private StockStatusType(String code, String title){
        this.code = code;
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public static StockStatusType of(String code){
        if(code.equals("00")){
            return SUFFICIENT;
        }else if(code.equals("01")){
            return INSUFFICIENT;
        }else if(code.equals("02")){
            return PROMPT_REPLENISHMENT;
        }else if(code.equals("03")){
            return FULL;
        }

        return UNKNOWN;
    }

    public static StockStatusType of(int code){
        if(code == 00){
            return SUFFICIENT;
        }else if(code == 01){
            return INSUFFICIENT;
        }else if(code == 02){
            return PROMPT_REPLENISHMENT;
        }else if(code == 03){
            return FULL;
        }

        return UNKNOWN;
    }

    @Override
    public String toString() {
        return "StockStatus{" +
                "code='" + code + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}

