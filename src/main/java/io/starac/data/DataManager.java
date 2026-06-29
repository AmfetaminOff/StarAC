package io.starac.data;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DataManager {

    private final ConcurrentHashMap<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    public PlayerData getPlayerData(Player player) {
        if (player == null) return null;
        return playerDataMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerData(player));
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    public boolean hasPlayerData(UUID uuid) {
        return playerDataMap.containsKey(uuid);
    }

    public void removePlayerData(UUID uuid) {
        PlayerData data = playerDataMap.remove(uuid);
        if (data != null) {
            data.clear();
        }
    }

    public Collection<PlayerData> getAllPlayerData() {
        return Collections.unmodifiableCollection(playerDataMap.values());
    }

    public int getActivePlayerCount() {
        return playerDataMap.size();
    }

    public void clearAll() {
        playerDataMap.values().forEach(PlayerData::clear);
        playerDataMap.clear();
    }

    public void updatePlayerState(Player player, long serverTick) {
        PlayerData data = getPlayerData(player);
        if (data != null) {
            data.updateState(player, serverTick);
        }
    }
}