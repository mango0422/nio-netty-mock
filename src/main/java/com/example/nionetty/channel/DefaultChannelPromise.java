package com.example.nionetty.channel;

/**
 * {@code DefaultChannelPromise} 클래스는 {@code ChannelPromise}의 기본 구현체입니다.
 */
public class DefaultChannelPromise extends ChannelPromise {

    @Override
    public DefaultChannelPromise setSuccess() {
        synchronized (lock) {
            if (!isDone()) {
                this.success = true;
                lock.notifyAll();
            }
        }
        return this;
    }

    @Override
    public DefaultChannelPromise setFailure(Throwable cause) {
        synchronized (lock) {
            if (!isDone()) {
                this.cause = cause;
                lock.notifyAll();
            }
        }
        return this;
    }

    @Override
    public boolean trySuccess() {
        if (!isDone()) {
            setSuccess();
            return true;
        }
        return false;
    }

    @Override
    public boolean tryFailure(Throwable cause) {
        if (!isDone()) {
            setFailure(cause);
            return true;
        }
        return false;
    }
}
