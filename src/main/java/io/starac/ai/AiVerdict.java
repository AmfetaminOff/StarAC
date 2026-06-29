package io.starac.ai;

public enum AiVerdict {

    CLEAN("CLEAN", "§a", "Чист", 0.0),

    UNKNOWN("UNKNOWN", "§7", "Неизвестно", 0.1),

    SUSPICIOUS("SUSPICIOUS", "§e", "Подозрителен", 0.4),

    CHEATING("CHEATING", "§c", "Читер", 0.7),

    CONFIRMED("CONFIRMED", "§4", "Подтверждён", 0.9),

    ERROR("ERROR", "§8", "Ошибка", 0.0);

    private final String id;
    private final String color;
    private final String displayName;
    private final double threshold;

    AiVerdict(String id, String color, String displayName, double threshold) {
        this.id = id;
        this.color = color;
        this.displayName = displayName;
        this.threshold = threshold;
    }

    public String getId() { return id; }
    public String getColor() { return color; }
    public String getDisplayName() { return displayName; }
    public double getThreshold() { return threshold; }

    public String getFormatted() {
        return color + displayName;
    }

    public String getFormattedWithConfidence(double confidence) {
        return String.format("%s%s §7(§f%.0f%%§7)",
                color, displayName, confidence * 100);
    }

    public static AiVerdict fromString(String str) {
        if (str == null || str.isBlank()) return UNKNOWN;
        String normalized = str.trim().toUpperCase();

        for (AiVerdict v : values()) {
            if (v.id.equals(normalized)) {
                return v;
            }
        }
        return UNKNOWN;
    }

    public static AiVerdict fromConfidence(double confidence) {
        if (confidence >= CONFIRMED.threshold) return CONFIRMED;
        if (confidence >= CHEATING.threshold) return CHEATING;
        if (confidence >= SUSPICIOUS.threshold) return SUSPICIOUS;
        return CLEAN;
    }

    public boolean isActionable() {
        return this == CHEATING || this == CONFIRMED;
    }

    public boolean isHighConfidence() {
        return this == CONFIRMED;
    }

    public boolean requiresMonitoring() {
        return this == SUSPICIOUS || this == CHEATING;
    }

    public boolean isSafe() {
        return this == CLEAN;
    }

    public AiVerdict escalate() {
        return switch (this) {
            case CLEAN -> SUSPICIOUS;
            case UNKNOWN -> SUSPICIOUS;
            case SUSPICIOUS -> CHEATING;
            case CHEATING -> CONFIRMED;
            case CONFIRMED -> CONFIRMED;
            case ERROR -> UNKNOWN;
        };
    }

    @Override
    public String toString() {
        return id;
    }
}