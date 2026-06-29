package io.starac.alert;

import io.starac.config.AlertConfig;
import io.starac.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class AlertManager {

    private final JavaPlugin plugin;
    private final AlertConfig config;
    private final Logger logger;

    private final Map<UUID, Long> lastAlertTime = new ConcurrentHashMap<>();

    public AlertManager(JavaPlugin plugin, AlertConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.logger = plugin.getLogger();
    }

    public void send(Alert alert) {
        if (!config.isEnabled()) return;
        if (isOnCooldown(alert.getPlayerUuid())) return;

        updateCooldown(alert.getPlayerUuid());

        String chatMessage = AlertFormatter.formatChat(alert);
        broadcastToStaff(chatMessage);

        if (config.isLogToConsole()) {
            logger.info(AlertFormatter.formatConsole(alert));
        }
    }

    public void sendDetailed(Alert alert, Player target) {
        for (String line : AlertFormatter.formatDetailed(alert)) {
            target.sendMessage(line);
        }
    }

    private void broadcastToStaff(String message) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission(config.getPermission())) {
                online.sendMessage(message);
            } else if (config.isBroadcastToOps() && online.isOp()) {
                online.sendMessage(message);
            }
        }
    }

    private boolean isOnCooldown(UUID uuid) {
        Long last = lastAlertTime.get(uuid);
        if (last == null) return false;
        return !TimeUtil.hasExpired(last, config.getAlertCooldown());
    }

    private void updateCooldown(UUID uuid) {
        lastAlertTime.put(uuid, TimeUtil.now());
    }

    public void clearCooldown(UUID uuid) {
        lastAlertTime.remove(uuid);
    }

    public void clearAll() {
        lastAlertTime.clear();
    }
}