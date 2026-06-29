package io.starac.ai;

import io.starac.StarAC;
import io.starac.ai.AiClient;
import io.starac.data.DataManager;
import io.starac.data.PlayerData;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class AiCheck {

    private final StarAC plugin;
    private final DataManager dataManager;
    private final AiClient client;

    private final double banThreshold;
    private final double flagThreshold;
    private final int analyzeIntervalTicks; 

    public AiCheck(StarAC plugin, DataManager dataManager, AiClient client) {
        this.plugin              = plugin;
        this.dataManager         = dataManager;
        this.client              = client;
        this.banThreshold        = plugin.getConfig().getDouble("Ai.ban-threshold", 0.85);
        this.flagThreshold       = plugin.getConfig().getDouble("Ai.flag-threshold", 0.55);
        this.analyzeIntervalTicks = plugin.getConfig().getInt("Ai.analyze-interval-ticks", 40);
    }

    public void start() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (player.hasPermission("starac.bypass")) continue;

                PlayerData data = dataManager.get(player);
                if (!data.hasEnoughData()) continue;

                scheduleAnalysis(player, data);
            }
        }, analyzeIntervalTicks, analyzeIntervalTicks);

        plugin.getLogger().info("[AiCheck] Запущен (интервал: " + analyzeIntervalTicks + " тиков, бан: " + banThreshold + ")");
    }

    private void scheduleAnalysis(Player player, PlayerData data) {
        String uuid = player.getUniqueId().toString();
        String name = player.getName();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                AiClient.AnalysisResult result = client.analyze(uuid, name, data);
                if (result == null) return;

                logResult(name, result);
                handleResult(player, result);

            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "[AiCheck] Ошибка анализа " + name + ": " + e.getMessage());
            }
        });
    }

    private void handleResult(Player player, AiClient.AnalysisResult result) {
        if (result.probability >= banThreshold) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;

                String reason = buildBanReason(result);
                player.kickPlayer(reason);

                plugin.getLogger().warning("[AiCheck] BANNED " + player.getName()
                        + " | score=" + formatScore(result.probability)
                        + " | flags=" + result.flags);

                plugin.getServer().broadcast(
                        "§c[StarAC] §f" + player.getName() + " §cбыл заблокирован Ai античитом §7(score: " + formatScore(result.probability) + ")",
                        "starac.alerts"
                );
            });

        } else if (result.probability >= flagThreshold) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getServer().broadcast(
                        "§e[StarAC] §f" + player.getName() + " §eподозрителен §7(score: " + formatScore(result.probability) + ", flags: " + result.flags + ")",
                        "starac.alerts"
                );
            });
        }
    }

    private void logResult(String name, AiClient.AnalysisResult result) {
        plugin.getLogger().info(String.format(
                "[AiCheck] %s → %s | score=%.2f (rf=%.2f, nn=%.2f) | flags=%s",
                name, result.verdict, result.probability,
                result.rfProbability, result.nnProbability,
                result.flags
        ));
    }

    private String buildBanReason(AiClient.AnalysisResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("§c[StarAC] Cheat detected\n");
        sb.append("§7Score: §f").append(formatScore(result.probability)).append("\n");
        if (!result.flags.isEmpty()) {
            sb.append("§7Flags: §f").append(String.join(", ", result.flags));
        }
        return sb.toString();
    }

    private String formatScore(double probability) {
        return String.format("%.0f%%", probability * 100);
    }
}