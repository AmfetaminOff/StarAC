package io.starac.util;

public final class StringUtil {

    private StringUtil() {}

    public static String formatScore(double score) {
        return String.format("%.0f%%", score * 100);
    }

    public static String formatScorePrecise(double score) {
        return String.format("%.1f%%", score * 100);
    }

    public static String confidenceBar(double score, int length) {
        int filled = (int) Math.round(score * length);
        filled = Math.max(0, Math.min(filled, length));

        String color = score >= 0.85 ? "§c" : score >= 0.55 ? "§e" : "§a";

        return color + "█".repeat(filled) + "§7" + "░".repeat(length - filled);
    }

    public static String coloredVerdict(String verdict) {
        return switch (verdict.toUpperCase()) {
            case "CHEAT"      -> "§c" + verdict;
            case "SUSPICIOUS" -> "§e" + verdict;
            case "LEGIT"      -> "§a" + verdict;
            default           -> "§7" + verdict;
        };
    }

    public static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    public static String formatFlags(java.util.List<String> flags) {
        if (flags == null || flags.isEmpty()) return "none";
        return String.join(", ", flags);
    }
}