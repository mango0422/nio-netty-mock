package com.example.nionetty.channel;

/**
 * {@code ChannelId} 인터페이스는 채널의 고유 식별자를 정의합니다.
 * <p>
 * 각 채널은 생성 시 고유한 식별자를 할당받으며, 이 식별자는 로깅, 디버깅, 채널 상태 추적 등에 활용됩니다.
 * </p>
 *
 * @author 
 * @version 1.0
 */
public interface ChannelId {

    /**
     * 채널의 짧은 식별자 문자열을 반환합니다.
     *
     * @return 채널의 짧은 식별자 문자열
     */
    String asShortText();

    /**
     * 채널의 전체 식별자 문자열을 반환합니다.
     *
     * @return 채널의 전체 식별자 문자열
     */
    String asLongText();
}
