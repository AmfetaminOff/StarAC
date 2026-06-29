package io.starac.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import io.starac.StarAC;
import io.starac.data.DataManager;
import io.starac.violation.manager.ViolationManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public final class PacketListener {

    private final StarAC plugin;
    private final DataManager dataManager;
    private final ViolationManager violationManager;
    private final PacketHandler handler;
    private final Logger logger;

    public PacketListener(StarAC plugin, DataManager dataManager, ViolationManager violationManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.violationManager = violationManager;
        this.handler = new PacketHandler(plugin, dataManager, violationManager);
        this.logger = plugin.getLogger();
    }

    public void register() {
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.HIGHEST,
                        PacketType.Play.Client.FLYING,
                        PacketType.Play.Client.POSITION,
                        PacketType.Play.Client.POSITION_LOOK,
                        PacketType.Play.Client.LOOK) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        if (event.isCancelled()) return;
                        Player player = event.getPlayer();
                        if (player == null || !player.isOnline()) return;

                        long currentTick = Bukkit.getCurrentTick();
                        dataManager.updatePlayerState(player, currentTick);
                        handler.handleMovement(player, event);
                    }
                }
        );

        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.MONITOR,
                        PacketType.Play.Client.ARM_ANIMATION) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        if (event.isCancelled()) return;
                        Player player = event.getPlayer();
                        if (player == null || !player.isOnline()) return;
                        handler.handleArmSwing(player, event);
                    }
                }
        );

        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.MONITOR,
                        PacketType.Play.Client.USE_ENTITY) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        if (event.isCancelled()) return;
                        Player player = event.getPlayer();
                        if (player == null || !player.isOnline()) return;
                        handler.handleUseEntity(player, event);
                    }
                }
        );

        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.MONITOR,
                        PacketType.Play.Client.BLOCK_DIG) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        if (event.isCancelled()) return;
                        Player player = event.getPlayer();
                        if (player == null || !player.isOnline()) return;
                        handler.handleBlockDig(player, event);
                    }
                }
        );

        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.MONITOR,
                        PacketType.Play.Client.BLOCK_PLACE,
                        PacketType.Play.Client.USE_ITEM) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        if (event.isCancelled()) return;
                        Player player = event.getPlayer();
                        if (player == null || !player.isOnline()) return;
                        handler.handleBlockPlace(player, event);
                    }
                }
        );

        logger.info("[PacketListener] Зарегистрировано 5 слушателей пакетов.");
    }

    public void unregister() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(plugin);
        logger.info("[PacketListener] Все слушатели удалены.");
    }
}