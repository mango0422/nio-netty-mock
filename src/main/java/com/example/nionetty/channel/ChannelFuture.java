package com.example.nionetty.channel;

import com.example.nionetty.util.Future;

/**
 * {@code ChannelFuture} 클래스는 채널의 비동기 작업 결과를 나타내는 객체입니다.
 */
public class ChannelFuture implements Future {

    // 하위 클래스에서 접근할 수 있도록 protected로 변경
    protected volatile boolean success;
    protected volatile Throwable cause;
    protected final Object lock = new Object();

    // 채널과 연결된 ChannelFuture로서 채널 정보를 보관
    private Channel channel;

    /**
     * 생성자. 초기 상태는 미완료입니다.
     */
    public ChannelFuture() {
        this.success = false;
        this.cause = null;
    }

    /**
     * 채널 정보를 설정합니다.
     *
     * @param channel 연관된 채널
     * @return 현재 {@code ChannelFuture} 인스턴스
     */
    public ChannelFuture setChannel(Channel channel) {
        this.channel = channel;
        return this;
    }

    /**
     * 채널 정보를 반환합니다.
     *
     * @return 연관된 {@link Channel} 객체, 설정되어 있지 않으면 null
     */
    public Channel channel() {
        return channel;
    }

    /**
     * 작업이 성공적으로 완료되었음을 설정하고 대기 중인 스레드를 깨웁니다.
     *
     * @return 현재 {@code ChannelFuture} 인스턴스
     */
    public ChannelFuture setSuccess() {
        synchronized (lock) {
            this.success = true;
            lock.notifyAll();
        }
        return this;
    }

    /**
     * 작업 실패 시, 원인 예외를 설정하고 대기 중인 스레드를 깨웁니다.
     *
     * @param cause 작업 실패 원인 예외
     * @return 현재 {@code ChannelFuture} 인스턴스
     */
    public ChannelFuture setFailure(Throwable cause) {
        synchronized (lock) {
            this.cause = cause;
            lock.notifyAll();
        }
        return this;
    }

    @Override
    public ChannelFuture sync() throws InterruptedException {
        synchronized (lock) {
            while (!isDone()) {
                lock.wait();
            }
        }
        return this;
    }

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
}
