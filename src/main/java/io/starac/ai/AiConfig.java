package io.starac.ai;

import org.bukkit.configuration.file.FileConfiguration;

public final class AiConfig {

    private final boolean enabled;

    private final String modelPath;
    private final String modelName;

    private final double suspiciousThreshold;
    private final double cheatingThreshold;
    private final double confirmedThreshold;

    private final boolean dataCollectionEnabled;
    private final int maxBufferSize;
    private final int autoExportIntervalMins;

    private final int batchSize;
    private final int maxQueueSize;
    private final int tickInterval;

    private AiConfig(Builder b) {
        this.enabled = b.enabled;
        this.modelPath = b.modelPath;
        this.modelName = b.modelName;
        this.suspiciousThreshold = b.suspiciousThreshold;
        this.cheatingThreshold = b.cheatingThreshold;
        this.confirmedThreshold = b.confirmedThreshold;
        this.dataCollectionEnabled = b.dataCollectionEnabled;
        this.maxBufferSize = b.maxBufferSize;
        this.autoExportIntervalMins = b.autoExportIntervalMins;
        this.batchSize = b.batchSize;
        this.maxQueueSize = b.maxQueueSize;
        this.tickInterval = b.tickInterval;
    }

    public boolean isEnabled() { return enabled; }
    public String getModelPath() { return modelPath; }
    public String getModelName() { return modelName; }
    public double getSuspiciousThreshold() { return suspiciousThreshold; }
    public double getCheatingThreshold() { return cheatingThreshold; }
    public double getConfirmedThreshold() { return confirmedThreshold; }
    public boolean isDataCollectionEnabled() { return dataCollectionEnabled; }
    public int getMaxBufferSize() { return maxBufferSize; }
    public int getAutoExportIntervalMins() { return autoExportIntervalMins; }
    public int getBatchSize() { return batchSize; }
    public int getMaxQueueSize() { return maxQueueSize; }
    public int getTickInterval() { return tickInterval; }

    public static AiConfig load(FileConfiguration cfg) {
        return new Builder()
                .enabled(cfg.getBoolean("ai.enabled", true))
                .modelPath(cfg.getString("ai.model-path", "models/default.onnx"))
                .modelName(cfg.getString("ai.model-name", "default"))
                .suspiciousThreshold(cfg.getDouble("ai.thresholds.suspicious", 0.40))
                .cheatingThreshold(cfg.getDouble("ai.thresholds.cheating", 0.70))
                .confirmedThreshold(cfg.getDouble("ai.thresholds.confirmed", 0.90))
                .dataCollectionEnabled(cfg.getBoolean("ai.data-collection.enabled", false))
                .maxBufferSize(cfg.getInt("ai.data-collection.max-buffer-size", 10000))
                .autoExportIntervalMins(cfg.getInt("ai.data-collection.auto-export-interval-mins", 60))
                .batchSize(cfg.getInt("ai.inference.batch-size", 8))
                .maxQueueSize(cfg.getInt("ai.inference.max-queue-size", 500))
                .tickInterval(cfg.getInt("ai.inference.tick-interval", 20))
                .build();
    }

    public static final class Builder {
        private boolean enabled = true;
        private String modelPath = "models/default.onnx";
        private String modelName = "default";
        private double suspiciousThreshold = 0.40;
        private double cheatingThreshold = 0.70;
        private double confirmedThreshold = 0.90;
        private boolean dataCollectionEnabled = false;
        private int maxBufferSize = 10000;
        private int autoExportIntervalMins = 60;
        private int batchSize = 8;
        private int maxQueueSize = 500;
        private int tickInterval = 20;

        public Builder enabled(boolean v) { enabled = v; return this; }
        public Builder modelPath(String v) { modelPath = v; return this; }
        public Builder modelName(String v) { modelName = v; return this; }
        public Builder suspiciousThreshold(double v) { suspiciousThreshold = v; return this; }
        public Builder cheatingThreshold(double v) { cheatingThreshold = v; return this; }
        public Builder confirmedThreshold(double v) { confirmedThreshold = v; return this; }
        public Builder dataCollectionEnabled(boolean v) { dataCollectionEnabled = v; return this; }
        public Builder maxBufferSize(int v) { maxBufferSize = v; return this; }
        public Builder autoExportIntervalMins(int v) { autoExportIntervalMins = v; return this; }
        public Builder batchSize(int v) { batchSize = v; return this; }
        public Builder maxQueueSize(int v) { maxQueueSize = v; return this; }
        public Builder tickInterval(int v) { tickInterval = v; return this; }

        public AiConfig build() {
            return new AiConfig(this);
        }
    }
}