package com.example.nionetty.channel;

/**
 * {@code ChannelConfig} 인터페이스는 채널의 동작을 구성하기 위한 옵션들을 정의합니다.
 * <p>
 * 소켓 옵션, 타임아웃, 버퍼 크기 등 네트워크 I/O 성능에 영향을 미치는 다양한 설정값들을 다룹니다.
 * 구체적인 구현은 {@link DefaultChannelConfig} 등에서 이루어집니다.
 * </p>
 *
 * @param <T> 옵션 값의 타입
 * 
 * @author 
 * @version 1.0
 */
public interface ChannelConfig {

    /**
     * 지정한 옵션의 값을 설정합니다.
     *
     * @param option 옵션 객체 {@link ChannelOption}
     * @param value  설정할 값
     * @param <T>    옵션 값의 타입
     */
    <T> void setOption(ChannelOption<T> option, T value);

    /**
     * 지정한 옵션의 현재 값을 반환합니다.
     *
     * @param option 옵션 객체 {@link ChannelOption}
     * @param <T>    옵션 값의 타입
     * @return 해당 옵션의 현재 값
     */
    <T> T getOption(ChannelOption<T> option);
}
