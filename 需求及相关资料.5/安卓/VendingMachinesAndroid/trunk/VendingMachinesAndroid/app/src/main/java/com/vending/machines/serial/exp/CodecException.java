package com.vending.machines.serial.exp;

public class CodecException extends RuntimeException {
    public CodecException() {
    }

    public CodecException(String detailMessage) {
        super(detailMessage);
    }

    public CodecException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CodecException(Throwable throwable) {
        super(throwable);
    }
}
