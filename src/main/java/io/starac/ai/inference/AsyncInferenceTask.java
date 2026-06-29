package io.starac.ai.inference;

import io.starac.StarAC;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Logger;

public final class AsyncInferenceTask extends BukkitRunnable {

    private final StarAC plugin;
    private final InferenceEngine engine;
    private final Logger logger;

    private long totalProcessed = 0;

    public AsyncInferenceTask(StarAC plugin, InferenceEngine engine) {
        this.plugin = plugin;
        this.engine = engine;
        this.logger = plugin.getLogger();
    }

    public void start(int intervalTicks) {
        runTaskTimerAsynchronously(plugin, intervalTicks, intervalTicks);
        logger.info("[AsyncInferenceTask] Запущен (интервал: " + intervalTicks + " тиков).");
    }

    @Override
    public void run() {
        try {
            int processed = engine.processBatch();
            totalProcessed += processed;

            if (totalProcessed % 100 == 0 && totalProcessed > 0) {
                logger.fine("[AsyncInferenceTask] Обработано " + totalProcessed
                        + " запросов. В очереди: " + engine.getQueueSize()
                        + " | Дропнуто: " + engine.getDroppedRequests());
            }
        } catch (Exception e) {
            logger.warning("[AsyncInferenceTask] Критическая ошибка в тике: " + e.getMessage());
        }
    }

    public long getTotalProcessed() {
        return totalProcessed;
    }
}