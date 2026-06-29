package io.starac.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class CircularBuffer<T> {

    private final Object[] data;
    private int head = 0;
    private int size = 0;

    public CircularBuffer(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        this.data = new Object[capacity];
    }

    public void add(T value) {
        data[head] = value;
        head = (head + 1) % data.length;
        if (size < data.length) size++;
    }

    @SuppressWarnings("unchecked")
    public T get(int i) {
        if (i < 0 || i >= size) throw new IndexOutOfBoundsException("index=" + i + " size=" + size);
        return (T) data[(head - size + i + data.length) % data.length];
    }

    @SuppressWarnings("unchecked")
    public T newest() {
        if (size == 0) return null;
        return (T) data[(head - 1 + data.length) % data.length];
    }

    @SuppressWarnings("unchecked")
    public T oldest() {
        if (size == 0) return null;
        return (T) data[(head - size + data.length) % data.length];
    }

    public int  size()     { return size; }
    public int  capacity() { return data.length; }
    public boolean isEmpty()  { return size == 0; }
    public boolean isFull()   { return size == data.length; }

    public void forEach(Consumer<T> action) {
        for (int i = 0; i < size; i++) action.accept(get(i));
    }

    public List<T> toList() {
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) list.add(get(i));
        return list;
    }

    public void clear() {
        head = 0;
        size = 0;
        for (int i = 0; i < data.length; i++) data[i] = null;
    }
}