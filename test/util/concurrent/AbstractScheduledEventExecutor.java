package com.example.nionetty.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;

public abstract class AbstractScheduledEventExecutor extends AbstractExecutorServiceImpl implements ScheduledExecutorService {
    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        throw new UnsupportedOperationException("Scheduling not implemented");
    }
    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        throw new UnsupportedOperationException("Scheduling not implemented");
    }
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        throw new UnsupportedOperationException("Scheduling not implemented");
    }
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        throw new UnsupportedOperationException("Scheduling not implemented");
    }
}
