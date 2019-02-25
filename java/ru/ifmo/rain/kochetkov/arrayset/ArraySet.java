package ru.ifmo.rain.kochetkov.arrayset;

import java.util.*;

/**
 * @author Kochetkov Nikita M3234
 * Date: 21.02.2019
 */

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private final List<T> data;
    private final Comparator<? super T> comparator;


    public ArraySet() {
        data = Collections.emptyList();
        comparator = null;
    }

    public ArraySet(Comparator<? super T> comparator) {
        data = Collections.emptyList();
        this.comparator = comparator;
    }

    public ArraySet(Collection<? extends T> collection, Comparator<? super T> comparator) {
        Set<T> tmp = new TreeSet<>(comparator);
        tmp.addAll(collection);
        this.data = new ArrayList<>(tmp);
        this.comparator = comparator;
    }

    public ArraySet(Collection<? extends T> collection) {
        this(collection, null);
    }


    private ArraySet(List<T> data, Comparator<? super T> comparator) {
        this.data = data;
        this.comparator = comparator;
    }

    public ArraySet(List<T> data) {
        this.data = data;
        comparator = null;
    }

    private T get(final int index) {
        try {
            Objects.checkIndex(index, size());
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
        if (index > size()) return null;
        return data.get(index);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(data, (T) Objects.requireNonNull(o), comparator) >= 0;
    }

    //max < e
    @Override
    public T lower(T t) {
        return get(getLowerIndex(t));
    }

    private int getLowerIndex(T t) {
        int res = Collections.binarySearch(data, Objects.requireNonNull(t), comparator);
        if (res < 0) {
            res = -res - 1;
        }
        res--;
        return res;
    }

    //max <= e
    @Override
    public T floor(T t) {
        return get(getFloorIndex(t));
    }

    private int getFloorIndex(T t) {
        int res = Collections.binarySearch(data, Objects.requireNonNull(t), comparator);
        if (res < 0) {
            res = -res - 1;
            res--;
        }
        return res;
    }

    //min >= e
    @Override
    public T ceiling(T t) {
        return get(getCeilingIndex(t));
    }

    private int getCeilingIndex(T t) {
        int res = Collections.binarySearch(data, Objects.requireNonNull(t), comparator);
        if (res < 0) {
            res = -res - 1;
        }
        return res;
    }

    //min > e
    @Override
    public T higher(T t) {
        return get(getHigherIndex(t));
    }

    private int getHigherIndex(T t) {
        int res = Collections.binarySearch(data, Objects.requireNonNull(t), comparator);
        if (res < 0) {
            res = -res - 1;
        } else {
            res++;
        }
        return res;
    }

    //min && del
    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    //max && del
    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(new ReversedList<>(data), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    private ArraySet<T> getSubSet(int newFromIndex, int newToIndex) {
        if (newFromIndex == 0 && newToIndex == size()) {
            return this;
        } else if (newFromIndex < newToIndex) {
            return new ArraySet<T>(data.subList(newFromIndex, newToIndex), comparator);
        } else {
            return new ArraySet<T>(comparator);
        }
    }

    //fromEl <= elements < toEl
    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        return tailSet(fromElement, fromInclusive).headSet(toElement, toInclusive);
    }

    //elements < t
    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        return getSubSet(0, headIndex(toElement, inclusive));
    }
    // t <= elements
    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        return getSubSet(tailIndex(fromElement, inclusive), size());
    }

    private int headIndex(T t, boolean inclusive) {
        int pos = Collections.binarySearch(data, Objects.requireNonNull(t), comparator());
        if (pos >= 0) {
            return inclusive ? pos + 1 : pos;
        } else {
            return -pos - 1;
        }
    }

    private int tailIndex(T t, boolean inclusive) {
        int pos = Collections.binarySearch(data, Objects.requireNonNull(t), comparator());
        if (pos >= 0) {
            return inclusive ? pos : pos + 1;
        } else {
            return -pos - 1;
        }
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement,true);
    }

    @Override
    public T first() {
        if (data.isEmpty()) {
            throw new NoSuchElementException();
        }
        return data.get(0);
    }

    @Override
    public T last() {
        if (data.isEmpty()) {
            throw new NoSuchElementException();
        }
        return data.get(data.size() - 1);
    }

    @Override
    public int size() {
        return data.size();
    }
}
