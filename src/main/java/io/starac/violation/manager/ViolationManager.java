package io.starac.violation.manager;

import io.starac.StarAC;
import io.starac.ai.AiFeatureSet;
import io.starac.ai.feature.FeatureExtractor;
import io.starac.data.DataManager;
import io.starac.data.PlayerData;
import io.starac.violation.*;
import io.starac.violation.action.Action;
import io.starac.violation.history.ViolationHistory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class ViolationManager {

    private final StarAC plugin;
    private final DataManager dataManager;
    private final PunishmentProfile punishmentProfile;
    private final ViolationHistory history;
    private final Logger logger;

    private final ConcurrentHashMap<UUID, ConcurrentHashMap<ViolationType, DecayableVL>> playerVLs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> actionCooldowns = new ConcurrentHashMap<>();
    private final long globalFlagCooldownMs;

    public ViolationManager(StarAC plugin, DataManager dataManager,
                            PunishmentProfile punishmentProfile,
                            ViolationHistory history) {
        AiFeatureSet features = FeatureExtractor.extract(data);
        String label = aiVerdict != null ? verdict.name() : "CLEAN";
        int vl = violationManager.getTotalVL(player.getUniqueId());
        plugin.getSessionRecorder().recordFrame(player, data, features, label, vl);
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.punishmentProfile = punishmentProfile;
        this.history = history;
        this.logger = plugin.getLogger();
        this.globalFlagCooldownMs = plugin.getViolationConfig().getGlobalCooldownMs();
    }

    public void flag(Player player, ViolationType type, String checkName,
                     double score, Map<String, String> debugData) {
        if (player == null || !player.isOnline()) return;
        if (player.hasPermission("starac.bypass")) return;

        flag(player.getUniqueId(), player.getName(), type, checkName, score, debugData);
    }

    public void flag(UUID uuid, String playerName, ViolationType type,
                     String checkName, double score, Map<String, String> debugData) {

        DecayableVL decayableVL = getOrCreateVL(uuid, type);
        ViolationRecord violation = ViolationRecord.builder()
                .player(uuid, playerName)
                .type(type)
                .score(score)
                .checkName(checkName)
                .serverTick(Bukkit.getCurrentTick())
                .debugData(debugData)
                .build();

        int newVL = decayableVL.addViolation(violation);

        logger.fine("[Violation] " + violation.toLogString() + " | VL=" + newVL);
        if (history != null) {
            history.saveAsync(violation, newVL);
        }
        checkAndExecuteActions(violation, newVL);
    }

    private DecayableVL getOrCreateVL(UUID uuid, ViolationType type) {
        return playerVLs
                .computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(type, k -> punishmentProfile.createDecayableVL(type));
    }

    private void checkAndExecuteActions(Violation violation, int currentVL) {
        List<Action> actions = punishmentProfile.getActions(violation.type());
        if (actions == null || actions.isEmpty()) return;

        for (Action action : actions) {
            if (currentVL >= action.getRequiredVL()) {
                String cooldownKey = violation.playerUuid() + ":" + violation.type() + ":" + action.getName();
                if (isOnCooldown(cooldownKey)) {
                    continue;
                }

                executeAction(action, violation, currentVL);
                updateCooldown(cooldownKey);
            }
        }
    }

    private void executeAction(Action action, Violation violation, int currentVL) {
        Player player = Bukkit.getPlayer(violation.playerUuid());

        int delay = action.getDelayTicks();
        if (delay > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                doExecute(action, violation, player, currentVL);
            }, delay);
        } else {
            doExecute(action, violation, player, currentVL);
        }
    }

    private void doExecute(Action action, Violation violation, Player player, int currentVL) {
        try {
            if (action.isSyncRequired()) {
                if (Bukkit.isPrimaryThread()) {
                    action.execute(violation, player, currentVL);
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> action.execute(violation, player, currentVL));
                }
            } else {
                if (Bukkit.isPrimaryThread()) {
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> action.execute(violation, player, currentVL));
                } else {
                    action.execute(violation, player, currentVL);
                }
            }
        } catch (Exception e) {
            logger.warning("[ViolationManager] Ошибка выполнения action " + action.getName() + ": " + e.getMessage());
        }
    }

    public void performDecay() {
        for (Map.Entry<UUID, ConcurrentHashMap<ViolationType, DecayableVL>> entry : playerVLs.entrySet()) {
            ConcurrentHashMap<ViolationType, DecayableVL> vlMap = entry.getValue();

            boolean allInactive = vlMap.values().stream().allMatch(DecayableVL::isInactive);
            if (allInactive) {
                vlMap.values().forEach(DecayableVL::reset);
                continue;
            }
            vlMap.values().forEach(DecayableVL::applyDecay);
        }
        cleanExpiredCooldowns();
    }

    private boolean isOnCooldown(String key) {
        Long last = actionCooldowns.get(key);
        if (last == null) return false;
        return !io.starac.util.TimeUtil.hasExpired(last, globalFlagCooldownMs);
    }

    private void updateCooldown(String key) {
        actionCooldowns.put(key, io.starac.util.TimeUtil.now());
    }

    private void cleanExpiredCooldowns() {
        long now = io.starac.util.TimeUtil.now();
        actionCooldowns.entrySet().removeIf(e -> io.starac.util.TimeUtil.hasExpired(e.getValue(), 60_000L));
    }

    public int getVL(UUID uuid, ViolationType type) {
        ConcurrentHashMap<ViolationType, DecayableVL> vlMap = playerVLs.get(uuid);
        if (vlMap == null) return 0;
        DecayableVL vl = vlMap.get(type);
        return vl != null ? vl.getVL() : 0;
    }

    public int getTotalVL(UUID uuid) {
        ConcurrentHashMap<ViolationType, DecayableVL> vlMap = playerVLs.get(uuid);
        if (vlMap == null) return 0;
        return vlMap.values().stream().mapToInt(DecayableVL::getVL).sum();
    }

    public Map<ViolationType, Integer> getActiveVLs(UUID uuid) {
        ConcurrentHashMap<ViolationType, DecayableVL> vlMap = playerVLs.get(uuid);
        if (vlMap == null) return Collections.emptyMap();

        Map<ViolationType, Integer> result = new EnumMap<>(ViolationType.class);
        vlMap.forEach((type, vl) -> {
            if (vl.getVL() > 0) {
                result.put(type, vl.getVL());
            }
        });
        return Collections.unmodifiableMap(result);
    }

    public void resetVL(UUID uuid, ViolationType type) {
        ConcurrentHashMap<ViolationType, DecayableVL> vlMap = playerVLs.get(uuid);
        if (vlMap != null) {
            DecayableVL vl = vlMap.get(type);
            if (vl != null) {
                vl.reset();
            }
        }
    }

    public void resetAllVL(UUID uuid) {
        ConcurrentHashMap<ViolationType, DecayableVL> vlMap = playerVLs.get(uuid);
        if (vlMap != null) {
            vlMap.values().forEach(DecayableVL::reset);
        }
    }

    public void removePlayer(UUID uuid) {
        playerVLs.remove(uuid);
        actionCooldowns.entrySet().removeIf(e -> e.getKey().startsWith(uuid.toString()));
    }

    public int getActivePlayerCount() {
        return (int) playerVLs.values().stream()
                .filter(m -> m.values().stream().anyMatch(vl -> vl.getVL() > 0))
                .count();
    }

    public void clearAll() {
        playerVLs.clear();
        actionCooldowns.clear();
    }
}