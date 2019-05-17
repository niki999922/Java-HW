package ru.ifmo.rain.kochetkov.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * @author Kochetkov Nikita M3234
 * Date: 14.04.2019
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final Queue<Runnable> tasks;
    private final List<Thread> threads;
    private final Lock lock;
    private final Condition queueNotEmpty, queueNotFull;

    private static final int MAX = 250;

    public ParallelMapperImpl(int threadsNumber) {
        if (threadsNumber <= 0) throw new IllegalArgumentException("You have incorrect count of threads.");
        lock = new ReentrantLock();
        queueNotEmpty = lock.newCondition();
        queueNotFull = lock.newCondition();
        threads = new ArrayList<>(threadsNumber);
        tasks = new ArrayDeque<>();
        for (int i = 0; i < threadsNumber; i++) {
            Thread thread = new Thread(() -> {
                Runnable task;
                while (!Thread.interrupted()) {
                    lock.lock();
                    try {
                        while (tasks.isEmpty()) {
                            try {
                                queueNotEmpty.await();
                            } catch (InterruptedException ignored) {
                                return;
                            }
                        }
                        task = tasks.poll();
                        queueNotFull.signalAll();
                    } finally {
                        lock.unlock();
                    }
                    task.run();
                }
            });
            thread.start();
            threads.add(thread);
        }
    }

    private <T, R> void addTask(Function<? super T, ? extends R> f, T arg, int index, List<R> result, CountDownLatch countDownLatch) throws InterruptedException {
        lock.lock();
        try {
            while (tasks.size() == MAX) {
                queueNotFull.await();
            }
            tasks.add(() -> {
                result.set(index, f.apply(arg));
                countDownLatch.countDown();
            });
            queueNotEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Maps function {@code f} over specified {@code args}.
     * Mapping for each element performs in parallel.
     *                  
     * @param f
     * @param args
     * @throws InterruptedException if calling thread was interrupted
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        List<R> result = new ArrayList<>(Collections.nCopies(args.size(), null));
        var countDownLatch = new CountDownLatch(args.size());
        for (var i = 0; i < args.size(); i++) {
            addTask(f, args.get(i), i, result, countDownLatch);
        }
        countDownLatch.await();
        return result;
    }

    /**
     * Stops all threads. All unfinished mappings leave in undefined state.
     */
    @Override
    public void close() {
        threads.forEach(Thread::interrupt);
    }
}
