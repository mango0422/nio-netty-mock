package com.example.nionetty.channel;

/**
 * {@code ChannelException} 클래스는 채널 관련 작업 수행 중 발생하는
 * 예외 상황을 나타내는 런타임 예외입니다.
 * <p>
 * 이 예외는 채널 초기화, 바인딩, 데이터 전송 등 다양한 채널 작업 중 발생할 수 있으며,
 * 구체적인 원인 분석과 디버깅에 도움을 주도록 설계되었습니다.
 * </p>
 *
 * @author
 * @version 1.0
 */
public class ChannelException extends RuntimeException {

    /**
     * 예외 메시지와 함께 {@code ChannelException}을 생성합니다.
     *
     * @param message 예외의 상세 메시지
     */
    public ChannelException(String message) {
        super(message);
    }

    /**
     * 예외 메시지와 원인 예외(cause)를 함께 제공하여 {@code ChannelException}을 생성합니다.
     *
     * @param message 예외의 상세 메시지
     * @param cause   원인 예외
     */
    public ChannelException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 원인 예외(cause)만을 인자로 하여 {@code ChannelException}을 생성합니다.
     *
     * @param cause 원인 예외
     */
    public ChannelException(Throwable cause) {
        super(cause);
    }
}
