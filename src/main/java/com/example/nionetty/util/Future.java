package com.example.nionetty.util;

/**
 * {@code Future} 인터페이스는 비동기 작업의 결과를 나타냅니다.
 * 완료 여부, 성공 상태, 실패 원인 등을 확인할 수 있습니다.
 * 
 * @author 
 * @version 1.0
 */
public interface Future {
    /**
     * 비동기 작업이 완료되었는지 여부를 반환합니다.
     *
     * @return 완료 시 {@code true}
     */
    boolean isDone();

    /**
     * 작업이 성공적으로 완료되었는지 여부를 반환합니다.
     *
     * @return 성공 시 {@code true}
     */
    boolean isSuccess();

    /**
     * 작업 실패의 원인 예외를 반환합니다.
     *
     * @return 실패 원인 예외, 작업이 성공했다면 {@code null}
     */
    Throwable getCause();

    /**
     * 작업 완료까지 동기적으로 대기합니다.
     *
     * @return 현재 {@code Future} 인스턴스
     * @throws InterruptedException 대기 중 인터럽트 발생 시
     */
    Future sync() throws InterruptedException;
}
