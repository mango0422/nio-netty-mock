package com.example.nionetty.util;

public interface GenericFutureListener<F extends Future<?>> {
    void operationComplete(F future) throws Exception;
}
