package io.starac.violation;

public enum Severity {

    LOW(1, "§a", "§a●", "LOW", 0.00, 0.30),
    MEDIUM(2, "§e", "§e●●", "MEDIUM", 0.30, 0.60),
    HIGH(3, "§c", "§c●●●", "HIGH", 0.60, 0.85),
    CRITICAL(4, "§4", "§4●●●●", "CRITICAL", 0.85, 1.01);

    private final int weight;
    private final String color;
    private final String icon;
    private final String label;
    private final double minScore;
    private final double maxScore;

    Severity(int weight, String color, String icon, String label, double minScore, double maxScore) {
        this.weight = weight;
        this.color = color;
        this.icon = icon;
        this.label = label;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public int getWeight() { return weight; }
    public String getColor() { return color; }
    public String getIcon() { return icon; }
    public String getLabel() { return label; }

    public static Severity fromScore(double score) {
        for (Severity s : values()) {
            if (score >= s.minScore && score < s.maxScore) {
                return s;
            }
        }
        return score >= 1.0 ? CRITICAL : LOW;
    }

    public String format() {
        return icon + " " + color + label;
    }
}