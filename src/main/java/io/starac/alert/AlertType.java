package io.starac.alert;

public enum AlertType {

    SUSPICIOUS("§e⚠ SUSPICIOUS", "§e"),
    CHEAT("§c✖ CHEAT", "§c"),
    PUNISHED("§4✖ PUNISHED", "§4"),
    INFO("§7ℹ INFO", "§7");

    private final String label;
    private final String color;

    AlertType(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() { return label; }
    public String getColor() { return color; }
}