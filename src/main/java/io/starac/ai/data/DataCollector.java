package io.starac.ai.data;

import io.starac.ai.AiFeatureSet;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public final class DataCollector {

    public record TrainingSample(
            UUID playerUuid,
            long timestamp,
            AiFeatureSet features,
            String label
    ) {}

    private final ConcurrentLinkedQueue<TrainingSample> buffer;
    private final AtomicInteger size;
    private final int maxBufferSize;

    public DataCollector(int maxBufferSize) {
        this.buffer = new ConcurrentLinkedQueue<>();
        this.size = new AtomicInteger(0);
        this.maxBufferSize = maxBufferSize;
    }

    public void record(UUID playerUuid, AiFeatureSet features, String label) {
        if (features == null || features.getDataPoints() < 5) return;

        TrainingSample sample = new TrainingSample(
                playerUuid,
                System.currentTimeMillis(),
                features,
                label != null ? label : "CLEAN"
        );

        buffer.add(sample);
        int currentSize = size.incrementAndGet();

        while (currentSize > maxBufferSize) {
            buffer.poll();
            currentSize = size.decrementAndGet();
        }
    }

    public java.util.List<TrainingSample> drain() {
        java.util.List<TrainingSample> result = new java.util.ArrayList<>(size.get());
        TrainingSample sample;
        while ((sample = buffer.poll()) != null) {
            result.add(sample);
            size.decrementAndGet();
        }
        return result;
    }

    public int getBufferSize() {
        return size.get();
    }

    public boolean isReadyForExport(int threshold) {
        return size.get() >= threshold;
    }

    public void clear() {
        buffer.clear();
        size.set(0);
    }
}