package com.example.nionetty.channel;

import java.util.Objects;

/**
 * {@code ChannelOption} 클래스는 채널 구성 옵션의 키를 나타냅니다.
 * <p>
 * 각 옵션은 {@code ChannelConfig}를 통해 설정될 수 있으며, 네트워크 소켓 옵션, 타임아웃 설정, 버퍼 크기 등 다양한 값들이 포함됩니다.
 * </p>
 *
 * @param <T> 옵션 값의 타입
 * 
 * @author 
 * @version 1.0
 */
public final class ChannelOption<T> {

    /** 옵션의 이름 */
    private final String name;

    /**
     * 생성자.
     *
     * @param name 옵션의 이름
     */
    private ChannelOption(String name) {
        this.name = name;
    }

    /**
     * 주어진 이름으로 {@code ChannelOption} 인스턴스를 생성합니다.
     *
     * @param name 옵션의 이름
     * @param <T>  옵션 값의 타입
     * @return 새로 생성된 {@code ChannelOption} 인스턴스
     */
    public static <T> ChannelOption<T> valueOf(String name) {
        return new ChannelOption<>(name);
    }

    /**
     * 옵션의 이름을 반환합니다.
     *
     * @return 옵션 이름
     */
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "ChannelOption{" + "name='" + name + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChannelOption)) {
            return false;
        }
        ChannelOption<?> that = (ChannelOption<?>) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
