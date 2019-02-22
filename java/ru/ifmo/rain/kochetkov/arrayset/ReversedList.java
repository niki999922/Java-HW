package ru.ifmo.rain.kochetkov.arrayset;

import java.util.AbstractList;
import java.util.List;

public class ReversedList<T> extends AbstractList<T> {
    private List<T> data;

    public int size() {
        return data.size();
    }

    ReversedList(List<T> data) {
        this.data = data;
    }

    @Override
    public T get(int index) {
            return data.get(size() - 1 - index);
    }
}