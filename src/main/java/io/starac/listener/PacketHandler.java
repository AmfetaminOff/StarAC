package io.starac.listener;

import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import io.starac.StarAC;
import io.starac.data.DataManager;
import io.starac.data.PlayerData;
import io.starac.violation.ViolationType;
import io.starac.violation.manager.ViolationManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.logging.Logger;

public final class PacketHandler {

    private final StarAC plugin;
    private final DataManager dataManager;
    private final ViolationManager violationManager;
    private final Logger logger;
    private static final double MAX_VANILLA_REACH = 3.1;

    public PacketHandler(StarAC plugin, DataManager dataManager, ViolationManager violationManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.violationManager = violationManager;
        this.logger = plugin.getLogger();
    }

    public void handleMovement(Player player, PacketEvent event) {
        PlayerData data = dataManager.getPlayerData(player);
        if (data == null) return;
        if (data.getAirTicks() > 40 && !player.isFlying() && !player.isGliding()
                && !player.hasPermission("starac.bypass.fly")) {
        }
    }

    public void handleArmSwing(Player player, PacketEvent event) {
        PlayerData data = dataManager.getPlayerData(player);
        if (data == null) return;

        data.recordClick();

        if (data.getCps() > 25.0 && !player.hasPermission("starac.bypass.autoclicker")) {
            violationManager.flag(player, ViolationType.AUTOCLICKER, "AutoClicker (A)", 0.6,
                    Map.of("cps", String.format("%.1f", data.getCps()),
                            "variance", String.format("%.2f", data.getClickVariance())));
        }
    }


    public void handleUseEntity(Player player, PacketEvent event) {
        EnumWrappers.EntityUseAction action = null;
        try {
            action = event.getPacket().getEntityUseActions().readSafely(0);
        } catch (Exception e) {
        }

        if (action != null && action != EnumWrappers.EntityUseAction.ATTACK) {
            return;
        }

        Entity target = event.getPacket().getEntityModifier(player.getWorld()).readSafely(0);
        if (!(target instanceof Player targetPlayer)) {
            return;
        }

        PlayerData data = dataManager.getPlayerData(player);
        if (data == null) return;

        double reach = calculateReach(player, targetPlayer);

        data.recordAttack(targetPlayer, reach);

        if (reach > MAX_VANILLA_REACH + 0.5 && !player.hasPermission("starac.bypass.reach")) {
            violationManager.flag(player, ViolationType.REACH, "Reach (B)", 0.75,
                    Map.of("reach", String.format("%.3f", reach),
                            "target", targetPlayer.getName(),
                            "maxVanilla", String.valueOf(MAX_VANILLA_REACH)));
        }

        if (isKillAuraSuspicious(data, targetPlayer)) {
            violationManager.flag(player, ViolationType.KILLAURA, "KillAura (A)", 0.65,
                    Map.of("target", targetPlayer.getName(),
                            "reach", String.format("%.3f", reach)));
        }
    }

    public void handleBlockDig(Player player, PacketEvent event) {
        PlayerData data = dataManager.getPlayerData(player);
        if (data == null) return;
    }

    public void handleBlockPlace(Player player, PacketEvent event) {
        PlayerData data = dataManager.getPlayerData(player);
        if (data == null) return;
    }

    private double calculateReach(Player attacker, Player target) {
        Location eye = attacker.getEyeLocation();
        Location targetLoc = target.getLocation();

        double targetY = targetLoc.getY() + 0.9;

        double dx = eye.getX() - targetLoc.getX();
        double dy = eye.getY() - targetY;
        double dz = eye.getZ() - targetLoc.getZ();

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private boolean isKillAuraSuspicious(PlayerData data, Player target) {
        long now = System.currentTimeMillis();
        long lastAttack = data.getLastAttackTime();

        if (now - lastAttack < 50) {
            return true;
        }

        if (!data.getGcdValues().isEmpty()) {
            double avgGcd = data.getGcdValues().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            if (avgGcd < 0.005 && avgGcd > 0) {
                return true;
            }
        }

        return false;
    }
}