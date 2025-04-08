package com.example.nionetty.buffer;

/**
 * {@code CustomBuffer} 클래스는 효율적인 네트워크 I/O 처리를 위해
 * 사용자 정의 버퍼를 구현한 클래스입니다.
 * <p>
 * 내부적으로 바이트 배열을 사용하여 데이터를 저장하며, 필요 시 자동으로
 * 버퍼 용량을 확장합니다. 또한 읽기/쓰기 인덱스를 관리하여 데이터의 흐름을 제어합니다.
 * </p>
 *
 * 주요 기능:
 * <ul>
 * <li>데이터 쓰기 및 읽기</li>
 * <li>쓰기 가능 공간 확보를 위한 자동 버퍼 확장</li>
 * <li>읽기/쓰기 인덱스 리셋 기능</li>
 * </ul>
 *
 * @author
 * @version 1.0
 */
public class CustomBuffer {

    /** 내부 바이트 배열 버퍼 */
    private byte[] buffer;
    /** 읽기 인덱스 */
    private int readIndex;
    /** 쓰기 인덱스 */
    private int writeIndex;

    /**
     * 기본 생성자.
     * 기본 버퍼 크기를 1024 바이트로 초기화합니다.
     */
    public CustomBuffer() {
        this(1024);
    }

    /**
     * 지정된 용량으로 {@code CustomBuffer}를 초기화합니다.
     *
     * @param capacity 초기 버퍼 용량 (바이트 단위)
     * @throws IllegalArgumentException 용량이 0 이하일 경우
     */
    public CustomBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Buffer capacity must be positive");
        }
        this.buffer = new byte[capacity];
        this.readIndex = 0;
        this.writeIndex = 0;
    }

    /**
     * 주어진 데이터를 버퍼에 기록합니다.
     *
     * @param data 기록할 바이트 배열 데이터
     * @return 실제로 기록된 바이트 수
     */
    public int write(byte[] data) {
        int length = data.length;
        ensureWritable(length);
        System.arraycopy(data, 0, buffer, writeIndex, length);
        writeIndex += length;
        return length;
    }

    /**
     * 버퍼에서 지정된 길이의 데이터를 읽어옵니다.
     *
     * @param length 읽어올 바이트 수
     * @return 읽어온 데이터가 담긴 바이트 배열
     */
    public byte[] read(int length) {
        int available = writeIndex - readIndex;
        if (length > available) {
            length = available;
        }
        byte[] output = new byte[length];
        System.arraycopy(buffer, readIndex, output, 0, length);
        readIndex += length;
        return output;
    }

    /**
     * 버퍼에 추가 데이터를 기록할 공간이 부족할 경우, 내부 버퍼 용량을 확장합니다.
     *
     * @param minWritableBytes 필요한 추가 공간 (바이트 단위)
     */
    private void ensureWritable(int minWritableBytes) {
        if (writeIndex + minWritableBytes > buffer.length) {
            // 현재 용량 부족: 기존 용량의 2배 또는 최소 필요한 크기 중 큰 값으로 확장
            int newCapacity = Math.max(buffer.length * 2, writeIndex + minWritableBytes);
            byte[] newBuffer = new byte[newCapacity];
            System.arraycopy(buffer, 0, newBuffer, 0, writeIndex);
            buffer = newBuffer;
        }
    }

    /**
     * 읽기/쓰기 인덱스를 리셋하여 버퍼를 재사용할 수 있도록 합니다.
     * <p>
     * 이 메서드를 호출하면 이전 데이터는 논리적으로 삭제되며, 이후 쓰기 작업은
     * 버퍼의 시작부분부터 이루어집니다.
     * </p>
     */
    public void reset() {
        readIndex = 0;
        writeIndex = 0;
    }

    /**
     * 현재 읽기 가능한 바이트 수를 반환합니다.
     *
     * @return 읽기 가능한 바이트 수
     */
    public int readableBytes() {
        return writeIndex - readIndex;
    }

    /**
     * 버퍼의 전체 용량(현재 할당된 크기)을 반환합니다.
     *
     * @return 버퍼의 전체 용량 (바이트 단위)
     */
    public int capacity() {
        return buffer.length;
    }
}
