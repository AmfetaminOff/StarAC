package io.starac.alert;

import io.starac.util.StringUtil;

public final class AlertFormatter {

    private AlertFormatter() {}

    public static String formatChat(Alert alert) {
        String score = StringUtil.formatScore(alert.getScore());
        String flags = StringUtil.formatFlags(alert.getFlags());
        String color = alert.getType().getColor();

        return "§8[§bStarAC§8] " +
                alert.getType().getLabel() + " §8| " +
                "§f" + alert.getPlayerName() + " §8| " +
                "§7score: " + color + score + " §8| " +
                "§7flags: §f" + flags;
    }

    public static String formatConsole(Alert alert) {
        String score = StringUtil.formatScorePrecise(alert.getScore());
        String flags = StringUtil.formatFlags(alert.getFlags());

        return "[StarAC][" + alert.getType().name() + "] " +
                alert.getPlayerName() +
                " | check=" + alert.getCheckName() +
                " | score=" + score +
                " | flags=[" + flags + "]";
    }

    public static String formatKickMessage(Alert alert) {
        return "§c§lStarAC §r§c— Cheat Detection\n\n" +
                "§7You were removed for suspicious behavior.\n" +
                "§7Score: §f" + StringUtil.formatScore(alert.getScore()) + "\n" +
                (alert.getFlags().isEmpty() ? "" :
                        "§7Flags: §f" + StringUtil.formatFlags(alert.getFlags()) + "\n") +
                "\n§eIf you believe this is a mistake, contact staff.";
    }

    public static String[] formatDetailed(Alert alert) {
        return new String[]{
                "§8§m----------------------------------------",
                "§bStarAC §7— Alert Details",
                "§7Player:    §f" + alert.getPlayerName(),
                "§7UUID:      §f" + alert.getPlayerUuid(),
                "§7Type:      " + alert.getType().getLabel(),
                "§7Check:     §f" + alert.getCheckName(),
                "§7Score:     §f" + StringUtil.formatScorePrecise(alert.getScore()) +
                        " " + StringUtil.confidenceBar(alert.getScore(), 10),
                "§7Flags:     §f" + StringUtil.formatFlags(alert.getFlags()),
                "§7Time:      §f" + formatTime(alert.getTimestamp()),
                "§8§m----------------------------------------"
        };
    }

    private static String formatTime(long timestamp) {
        java.time.LocalDateTime dt = java.time.LocalDateTime
                .ofInstant(java.time.Instant.ofEpochMilli(timestamp),
                        java.time.ZoneId.systemDefault());
        return String.format("%02d:%02d:%02d", dt.getHour(), dt.getMinute(), dt.getSecond());
    }
}