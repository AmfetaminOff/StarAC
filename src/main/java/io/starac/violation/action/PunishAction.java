package io.starac.violation.action;

import io.starac.StarAC;
import io.starac.violation.Violation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public final class PunishAction implements Action {

    public enum PunishmentType {
        KICK("Кик", true),
        BAN("Бан", true),
        MUTE("Мут", true);

        private final String displayName;
        private final boolean syncRequired;

        PunishmentType(String displayName, boolean syncRequired) {
            this.displayName = displayName;
            this.syncRequired = syncRequired;
        }

        public String getDisplayName() { return displayName; }
    }

    private final StarAC plugin;
    private final Logger logger;
    private final PunishmentType type;
    private final int requiredVL;
    private final String reasonTemplate;
    private final String duration;

    public PunishAction(StarAC plugin, PunishmentType type, int requiredVL,
                        String reasonTemplate, String duration) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.type = type;
        this.requiredVL = requiredVL;
        this.reasonTemplate = reasonTemplate;
        this.duration = duration != null ? duration : "permanent";
    }

    @Override
    public void execute(Violation violation, Player player, int currentVL) {
        String reason = resolveReason(violation, currentVL);

        switch (type) {
            case KICK:
                kickPlayer(player, reason);
                break;
            case BAN:
                banPlayer(violation, reason);
                break;
            case MUTE:
                mutePlayer(violation, reason);
                break;
        }
    }

    private String resolveReason(Violation violation, int currentVL) {
        if (reasonTemplate == null || reasonTemplate.isBlank()) {
            return "§c[StarAC] §f" + violation.type().getName() + " (VL=" + currentVL + ")";
        }
        return reasonTemplate
                .replace("{player}", violation.playerName())
                .replace("{type}", violation.type().getName())
                .replace("{vl}", String.valueOf(currentVL))
                .replace("{score}", String.format("%.2f", violation.score()))
                .replace("{code}", violation.type().getCode())
                .replace("{category}", violation.type().getCategory().getDisplayName());
    }

    private void kickPlayer(Player player, String reason) {
        if (player == null || !player.isOnline()) return;
        player.kickPlayer(reason);
        logger.warning("[PunishAction] KICK " + player.getName() + " | " + reason);
    }

    private void banPlayer(Violation violation, String reason) {
        String cleanReason = reason.replaceAll("§[0-9a-fk-or]", "").trim();
        String playerName = violation.playerName();

        if (Bukkit.getPluginManager().isPluginEnabled("LiteBans")) {
            String cmd = String.format("litebans:ban %s %s %s",
                    playerName, cleanReason, duration);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            logger.warning("[PunishAction] BAN (LiteBans) " + playerName);
            return;
        }

        if (Bukkit.getPluginManager().isPluginEnabled("AdvancedBan")) {
            String cmd = String.format("advancedban:ban %s %s %s",
                    playerName, cleanReason, duration);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            logger.warning("[PunishAction] BAN (AdvancedBan) " + playerName);
            return;
        }

        Player player = Bukkit.getPlayer(violation.playerUuid());
        if (player != null && player.isOnline()) {
            player.kickPlayer("§c[StarAC] §fВы были забанены. Причина: " + cleanReason);
        }
        logger.warning("[PunishAction] BAN (fallback) " + playerName + " | " + cleanReason);
    }

    private void mutePlayer(Violation violation, String reason) {
        String cleanReason = reason.replaceAll("§[0-9a-fk-or]", "").trim();
        String playerName = violation.playerName();

        if (Bukkit.getPluginManager().isPluginEnabled("LiteBans")) {
            String cmd = String.format("litebans:mute %s %s %s",
                    playerName, cleanReason, duration);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            return;
        }

        if (Bukkit.getPluginManager().isPluginEnabled("AdvancedBan")) {
            String cmd = String.format("advancedban:mute %s %s %s",
                    playerName, cleanReason, duration);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            return;
        }

        logger.warning("[PunishAction] MUTE (no plugin) " + playerName);
    }

    @Override
    public String getName() {
        return type.name();
    }

    @Override
    public boolean isSyncRequired() {
        return type.syncRequired;
    }

    @Override
    public int getRequiredVL() {
        return requiredVL;
    }

    public static Builder builder(StarAC plugin) {
        return new Builder(plugin);
    }

    public static final class Builder {
        private final StarAC plugin;
        private PunishmentType type = PunishmentType.KICK;
        private int requiredVL = 10;
        private String reasonTemplate = "§c[StarAC] §f{type} detected (VL={vl}, score={score}).";
        private String duration = "permanent";

        public Builder(StarAC plugin) {
            this.plugin = plugin;
        }

        public Builder type(PunishmentType type) {
            this.type = type;
            return this;
        }

        public Builder requiredVL(int vl) {
            this.requiredVL = Math.max(0, vl);
            return this;
        }

        public Builder reason(String template) {
            this.reasonTemplate = template;
            return this;
        }

        public Builder duration(String duration) {
            this.duration = duration;
            return this;
        }

        public PunishAction build() {
            return new PunishAction(plugin, type, requiredVL, reasonTemplate, duration);
        }
    }
}