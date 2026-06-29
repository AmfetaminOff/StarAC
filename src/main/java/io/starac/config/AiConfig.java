package io.starac.config;

import org.bukkit.configuration.file.FileConfiguration;

public final class AiConfig {

    private final String apiUrl;
    private final String apiKey;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;
    private final double banThreshold;
    private final double flagThreshold;
    private final boolean enabled;

    private AiConfig(Builder b) {
        this.apiUrl           = b.apiUrl;
        this.apiKey           = b.apiKey;
        this.connectTimeoutMs = b.connectTimeoutMs;
        this.readTimeoutMs    = b.readTimeoutMs;
        this.banThreshold     = b.banThreshold;
        this.flagThreshold    = b.flagThreshold;
        this.enabled          = b.enabled;
    }

    public static AiConfig load(FileConfiguration cfg) {
        return new Builder()
                .apiUrl(cfg.getString("ai.api-url", "http://localhost:8080"))
                .apiKey(cfg.getString("ai.api-key", ""))
                .connectTimeoutMs(cfg.getInt("ai.connect-timeout-ms", 2000))
                .readTimeoutMs(cfg.getInt("ai.read-timeout-ms", 3000))
                .banThreshold(cfg.getDouble("ai.ban-threshold", 0.85))
                .flagThreshold(cfg.getDouble("ai.flag-threshold", 0.55))
                .enabled(cfg.getBoolean("ai.enabled", true))
                .build();
    }

    public String getApiUrl()          { return apiUrl; }
    public String getApiKey()          { return apiKey; }
    public int    getConnectTimeout()  { return connectTimeoutMs; }
    public int    getReadTimeout()     { return readTimeoutMs; }
    public double getBanThreshold()    { return banThreshold; }
    public double getFlagThreshold()   { return flagThreshold; }
    public boolean isEnabled()         { return enabled; }

    public static final class Builder {
        private String apiUrl           = "http://localhost:8080";
        private String apiKey           = "";
        private int    connectTimeoutMs = 2000;
        private int    readTimeoutMs    = 3000;
        private double banThreshold     = 0.85;
        private double flagThreshold    = 0.55;
        private boolean enabled         = true;

        public Builder apiUrl(String v)           { apiUrl = v; return this; }
        public Builder apiKey(String v)           { apiKey = v; return this; }
        public Builder connectTimeoutMs(int v)    { connectTimeoutMs = v; return this; }
        public Builder readTimeoutMs(int v)       { readTimeoutMs = v; return this; }
        public Builder banThreshold(double v)     { banThreshold = v; return this; }
        public Builder flagThreshold(double v)    { flagThreshold = v; return this; }
        public Builder enabled(boolean v)         { enabled = v; return this; }
        public AiConfig build()                   { return new AiConfig(this); }
    }
}