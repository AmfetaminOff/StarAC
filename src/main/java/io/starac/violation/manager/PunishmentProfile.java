package io.starac.violation.manager;

import io.starac.StarAC;
import io.starac.alert.AlertManager;
import io.starac.data.DataManager;
import io.starac.violation.ViolationType;
import io.starac.violation.action.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public final class PunishmentProfile {

    private final StarAC plugin;
    private final AlertManager alertManager;
    private final DataManager dataManager;
    private final Map<ViolationType, List<Action>> actionsByType;
    private final Map<ViolationType, DecayConfig> decayConfigs;

    private PunishmentProfile(Builder b) {
        this.plugin = b.plugin;
        this.alertManager = b.alertManager;
        this.dataManager = b.dataManager;
        this.actionsByType = Collections.unmodifiableMap(new EnumMap<>(b.actionsByType));
        this.decayConfigs = Collections.unmodifiableMap(new EnumMap<>(b.decayConfigs));
    }

    public List<Action> getActions(ViolationType type) {
        return actionsByType.getOrDefault(type, Collections.emptyList());
    }

    public DecayableVL createDecayableVL(ViolationType type) {
        DecayConfig cfg = decayConfigs.getOrDefault(type, DecayConfig.defaults(type));
        return DecayableVL.builder(type)
                .maxVL(cfg.maxVL)
                .strategy(cfg.strategy)
                .decayIntervalTicks(cfg.decayIntervalTicks)
                .decayAmount(cfg.decayAmount)
                .decayFactor(cfg.decayFactor)
                .inactivityResetMs(cfg.inactivityResetMs)
                .build();
    }

    public static PunishmentProfile load(StarAC plugin, AlertManager alertManager,
                                         DataManager dataManager, FileConfiguration cfg) {
        Builder b = new Builder(plugin, alertManager, dataManager);

        ConfigurationSection section = cfg.getConfigurationSection("punishments");
        if (section == null) {
            plugin.getLogger().warning("[PunishmentProfile] Секция 'punishments' не найдена, используются дефолты.");
            return defaults(plugin, alertManager, dataManager);
        }

        for (String typeName : section.getKeys(false)) {
            ViolationType type = ViolationType.fromString(typeName);
            if (type == null) {
                plugin.getLogger().warning("[PunishmentProfile] Неизвестный тип: " + typeName);
                continue;
            }

            List<Map<?, ?>> actionMaps = section.getMapList(typeName);
            for (Map<?, ?> actionMap : actionMaps) {
                Action action = parseAction(plugin, alertManager, dataManager, actionMap);
                if (action != null) {
                    b.addAction(type, action);
                }
            }

            ConfigurationSection decaySection = section.getConfigurationSection(typeName + "-decay");
            if (decaySection != null) {
                DecayConfig decayCfg = DecayConfig.fromConfig(type, decaySection);
                b.decayConfig(type, decayCfg);
            }
        }

        return b.build();
    }

    private static Action parseAction(StarAC plugin, AlertManager alertManager,
                                      DataManager dataManager, Map<?, ?> map) {
        try {
            String actionStr = String.valueOf(map.getOrDefault("action", "ALERT"));
            int vl = map.containsKey("vl") ? ((Number) map.get("vl")).intValue() : 0;

            return switch (actionStr.toUpperCase()) {
                case "ALERT" -> AlertAction.builder(alertManager)
                        .requiredVL(vl)
                        .includeDebugData(true)
                        .build();

                case "SETBACK", "CANCEL" -> CancelAction.builder(plugin, dataManager)
                        .requiredVL(vl)
                        .silent(map.containsKey("silent") && (Boolean) map.get("silent"))
                        .build();

                case "LOG" -> LogAction.builder(plugin)
                        .requiredVL(vl)
                        .target(LogAction.LogTarget.BOTH)
                        .build();

                case "KICK" -> PunishAction.builder(plugin)
                        .type(PunishAction.PunishmentType.KICK)
                        .requiredVL(vl)
                        .reason(String.valueOf(map.getOrDefault("reason", "§c[StarAC] §f{type} detected (VL={vl}).")))
                        .build();

                case "BAN" -> PunishAction.builder(plugin)
                        .type(PunishAction.PunishmentType.BAN)
                        .requiredVL(vl)
                        .reason(String.valueOf(map.getOrDefault("reason", "§c[StarAC] §f{type} confirmed (VL={vl}).")))
                        .duration(String.valueOf(map.getOrDefault("duration", "permanent")))
                        .build();

                case "MUTE" -> PunishAction.builder(plugin)
                        .type(PunishAction.PunishmentType.MUTE)
                        .requiredVL(vl)
                        .reason(String.valueOf(map.getOrDefault("reason", "§c[StarAC] §f{type} spam (VL={vl}).")))
                        .duration(String.valueOf(map.getOrDefault("duration", "1h")))
                        .build();

                default -> null;
            };

        } catch (Exception e) {
            plugin.getLogger().warning("[PunishmentProfile] Ошибка парсинга action: " + e.getMessage());
            return null;
        }
    }

    public static PunishmentProfile defaults(StarAC plugin, AlertManager alertManager, DataManager dataManager) {
        Builder b = new Builder(plugin, alertManager, dataManager);


        b.addAction(ViolationType.KILLAURA, AlertAction.builder(alertManager).requiredVL(3).build());
        b.addAction(ViolationType.KILLAURA, CancelAction.builder(plugin, dataManager).requiredVL(5).build());
        b.addAction(ViolationType.KILLAURA, PunishAction.builder(plugin).type(PunishAction.PunishmentType.KICK).requiredVL(10).build());
        b.addAction(ViolationType.KILLAURA, PunishAction.builder(plugin).type(PunishAction.PunishmentType.BAN).requiredVL(15).duration("7d").build());
        b.addAction(ViolationType.FLY, AlertAction.builder(alertManager).requiredVL(2).build());
        b.addAction(ViolationType.FLY, CancelAction.builder(plugin, dataManager).requiredVL(4).build());
        b.addAction(ViolationType.FLY, PunishAction.builder(plugin).type(PunishAction.PunishmentType.KICK).requiredVL(8).build());
        b.addAction(ViolationType.SPEED, AlertAction.builder(alertManager).requiredVL(3).build());
        b.addAction(ViolationType.SPEED, CancelAction.builder(plugin, dataManager).requiredVL(5).build());
        b.addAction(ViolationType.SPEED, PunishAction.builder(plugin).type(PunishAction.PunishmentType.KICK).requiredVL(10).build());
        b.addAction(ViolationType.REACH, AlertAction.builder(alertManager).requiredVL(3).build());
        b.addAction(ViolationType.REACH, CancelAction.builder(plugin, dataManager).requiredVL(5).build());
        b.addAction(ViolationType.REACH, PunishAction.builder(plugin).type(PunishAction.PunishmentType.KICK).requiredVL(10).build());
        b.addAction(ViolationType.SCAFFOLD, AlertAction.builder(alertManager).requiredVL(3).build());
        b.addAction(ViolationType.SCAFFOLD, CancelAction.builder(plugin, dataManager).requiredVL(5).build());
        b.addAction(ViolationType.SCAFFOLD, PunishAction.builder(plugin).type(PunishAction.PunishmentType.KICK).requiredVL(10).build());

        return b.build();
    }

    public static final class DecayConfig {
        final int maxVL;
        final DecayableVL.DecayStrategy strategy;
        final int decayIntervalTicks;
        final double decayAmount;
        final double decayFactor;
        final long inactivityResetMs;

        private DecayConfig(int maxVL, DecayableVL.DecayStrategy strategy, int decayIntervalTicks,
                            double decayAmount, double decayFactor, long inactivityResetMs) {
            this.maxVL = maxVL;
            this.strategy = strategy;
            this.decayIntervalTicks = decayIntervalTicks;
            this.decayAmount = decayAmount;
            this.decayFactor = decayFactor;
            this.inactivityResetMs = inactivityResetMs;
        }

        public static DecayConfig defaults(ViolationType type) {
            return new DecayConfig(type.getMaxVL(), DecayableVL.DecayStrategy.EXPONENTIAL,
                    1200, 1.0, 0.9, 300_000L);
        }

        public static DecayConfig fromConfig(ViolationType type, ConfigurationSection section) {
            String strategyStr = section.getString("strategy", "EXPONENTIAL");
            DecayableVL.DecayStrategy strategy = DecayableVL.DecayStrategy.valueOf(strategyStr.toUpperCase());

            return new DecayConfig(
                    section.getInt("max-vl", type.getMaxVL()),
                    strategy,
                    section.getInt("decay-interval-ticks", 1200),
                    section.getDouble("decay-amount", 1.0),
                    section.getDouble("decay-factor", 0.9),
                    section.getLong("inactivity-reset-ms", 300_000L)
            );
        }
    }

    public static final class Builder {
        private final StarAC plugin;
        private final AlertManager alertManager;
        private final DataManager dataManager;
        private final Map<ViolationType, List<Action>> actionsByType = new EnumMap<>(ViolationType.class);
        private final Map<ViolationType, DecayConfig> decayConfigs = new EnumMap<>(ViolationType.class);

        public Builder(StarAC plugin, AlertManager alertManager, DataManager dataManager) {
            this.plugin = plugin;
            this.alertManager = alertManager;
            this.dataManager = dataManager;
        }

        public Builder addAction(ViolationType type, Action action) {
            actionsByType.computeIfAbsent(type, k -> new ArrayList<>()).add(action);
            actionsByType.get(type).sort(Comparator.comparingInt(Action::getRequiredVL));
            return this;
        }

        public Builder decayConfig(ViolationType type, DecayConfig cfg) {
            decayConfigs.put(type, cfg);
            return this;
        }

        public PunishmentProfile build() {
            return new PunishmentProfile(this);
        }
    }
}