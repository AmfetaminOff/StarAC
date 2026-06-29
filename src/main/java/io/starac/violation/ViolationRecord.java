package io.starac.violation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record ViolationRecord(
        UUID playerUuid,
        String playerName,
        ViolationType type,
        Severity severity,
        double score,
        String checkName,
        Map<String, String> debugData,
        long timestamp,
        long serverTick
) implements Violation {

    public ViolationRecord {
        if (playerUuid == null) throw new IllegalArgumentException("playerUuid cannot be null");
        if (playerName == null) throw new IllegalArgumentException("playerName cannot be null");
        if (type == null) throw new IllegalArgumentException("type cannot be null");
        if (severity == null) severity = Severity.fromScore(score);
        if (checkName == null) checkName = type.getName();
        if (debugData == null) debugData = Map.of();
        else debugData = Map.copyOf(debugData);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID playerUuid;
        private String playerName;
        private ViolationType type;
        private Severity severity;
        private double score = 0.0;
        private String checkName;
        private final Map<String, String> debugData = new HashMap<>();
        private long serverTick = -1;

        public Builder player(UUID uuid, String name) {
            this.playerUuid = uuid;
            this.playerName = name;
            return this;
        }

        public Builder type(ViolationType type) {
            this.type = type;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder score(double score) {
            this.score = Math.max(0.0, Math.min(1.0, score));
            return this;
        }

        public Builder checkName(String checkName) {
            this.checkName = checkName;
            return this;
        }

        public Builder debug(String key, String value) {
            if (key != null && value != null) this.debugData.put(key, value);
            return this;
        }

        public Builder debug(String key, Number value) {
            if (key != null && value != null) this.debugData.put(key, String.format("%.4f", value.doubleValue()));
            return this;
        }

        public Builder debug(String key, boolean value) {
            if (key != null) this.debugData.put(key, String.valueOf(value));
            return this;
        }

        public Builder serverTick(long tick) {
            this.serverTick = tick;
            return this;
        }

        public ViolationRecord build() {
            return new ViolationRecord(
                    playerUuid,
                    playerName,
                    type,
                    severity,
                    score,
                    checkName,
                    debugData,
                    System.currentTimeMillis(),
                    serverTick
            );
        }
    }
}