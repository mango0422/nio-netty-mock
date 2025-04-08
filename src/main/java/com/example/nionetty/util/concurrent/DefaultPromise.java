package com.example.nionetty.util.concurrent;

import com.example.nionetty.util.Future;
import com.example.nionetty.util.Promise;

/**
 * {@code DefaultPromise} 클래스는 {@link Promise} 인터페이스의 기본 구현체입니다.
 * 채널이나 이벤트 루프와 관련된 비동기 작업에서 사용될 수 있습니다.
 * 
 * @author 
 * @version 1.0
 */
public class DefaultPromise implements Promise {

    private volatile boolean success;
    private volatile Throwable cause;
    private final Object lock = new Object();

    @Override
    public boolean isDone() {
        return success || (cause != null);
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    @Override
    public Future sync() throws InterruptedException {
        synchronized (lock) {
            while (!isDone()) {
                lock.wait();
            }
        }
        return this;
    }

    @Override
    public Promise setSuccess() {
        synchronized (lock) {
            this.success = true;
            lock.notifyAll();
        }
        return this;
    }

    @Override
    public Promise setFailure(Throwable cause) {
        synchronized (lock) {
            this.cause = cause;
            lock.notifyAll();
        }
        return this;
    }
}
