package com.example.nionetty.channel;

/**
 * {@code VoidChannelPromise} 클래스는 아무런 동작도 하지 않는 특수 {@link ChannelPromise}
 * 구현체입니다.
 * 주로 결과에 대한 처리가 필요 없는 경우 사용합니다.
 */
public final class VoidChannelPromise extends DefaultChannelPromise {

    private static final VoidChannelPromise INSTANCE = new VoidChannelPromise();

    private VoidChannelPromise() {
    }

    /**
     * 유일한 {@code VoidChannelPromise} 인스턴스를 반환합니다.
     * 
     * @return {@code VoidChannelPromise} 인스턴스
     */
    public static VoidChannelPromise getInstance() {
        return INSTANCE;
    }

    @Override
    public VoidChannelPromise setSuccess() {
        // 아무 동작도 하지 않음
        return this;
    }

    @Override
    public VoidChannelPromise setFailure(Throwable cause) {
        // 아무 동작도 하지 않음
        return this;
    }

    @Override
    public boolean trySuccess() {
        return true;
    }

    @Override
    public boolean tryFailure(Throwable cause) {
        return true;
    }
}
