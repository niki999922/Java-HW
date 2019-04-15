package ru.ifmo.rain.kochetkov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Kochetkov Nikita M3234
 * Date: 07.03.2019
 */
@SuppressWarnings("Duplicates")
public class IterativeParallelism implements ListIP {
    private ArrayList<Thread> threadsList;
    private int numberThreads, eachThreadElements;
    private boolean restThreadElements;

    private <T> void checkArgs(int threads, List<? extends T> values, Object... objects) throws InterruptedException {
        if (threads <= 0) throw new InterruptedException("Not enough threads, expected more 0");
        try {
            Objects.requireNonNull(values);
        } catch (NullPointerException e) {
            throw new InterruptedException("Interrupted, values list is null");
        }
        try {
            for (Object object : objects) {
                Objects.requireNonNull(object);
            }
        } catch (NullPointerException e) {
            throw new InterruptedException("Interrupted, check other arguments");
        }
    }

    private <T> void preparation(int threads, List<? extends T> list) {
        numberThreads = Math.max(1, Math.min(threads, list.size()));
        eachThreadElements = list.size() / numberThreads;
        restThreadElements = list.size() % numberThreads > 0;
        threadsList = new ArrayList<>();
    }

    private <T, U> void initTreads(List<? extends T> values, ArrayList<U> resultTreads, Function<Stream<? extends T>, U> threadFunction) {
        for (int i = 0; i < numberThreads; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> {
                resultTreads.set(finalI, threadFunction.apply(values.subList(finalI * eachThreadElements, (finalI + 1) * eachThreadElements).stream()));
            });
            thread.start();
            threadsList.add(thread);
        }
        if (restThreadElements) {
            Thread thread = new Thread(() -> {
                resultTreads.set(numberThreads, threadFunction.apply(values.subList(numberThreads * eachThreadElements, values.size()).stream()));
            });
            thread.start();
            threadsList.add(thread);
        }
    }

    private void joinTreads() throws InterruptedException {
        for (Thread thread : threadsList) {
            thread.join();
        }
    }

    private <U> U processingResult(List<U> resultTreads, Function<Stream<? extends U>, U> resFunction) {
        return resFunction.apply(resultTreads.stream());
    }

    private <T, U> U task(int threads, List<? extends T> values, Function<Stream<? extends T>, U> threadFunction, Function<Stream<? extends U>, U> resFunction) throws InterruptedException {
        checkArgs(threads, values, threadFunction, resFunction);
        preparation(threads, values);
        ArrayList<U> resultTreads = new ArrayList<>(Collections.nCopies(numberThreads + ((restThreadElements) ? 1 : 0), null));
        initTreads(values, resultTreads, threadFunction);
        joinTreads();
        return processingResult(resultTreads, resFunction);
    }

    /**
     * Join values to string.
     *
     * @param threads number or concurrent threads.
     * @param values  values to join.
     * @return list of joined result of {@link #toString()} call on each value.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return task(threads, values, stream -> stream.map(Object::toString).collect(Collectors.joining()), stream -> stream.collect(Collectors.joining()));
    }

    /**
     * Filters values by predicate.
     *
     * @param threads   number or concurrent threads.
     * @param values    values to filter.
     * @param predicate filter predicate.
     * @return list of values satisfying given predicated. Order of values is preserved.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return task(threads, values, stream -> stream.filter(predicate).collect(Collectors.toList()), stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    /**
     * Mas values.
     *
     * @param threads number or concurrent threads.
     * @param values  values to filter.
     * @param f       mapper function.
     * @return list of values mapped by given function.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return task(threads, values, stream -> stream.map(f).collect(Collectors.toList()), stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    /**
     * Returns maximum value.
     *
     * @param threads    number or concurrent threads.
     * @param values     values to get maximum of.
     * @param comparator value comparator.
     * @return maximum of given values
     * @throws InterruptedException   if executing thread was interrupted.
     * @throws NoSuchElementException if not values are given.
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return task(threads, values, (stream) -> stream.max(comparator).orElseThrow(), (stream) -> stream.max(comparator).orElseThrow());
    }

    /**
     * Returns minimum value.
     *
     * @param threads    number or concurrent threads.
     * @param values     values to get minimum of.
     * @param comparator value comparator.
     * @return minimum of given values
     * @throws InterruptedException   if executing thread was interrupted.
     * @throws NoSuchElementException if not values are given.
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, Collections.reverseOrder(comparator));
    }

    /**
     * Returns whether all values satisfies predicate.
     *
     * @param threads   number or concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return whether all values satisfies predicate or {@code true}, if no values are given.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return task(threads, values, (stream) -> stream.allMatch(predicate), (stream) -> stream.allMatch(element -> element));
    }

    /**
     * Returns whether any of values satisfies predicate.
     *
     * @param threads   number or concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return whether any value satisfies predicate or {@code false}, if no values are given.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }
}
