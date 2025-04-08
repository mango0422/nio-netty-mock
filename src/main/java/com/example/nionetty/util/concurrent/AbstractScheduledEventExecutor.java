package com.example.nionetty.util.concurrent;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * {@code AbstractScheduledEventExecutor} 클래스는 {@link ScheduledExecutorService} 인터페이스의
 * 기본 구현체로, 예약 작업 실행에 필요한 메서드들을 플레이스홀더 형태로 제공합니다.
 * 
 * (플레이스홀더)
 * 
 * @author 
 * @version 1.0
 */
public abstract class AbstractScheduledEventExecutor implements ScheduledExecutorService {

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

    // 예약 작업 관련 메서드들도 필요 시 구현합니다.
}
