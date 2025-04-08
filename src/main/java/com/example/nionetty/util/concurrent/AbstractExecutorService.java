package com.example.nionetty.util.concurrent;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * {@code AbstractExecutorService} 클래스는 {@link ExecutorService} 인터페이스의
 * 기본 메서드들을 플레이스홀더 형태로 구현합니다.
 * 실제 구현 시에는 스레드 풀 관리 및 작업 큐 처리 로직을 추가해야 합니다.
 * 
 * (플레이스홀더)
 * 
 * @author 
 * @version 1.0
 */
public abstract class AbstractExecutorService implements ExecutorService {

    @Override
    public void shutdown() {}

    @Override
    public List<Runnable> shutdownNow() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        return false;
    }

    // submit(), invokeAll() 등 다른 메서드들도 필요 시 구현합니다.
}
