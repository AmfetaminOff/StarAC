package io.starac.violation;

import java.util.Map;
import java.util.UUID;

public interface Violation {

    UUID playerUuid();
    String playerName();
    ViolationType type();
    Severity severity();
    double score();
    String checkName();
    Map<String, String> debugData();
    long timestamp();
    long serverTick();

    default double calculateWeight() {
        return type().getBaseWeight() * severity().getWeight() * (1.0 + score());
    }

    default String toLogString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(type().name()).append("|").append(type().getCode()).append("] ");
        sb.append(playerName()).append(" (").append(String.format("%.2f", score())).append(")");

        if (!debugData().isEmpty()) {
            sb.append(" ");
            debugData().forEach((k, v) -> sb.append(k).append("=").append(v).append(", "));
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

    default String toAlertString(int currentVL) {
        return type().getFormattedName() + " §f" + playerName()
                + " §7| " + severity().format()
                + " §7| score: §f" + String.format("%.2f", score())
                + " §7| VL: §f" + currentVL;
    }
}