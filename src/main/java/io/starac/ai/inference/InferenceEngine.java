package io.starac.ai.inference;

import io.starac.ai.AiManager;
import io.starac.ai.AiFeatureSet;
import io.starac.ai.AiVerdict;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public final class InferenceEngine {

    public static final class InferenceRequest implements Comparable<InferenceRequest> {
        private final UUID playerUuid;
        private final AiFeatureSet features;
        private final int priority;
        private final BiConsumer<UUID, AiVerdict> callback;
        private final long createdAt;

        public InferenceRequest(UUID playerUuid, AiFeatureSet features, int priority, BiConsumer<UUID, AiVerdict> callback) {
            this.playerUuid = playerUuid;
            this.features = features;
            this.priority = priority;
            this.callback = callback;
            this.createdAt = System.currentTimeMillis();
        }

        @Override
        public int compareTo(InferenceRequest other) {
            return Integer.compare(other.priority, this.priority);
        }
    }

    private final AiManager aiManager;
    private final PriorityBlockingQueue<InferenceRequest> queue;
    private final int maxQueueSize;
    private final int batchSize;
    private final AtomicInteger droppedRequests = new AtomicInteger(0);

    public InferenceEngine(AiManager aiManager, int maxQueueSize, int batchSize) {
        this.aiManager = aiManager;
        this.maxQueueSize = maxQueueSize;
        this.batchSize = batchSize;
        this.queue = new PriorityBlockingQueue<>(Math.min(maxQueueSize, 100));
    }

    public void submit(UUID playerUuid, AiFeatureSet features, int vl, BiConsumer<UUID, AiVerdict> callback) {
        if (queue.size() >= maxQueueSize) {
            droppedRequests.incrementAndGet();
            return;
        }

        InferenceRequest request = new InferenceRequest(playerUuid, features, vl, callback);
        queue.offer(request);
    }

    public int processBatch() {
        if (queue.isEmpty()) return 0;

        List<InferenceRequest> batch = new ArrayList<>(batchSize);
        queue.drainTo(batch, batchSize);

        for (InferenceRequest request : batch) {
            try {
                AiVerdict verdict = aiManager.predict(request.features);
                if (request.callback != null) {
                    request.callback.accept(request.playerUuid, verdict);
                }
            } catch (Exception e) {
                if (request.callback != null) {
                    request.callback.accept(request.playerUuid, AiVerdict.ERROR);
                }
            }
        }

        return batch.size();
    }

    public int getQueueSize() {
        return queue.size();
    }

    public int getDroppedRequests() {
        return droppedRequests.get();
    }

    public void clearQueue() {
        queue.clear();
    }
}