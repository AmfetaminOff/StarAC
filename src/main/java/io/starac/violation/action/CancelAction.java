package io.starac.violation.action;

import io.starac.StarAC;
import io.starac.data.PlayerData;
import io.starac.data.DataManager;
import io.starac.violation.Violation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.logging.Logger;

public final class CancelAction implements Action {

    private final StarAC plugin;
    private final DataManager dataManager;
    private final Logger logger;
    private final int requiredVL;
    private final boolean silent;

    public CancelAction(StarAC plugin, DataManager dataManager, int requiredVL, boolean silent) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.logger = plugin.getLogger();
        this.requiredVL = requiredVL;
        this.silent = silent;
    }

    @Override
    public void execute(Violation violation, Player player, int currentVL) {
        if (player == null || !player.isOnline()) return;

        PlayerData data = dataManager.getPlayerData(player.getUniqueId());
        if (data == null) {
            logger.fine("[CancelAction] PlayerData не найдена для " + player.getName());
            return;
        }

        Location setback = data.getLastSafeLocation();
        if (setback == null || setback.getWorld() == null) {
            logger.fine("[CancelAction] Нет безопасной позиции для отката " + player.getName());
            return;
        }

        if (player.getLocation().distanceSquared(setback) < 0.5) {
            return;
        }

        player.teleport(setback);

        player.setVelocity(new Vector(0, 0, 0));

        if (!silent) {
            player.sendMessage("§c[StarAC] §7Ваше движение было отклонено. (VL: " + currentVL + ")");
        }

        logger.fine("[CancelAction] Откат " + player.getName() + " на "
                + setback.getBlockX() + "," + setback.getBlockY() + "," + setback.getBlockZ());
    }

    @Override
    public String getName() {
        return "SETBACK";
    }

    @Override
    public boolean isSyncRequired() {
        return true;
    }

    @Override
    public int getRequiredVL() {
        return requiredVL;
    }

    public static Builder builder(StarAC plugin, DataManager dataManager) {
        return new Builder(plugin, dataManager);
    }

    public static final class Builder {
        private final StarAC plugin;
        private final DataManager dataManager;
        private int requiredVL = 1;
        private boolean silent = false;

        public Builder(StarAC plugin, DataManager dataManager) {
            this.plugin = plugin;
            this.dataManager = dataManager;
        }

        public Builder requiredVL(int vl) {
            this.requiredVL = Math.max(0, vl);
            return this;
        }

        public Builder silent(boolean silent) {
            this.silent = silent;
            return this;
        }

        public CancelAction build() {
            return new CancelAction(plugin, dataManager, requiredVL, silent);
        }
    }
}