package io.starac.analysis.rotation.window;

public final class RotationWindow {

    private final RotationSample[] samples;

    private int index;
    private int size;

    public RotationWindow(int capacity) {
        this.samples = new RotationSample[capacity];
    }

    public void add(RotationSample sample) {

        samples[index] = sample;

        index++;

        if (index >= samples.length) {
            index = 0;
        }

        if (size < samples.length) {
            size++;
        }

    }

    public RotationSample get(int i) {

        if (i < 0 || i >= size)
            throw new IndexOutOfBoundsException();

        int actual = index - size + i;

        if (actual < 0)
            actual += samples.length;

        return samples[actual];
    }

    public RotationSample newest() {

        if (size == 0)
            return null;

        int newest = index - 1;

        if (newest < 0)
            newest = samples.length - 1;

        return samples[newest];
    }

    public RotationSample oldest() {

        if (size == 0)
            return null;

        return get(0);
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return samples.length;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {

        index = 0;
        size = 0;

        for (int i = 0; i < samples.length; i++) {
            samples[i] = null;
        }

    }

}