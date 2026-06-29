package io.starac.task;

import io.starac.StarAC;
import io.starac.data.DataManager;
import io.starac.violation.manager.ViolationManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Logger;

public final class DecayTask extends BukkitRunnable {

    private final StarAC plugin;
    private final DataManager dataManager;
    private final ViolationManager violationManager;
    private final Logger logger;

    public DecayTask(StarAC plugin, DataManager dataManager, ViolationManager violationManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.violationManager = violationManager;
        this.logger = plugin.getLogger();
    }

    public void start(int intervalTicks) {
        runTaskTimer(plugin, intervalTicks, intervalTicks);
        logger.info("[DecayTask] Запущен (интервал: " + intervalTicks + " тиков).");
    }

    @Override
    public void run() {
        violationManager.performDecay();
    }
}