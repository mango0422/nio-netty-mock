package com.example.nionetty.util;

/**
 * {@code GenericFutureListener} 인터페이스는 {@link Future} 완료 시 실행할 리스너를 정의합니다.
 *
 * @param <F> {@link Future} 타입
 * 
 * @author 
 * @version 1.0
 */
public interface GenericFutureListener<F extends Future> {
    /**
     * Future 작업 완료 후 호출됩니다.
     *
     * @param future 완료된 {@link Future} 객체
     * @throws Exception 작업 완료 처리 중 발생할 수 있는 예외
     */
    void operationComplete(F future) throws Exception;
}
