package com.vending.machines.serial;

public interface RequestHandler<T> {
    public void onSuccess(T t);

    public void onTimeout();

    public void onError(Throwable throwable);
}
