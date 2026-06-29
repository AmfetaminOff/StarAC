package io.starac.config;

import org.bukkit.plugin.java.JavaPlugin;

public final class StarConfig {

    private final JavaPlugin plugin;

    private AiConfig        ai;
    private AlertConfig     alerts;
    private ViolationConfig violations;

    private boolean debug;
    private int     analyzeIntervalTicks;

    public StarConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        var cfg = plugin.getConfig();

        ai         = AiConfig.load(cfg);
        alerts     = AlertConfig.load(cfg);
        violations = ViolationConfig.load(cfg);

        debug                = cfg.getBoolean("debug", false);
        analyzeIntervalTicks = cfg.getInt("analyze-interval-ticks", 40);

        if (debug) {
            plugin.getLogger().info("[StarConfig] Конфиг загружен:");
            plugin.getLogger().info("  AI url: "       + ai.getApiUrl());
            plugin.getLogger().info("  Ban threshold: " + ai.getBanThreshold());
            plugin.getLogger().info("  Flag threshold: " + ai.getFlagThreshold());
            plugin.getLogger().info("  Analyze interval: " + analyzeIntervalTicks + " ticks");
        }
    }

    public AiConfig        getAi()         { return ai; }
    public AlertConfig     getAlerts()     { return alerts; }
    public ViolationConfig getViolations() { return violations; }
    public boolean         isDebug()       { return debug; }
    public int             getAnalyzeIntervalTicks() { return analyzeIntervalTicks; }
}