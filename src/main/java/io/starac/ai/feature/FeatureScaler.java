package io.starac.ai.feature;

import io.starac.ai.AiFeatureSet;

import java.util.HashMap;
import java.util.Map;

public final class FeatureScaler {

    public enum ScalingMethod {
        MIN_MAX,
        Z_SCORE
    }

    private final ScalingMethod method;
    private final Map<String, Double> params1;
    private final Map<String, Double> params2;

    private FeatureScaler(ScalingMethod method, Map<String, Double> params1, Map<String, Double> params2) {
        this.method = method;
        this.params1 = params1;
        this.params2 = params2;
    }

    public AiFeatureSet scale(AiFeatureSet features) {
        AiFeatureSet.Builder b = new AiFeatureSet.Builder();

        b.rotationEntropy(scale("rotation_entropy", features.getRotationEntropy()));
        b.avgYawSpeed(scale("avg_yaw_speed", features.getAvgYawSpeed()));
        b.avgPitchSpeed(scale("avg_pitch_speed", features.getAvgPitchSpeed()));
        b.cps(scale("cps", features.getCps()));
        b.clickVariance(scale("click_variance", features.getClickVariance()));
        b.clickConsistency(scale("click_consistency", features.getClickConsistency()));
        b.avgReach(scale("avg_reach", features.getAvgReach()));
        b.maxReach(scale("max_reach", features.getMaxReach()));
        b.avgSpeed(scale("avg_speed", features.getAvgSpeed()));
        b.maxSpeed(scale("max_speed", features.getMaxSpeed()));
        b.speedVariance(scale("speed_variance", features.getSpeedVariance()));
        b.jumpFrequency(scale("jump_frequency", features.getJumpFrequency()));
        b.sprintRatio(scale("sprint_ratio", features.getSprintRatio()));
        b.yawDeltas(features.getYawDeltas());
        b.pitchDeltas(features.getPitchDeltas());
        b.gcdValues(features.getGcdValues());
        b.reachValues(features.getReachValues());
        b.strafeDeltas(features.getStrafeDeltas());
        b.dataPoints(features.getDataPoints());
        b.collectionTimeMs(features.getCollectionTimeMs());
        b.tps(features.getTps());

        return b.build();
    }

    private double scale(String featureName, double value) {
        Double p1 = params1.get(featureName);
        Double p2 = params2.get(featureName);

        if (p1 == null || p2 == null) {
            return value;
        }

        return switch (method) {
            case MIN_MAX -> {
                double range = p2 - p1;
                yield range > 0 ? (value - p1) / range : 0.0;
            }
            case Z_SCORE -> {
                yield p2 > 0 ? (value - p1) / p2 : 0.0;
            }
        };
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private ScalingMethod method = ScalingMethod.MIN_MAX;
        private final Map<String, Double> params1 = new HashMap<>();
        private final Map<String, Double> params2 = new HashMap<>();

        public Builder method(ScalingMethod method) {
            this.method = method;
            return this;
        }

        public Builder minMax(String featureName, double min, double max) {
            params1.put(featureName, min);
            params2.put(featureName, max);
            return this;
        }

        public Builder zScore(String featureName, double mean, double std) {
            params1.put(featureName, mean);
            params2.put(featureName, std);
            return this;
        }

        public Builder defaults() {
            minMax("rotation_entropy", 0.0, 3.5);
            minMax("avg_yaw_speed", 0.0, 50.0);
            minMax("avg_pitch_speed", 0.0, 30.0);
            minMax("cps", 0.0, 25.0);
            minMax("click_variance", 0.0, 10.0);
            minMax("click_consistency", 0.0, 1.0);
            minMax("avg_reach", 2.0, 5.0);
            minMax("max_reach", 2.0, 6.0);
            minMax("avg_speed", 0.0, 2.0);
            minMax("max_speed", 0.0, 3.0);
            minMax("speed_variance", 0.0, 1.0);
            minMax("jump_frequency", 0.0, 10.0);
            minMax("sprint_ratio", 0.0, 1.0);
            return this;
        }

        public FeatureScaler build() {
            return new FeatureScaler(method, params1, params2);
        }
    }
}