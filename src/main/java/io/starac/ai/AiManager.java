package io.starac.ai;

import io.starac.StarAC;
import io.starac.ai.model.*;
import io.starac.ai.model.AiModel;
import io.starac.ai.model.HeuristicFallBack;
import io.starac.ai.model.ONNXModel;

import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class AiManager {

    private static AiManager instance;

    private final StarAC plugin;
    private final Logger logger;
    private final AiModel primaryModel;
    private final AiModel fallbackModel;
    private final ConcurrentLinkedQueue<InferenceRequest> requestQueue;
    private final AtomicBoolean processing;

    private long totalInferences = 0;
    private long totalInferenceTimeMs = 0;

    private AiManager(StarAC plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.requestQueue = new ConcurrentLinkedQueue<>();
        this.processing = new AtomicBoolean(false);

        AiModel loaded = loadPrimaryModel();
        if (loaded != null) {
            this.primaryModel = loaded;
            logger.info("[AIManager] Основная модель загружена: " + primaryModel.getName());
        } else {
            this.primaryModel = null;
            logger.warning("[AIManager] Не удалось загрузить основную модель, используется fallback.");
        }

        this.fallbackModel = new HeuristicFallBack();
    }

    public static synchronized AiManager getInstance(StarAC plugin) {
        if (instance == null) {
            instance = new AiManager(plugin);
        }
        return instance;
    }

    private AiModel loadPrimaryModel() {
        String modelPath = plugin.getAiConfig().getModelPath();
        if (modelPath == null || modelPath.isBlank()) {
            return null;
        }

        File modelFile = new File(plugin.getDataFolder(), modelPath);
        if (!modelFile.exists()) {
            logger.warning("[AIManager] Файл модели не найден: " + modelFile.getAbsolutePath());
            return null;
        }

        try {
            return new ONNXModel(modelFile.getAbsolutePath(), plugin.getAiConfig().getModelName());
        } catch (Exception e) {
            logger.warning("[AIManager] Ошибка загрузки ONNX модели: " + e.getMessage());
            return null;
        }
    }

    public AiVerdict predict(AiFeatureSet features) {
        AiModel model = primaryModel != null ? primaryModel : fallbackModel;
        long start = System.currentTimeMillis();

        try {
            AiVerdict result = model.predict(features);
            long elapsed = System.currentTimeMillis() - start;
            totalInferences++;
            totalInferenceTimeMs += elapsed;
            return result;

        } catch (Exception e) {
            logger.warning("[AIManager] Ошибка инференса: " + e.getMessage());
            return fallbackModel.predict(features);
        }
    }

    public void predictAsync(AiFeatureSet features, InferenceCallback callback) {
        requestQueue.add(new InferenceRequest(features, callback));
        processQueue();
    }

    private void processQueue() {
        if (processing.compareAndSet(false, true)) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                InferenceRequest req;
                while ((req = requestQueue.poll()) != null) {
                    AiVerdict result = predict(req.features);
                    req.callback.onComplete(result);
                }
                processing.set(false);
            });
        }
    }

    public long getTotalInferences() {
        return totalInferences;
    }

    public double getAverageInferenceTimeMs() {
        return totalInferences > 0 ? (double) totalInferenceTimeMs / totalInferences : 0.0;
    }

    public int getQueueSize() {
        return requestQueue.size();
    }

    public boolean isPrimaryModelLoaded() {
        return primaryModel != null;
    }

    public String getPrimaryModelName() {
        return primaryModel != null ? primaryModel.getName() : "none";
    }

    public interface InferenceCallback {
        void onComplete(AiVerdict verdict);
    }

    private record InferenceRequest(AiFeatureSet features, InferenceCallback callback) {}
}