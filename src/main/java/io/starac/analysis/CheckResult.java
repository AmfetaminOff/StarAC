package io.starac.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class CheckResult {

    private static final CheckResult CLEAN = new CheckResult(0.0, Collections.emptyMap(), false);

    private final double score;
    private final Map<String, String> debugData;
    private final boolean flag;

    private CheckResult(double score, Map<String, String> debugData, boolean flag) {
        this.score = Math.max(0.0, Math.min(1.0, score));
        this.debugData = Collections.unmodifiableMap(new HashMap<>(debugData));
        this.flag = flag;
    }

    public double getScore() {
        return score;
    }

    public Map<String, String> getDebugData() {
        return debugData;
    }

    public boolean isFlag() {
        return flag;
    }

    public String getDebug(String key) {
        return debugData.get(key);
    }

    public static CheckResult flag(double score, Map<String, String> debugData) {
        if (score <= 0.0) {
            return CLEAN;
        }
        return new CheckResult(score, debugData != null ? debugData : Collections.emptyMap(), true);
    }

    public static CheckResult flag(double score) {
        return flag(score, Collections.emptyMap());
    }

    public static CheckResult flag(double score, String debugKey, String debugValue) {
        Map<String, String> debug = new HashMap<>();
        debug.put(debugKey, debugValue);
        return flag(score, debug);
    }

    public static CheckResult clean() {
        return CLEAN;
    }

    public static CheckResult cleanWithDebug(Map<String, String> debugData) {
        return new CheckResult(0.0, debugData != null ? debugData : Collections.emptyMap(), false);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private double score = 0.0;
        private final Map<String, String> debugData = new HashMap<>();
        private boolean flag = false;

        public Builder score(double score) {
            this.score = score;
            return this;
        }

        public Builder debug(String key, String value) {
            if (key != null && value != null) {
                debugData.put(key, value);
            }
            return this;
        }

        public Builder debug(String key, Number value) {
            if (key != null && value != null) {
                debugData.put(key, String.format("%.4f", value.doubleValue()));
            }
            return this;
        }

        public Builder debug(String key, boolean value) {
            if (key != null) {
                debugData.put(key, String.valueOf(value));
            }
            return this;
        }

        public Builder flag(boolean flag) {
            this.flag = flag;
            return this;
        }

        public CheckResult build() {
            return new CheckResult(score, debugData, flag);
        }
    }

    @Override
    public String toString() {
        return String.format("CheckResult[score=%.2f, flag=%s, debug=%s]",
                score, flag, debugData);
    }
}