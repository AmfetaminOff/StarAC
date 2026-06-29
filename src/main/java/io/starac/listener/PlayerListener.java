package io.starac.listener;

import io.starac.StarAC;
import io.starac.data.DataManager;
import io.starac.data.PlayerData;
import io.starac.violation.manager.ViolationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.logging.Logger;

public final class PlayerListener implements Listener {

    private final StarAC plugin;
    private final DataManager dataManager;
    private final ViolationManager violationManager;
    private final Logger logger;

    public PlayerListener(StarAC plugin, DataManager dataManager, ViolationManager violationManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.violationManager = violationManager;
        this.logger = plugin.getLogger();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        dataManager.getPlayerData(player);
        logger.fine("[PlayerListener] " + player.getName() + " joined, PlayerData created.");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        dataManager.removePlayerData(player.getUniqueId());

        violationManager.removePlayer(player.getUniqueId());

        logger.fine("[PlayerListener] " + player.getName() + " quit, data cleaned.");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerData data = dataManager.getPlayerData(player);

        if (data != null) {
            data.resetMovementState();
            logger.fine("[PlayerListener] " + player.getName() + " respawned, movement state reset.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        PlayerData attackerData = dataManager.getPlayerData(attacker);
        if (attackerData != null) {
            attackerData.recordCombatHit(victim);
        }
    }
}