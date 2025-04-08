package com.example.nionetty.util.concurrent;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractExecutorServiceImpl extends AbstractExecutorService {
    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        return null;
    }
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return true;
    }
}
