package io.starac.task;

import io.starac.config.StarConfig;
import io.starac.violation.manager.ViolationManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class VlDecayTask extends StarTask {

    private final ViolationManager violationManager;
    private final StarConfig config;

    public VlDecayTask(JavaPlugin plugin, ViolationManager violationManager, StarConfig config) {
        super(plugin);
        this.violationManager = violationManager;
        this.config           = config;
    }

    @Override
    public void run() {
        violationManager.decayAll();
    }

    public void start() {
        int decay = config.getViolations().getVlDecayTicks();
        startSync(decay, decay);
        plugin.getLogger().info("[VlDecayTask] Запущен (decay каждые " + decay + " тиков)");
    }

    @Override
    public String getName() { return "VlDecayTask"; }
}