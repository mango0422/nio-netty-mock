package com.example.nionetty.channel;

/**
 * {@code ChannelPromise} 추상 클래스는 채널 비동기 작업의 결과를 설정할 수 있는 메서드를 정의합니다.
 */
public abstract class ChannelPromise extends ChannelFuture {

    /**
     * 작업이 성공적으로 완료되었음을 설정합니다.
     *
     * @return 현재 {@code ChannelPromise} 인스턴스
     */
    public abstract ChannelPromise setSuccess();

    /**
     * 작업 실패 시, 원인 예외를 설정합니다.
     *
     * @param cause 작업 실패 원인 예외
     * @return 현재 {@code ChannelPromise} 인스턴스
     */
    public abstract ChannelPromise setFailure(Throwable cause);

    /**
     * 조건부로 작업 성공을 시도합니다.
     *
     * @return 성공하면 {@code true}, 이미 완료되었다면 {@code false}
     */
    public abstract boolean trySuccess();

    /**
     * 조건부로 작업 실패를 시도합니다.
     *
     * @param cause 작업 실패 원인 예외
     * @return 실패 설정에 성공하면 {@code true}, 이미 완료되었다면 {@code false}
     */
    public abstract boolean tryFailure(Throwable cause);
}
