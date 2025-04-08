package com.example.nionetty.util;

/**
 * {@code Promise} 인터페이스는 {@link Future}를 확장하여,
 * 비동기 작업의 결과를 설정할 수 있는 메서드를 추가로 제공합니다.
 * 
 * @author 
 * @version 1.0
 */
public interface Promise extends Future {
    /**
     * 작업이 성공적으로 완료되었음을 설정합니다.
     *
     * @return 현재 {@code Promise} 인스턴스
     */
    Promise setSuccess();

    /**
     * 작업 실패 시, 원인 예외를 설정합니다.
     *
     * @param cause 작업 실패 원인 예외
     * @return 현재 {@code Promise} 인스턴스
     */
    Promise setFailure(Throwable cause);
}
