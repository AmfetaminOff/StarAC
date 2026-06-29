package io.starac.task;

import io.starac.config.StarConfig;
import io.starac.data.DataManager;
import io.starac.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.BiConsumer;

public final class AnalysisTask extends StarTask {

    private final DataManager dataManager;
    private final StarConfig config;

    private final BiConsumer<Player, PlayerData> onAnalyze;

    private int tickCount = 0;

    public AnalysisTask(JavaPlugin plugin, DataManager dataManager,
                        StarConfig config, BiConsumer<Player, PlayerData> onAnalyze) {
        super(plugin);
        this.dataManager = dataManager;
        this.config      = config;
        this.onAnalyze   = onAnalyze;
    }

    @Override
    public void run() {
        tickCount++;

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission("starac.bypass")) continue;

            PlayerData data = dataManager.get(player.getUniqueId());
            if (data == null || !data.hasEnoughData()) continue;

            onAnalyze.accept(player, data);
        }
    }

    public void start() {
        int interval = config.getAnalyzeIntervalTicks();
        startSync(interval, interval);
        plugin.getLogger().info("[AnalysisTask] Запущен (интервал: " + interval + " тиков)");
    }

    @Override
    public String getName() { return "AnalysisTask"; }
}