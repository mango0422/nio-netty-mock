package com.example.nionetty.util.concurrent;

import com.example.nionetty.util.Future;
import com.example.nionetty.util.GenericFutureListener;
import com.example.nionetty.util.Promise;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DefaultPromise<V> implements Promise<V>, Future<V> {
    private volatile boolean done = false;
    private volatile boolean cancelled = false;
    private V result;
    private Throwable cause;
    private final List<GenericFutureListener<? extends Future<? super V>>> listeners = new ArrayList<>();
    private final Object lock = new Object();

    @Override
    public boolean isSuccess() {
        return done && cause == null;
    }
    
    @Override
    public Throwable cause() {
        return cause;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public boolean isDone() {
        return done;
    }
    
    @Override
    public V getNow() {
        return result;
    }
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        synchronized(lock) {
            if (done) return false;
            cancelled = true;
            done = true;
            lock.notifyAll();
        }
        notifyListeners();
        return true;
    }
    
    @Override
    public V get() throws InterruptedException, ExecutionException {
        await();
        if (cancelled) throw new CancellationException();
        if (cause != null) throw new ExecutionException(cause);
        return result;
    }
    
    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!await(timeout, unit)) {
            throw new TimeoutException();
        }
        if (cancelled) throw new CancellationException();
        if (cause != null) throw new ExecutionException(cause);
        return result;
    }
    
    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        long timeoutNanos = unit.toNanos(timeout);
        long deadline = System.nanoTime() + timeoutNanos;
        synchronized(lock) {
            while (!done && timeoutNanos > 0) {
                TimeUnit.NANOSECONDS.timedWait(lock, timeoutNanos);
                timeoutNanos = deadline - System.nanoTime();
            }
            return done;
        }
    }
    
    @Override
    public boolean await(long timeoutMillis) throws InterruptedException {
        return await(timeoutMillis, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
        long timeoutNanos = unit.toNanos(timeout);
        long deadline = System.nanoTime() + timeoutNanos;
        boolean interrupted = false;
        synchronized(lock) {
            while (!done && timeoutNanos > 0) {
                try {
                    TimeUnit.NANOSECONDS.timedWait(lock, timeoutNanos);
                } catch (InterruptedException e) {
                    interrupted = true;
                }
                timeoutNanos = deadline - System.nanoTime();
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        return done;
    }
    
    @Override
    public boolean awaitUninterruptibly(long timeoutMillis) {
        return awaitUninterruptibly(timeoutMillis, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public Future<V> await() throws InterruptedException {
        synchronized(lock) {
            while (!done) {
                lock.wait();
            }
        }
        return this;
    }
    
    @Override
    public Future<V> awaitUninterruptibly() {
        boolean interrupted = false;
        synchronized(lock) {
            while (!done) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
        return this;
    }
    
    @Override
    public Future<V> sync() throws InterruptedException, ExecutionException {
        await();
        if (!isSuccess()) {
            throw new ExecutionException(cause);
        }
        return this;
    }
    
    @Override
    public Future<V> syncUninterruptibly() throws ExecutionException {
        awaitUninterruptibly();
        if (!isSuccess()) {
            throw new ExecutionException(cause);
        }
        return this;
    }
    
    @Override
    public Promise<V> setSuccess(V result) {
        synchronized(lock) {
            if (done) throw new IllegalStateException("Promise already completed");
            this.result = result;
            done = true;
            lock.notifyAll();
        }
        notifyListeners();
        return this;
    }
    
    @Override
    public boolean trySuccess(V result) {
        synchronized(lock) {
            if (done) return false;
            this.result = result;
            done = true;
            lock.notifyAll();
        }
        notifyListeners();
        return true;
    }
    
    @Override
    public Promise<V> setFailure(Throwable cause) {
        synchronized(lock) {
            if (done) throw new IllegalStateException("Promise already completed");
            this.cause = cause;
            done = true;
            lock.notifyAll();
        }
        notifyListeners();
        return this;
    }
    
    @Override
    public boolean tryFailure(Throwable cause) {
        synchronized(lock) {
            if (done) return false;
            this.cause = cause;
            done = true;
            lock.notifyAll();
        }
        notifyListeners();
        return true;
    }
    
    @Override
    public boolean setUncancellable() {
        // 간단 구현: 별도의 취소 방지 로직 없이 작업 상태에 따라 true 반환
        synchronized(lock) {
            if (done) return false;
            return true;
        }
    }
    
    @Override
    public Promise<V> addListener(GenericFutureListener<? extends Future<? super V>> listener) {
        if (listener == null) return this;
        synchronized(lock) {
            if (!done) {
                listeners.add(listener);
            } else {
                notifyListener(listener);
            }
        }
        return this;
    }
    
    @Override
    public Promise<V> removeListener(GenericFutureListener<? extends Future<? super V>> listener) {
        synchronized(lock) {
            listeners.remove(listener);
        }
        return this;
    }
    
    private void notifyListeners() {
        List<GenericFutureListener<? extends Future<? super V>>> copy;
        synchronized(lock) {
            copy = new ArrayList<>(listeners);
            listeners.clear();
        }
        for (GenericFutureListener<? extends Future<? super V>> listener : copy) {
            notifyListener(listener);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void notifyListener(GenericFutureListener<? extends Future<? super V>> listener) {
        try {
            ((GenericFutureListener<Future<V>>)listener).operationComplete(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
