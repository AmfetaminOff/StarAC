package io.starac.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AiFeatureSet {

    private final List<Double> yawDeltas;
    private final List<Double> pitchDeltas;
    private final List<Double> gcdValues;
    private final double rotationEntropy;
    private final double avgYawSpeed;
    private final double avgPitchSpeed;
    private final double cps;
    private final double clickVariance;
    private final double clickConsistency;
    private final List<Double> reachValues;
    private final double avgReach;
    private final double maxReach;
    private final List<Double> strafeDeltas;
    private final double avgSpeed;
    private final double maxSpeed;
    private final double speedVariance;
    private final double jumpFrequency;
    private final double sprintRatio;
    private final int dataPoints;
    private final long collectionTimeMs;
    private final double tps;

    private AiFeatureSet(Builder b) {
        this.yawDeltas = Collections.unmodifiableList(new ArrayList<>(b.yawDeltas));
        this.pitchDeltas = Collections.unmodifiableList(new ArrayList<>(b.pitchDeltas));
        this.gcdValues = Collections.unmodifiableList(new ArrayList<>(b.gcdValues));
        this.rotationEntropy = b.rotationEntropy;
        this.avgYawSpeed = b.avgYawSpeed;
        this.avgPitchSpeed = b.avgPitchSpeed;
        this.cps = b.cps;
        this.clickVariance = b.clickVariance;
        this.clickConsistency = b.clickConsistency;
        this.reachValues = Collections.unmodifiableList(new ArrayList<>(b.reachValues));
        this.avgReach = b.avgReach;
        this.maxReach = b.maxReach;
        this.strafeDeltas = Collections.unmodifiableList(new ArrayList<>(b.strafeDeltas));
        this.avgSpeed = b.avgSpeed;
        this.maxSpeed = b.maxSpeed;
        this.speedVariance = b.speedVariance;
        this.jumpFrequency = b.jumpFrequency;
        this.sprintRatio = b.sprintRatio;
        this.dataPoints = b.dataPoints;
        this.collectionTimeMs = b.collectionTimeMs;
        this.tps = b.tps;
    }

    public List<Double> getYawDeltas() { return yawDeltas; }
    public List<Double> getPitchDeltas() { return pitchDeltas; }
    public List<Double> getGcdValues() { return gcdValues; }
    public double getRotationEntropy() { return rotationEntropy; }
    public double getAvgYawSpeed() { return avgYawSpeed; }
    public double getAvgPitchSpeed() { return avgPitchSpeed; }
    public double getCps() { return cps; }
    public double getClickVariance() { return clickVariance; }
    public double getClickConsistency() { return clickConsistency; }
    public List<Double> getReachValues() { return reachValues; }
    public double getAvgReach() { return avgReach; }
    public double getMaxReach() { return maxReach; }
    public List<Double> getStrafeDeltas() { return strafeDeltas; }
    public double getAvgSpeed() { return avgSpeed; }
    public double getMaxSpeed() { return maxSpeed; }
    public double getSpeedVariance() { return speedVariance; }
    public double getJumpFrequency() { return jumpFrequency; }
    public double getSprintRatio() { return sprintRatio; }
    public int getDataPoints() { return dataPoints; }
    public long getCollectionTimeMs() { return collectionTimeMs; }
    public double getTps() { return tps; }
    public boolean isValid() {
        return dataPoints >= 5 && tps > 0;
    }

    public int getScalarFeatureCount() {
        return 18;
    }

    public int getTotalFeatureCount() {
        return getScalarFeatureCount()
                + yawDeltas.size()
                + pitchDeltas.size()
                + gcdValues.size()
                + reachValues.size()
                + strafeDeltas.size();
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder(2048);
        sb.append("{");

        sb.append("\"rotation_entropy\":").append(rotationEntropy).append(",");
        sb.append("\"avg_yaw_speed\":").append(avgYawSpeed).append(",");
        sb.append("\"avg_pitch_speed\":").append(avgPitchSpeed).append(",");
        sb.append("\"cps\":").append(cps).append(",");
        sb.append("\"click_variance\":").append(clickVariance).append(",");
        sb.append("\"click_consistency\":").append(clickConsistency).append(",");
        sb.append("\"avg_reach\":").append(avgReach).append(",");
        sb.append("\"max_reach\":").append(maxReach).append(",");
        sb.append("\"avg_speed\":").append(avgSpeed).append(",");
        sb.append("\"max_speed\":").append(maxSpeed).append(",");
        sb.append("\"speed_variance\":").append(speedVariance).append(",");
        sb.append("\"jump_frequency\":").append(jumpFrequency).append(",");
        sb.append("\"sprint_ratio\":").append(sprintRatio).append(",");
        sb.append("\"yaw_deltas\":").append(toJsonArray(yawDeltas)).append(",");
        sb.append("\"pitch_deltas\":").append(toJsonArray(pitchDeltas)).append(",");
        sb.append("\"gcd_values\":").append(toJsonArray(gcdValues)).append(",");
        sb.append("\"reach_values\":").append(toJsonArray(reachValues)).append(",");
        sb.append("\"strafe_deltas\":").append(toJsonArray(strafeDeltas)).append(",");
        sb.append("\"data_points\":").append(dataPoints).append(",");
        sb.append("\"collection_time_ms\":").append(collectionTimeMs).append(",");
        sb.append("\"tps\":").append(tps);

        sb.append("}");
        return sb.toString();
    }

    private String toJsonArray(List<Double> values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            sb.append(String.format("%.6f", values.get(i)));
            if (i < values.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("AiFeatureSet[dataPoints=%d, cps=%.1f, reach=%.2f, entropy=%.2f]",
                dataPoints, cps, avgReach, rotationEntropy);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private List<Double> yawDeltas = new ArrayList<>();
        private List<Double> pitchDeltas = new ArrayList<>();
        private List<Double> gcdValues = new ArrayList<>();
        private double rotationEntropy;
        private double avgYawSpeed;
        private double avgPitchSpeed;
        private double cps;
        private double clickVariance;
        private double clickConsistency;
        private List<Double> reachValues = new ArrayList<>();
        private double avgReach;
        private double maxReach;
        private List<Double> strafeDeltas = new ArrayList<>();
        private double avgSpeed;
        private double maxSpeed;
        private double speedVariance;
        private double jumpFrequency;
        private double sprintRatio;
        private int dataPoints;
        private long collectionTimeMs;
        private double tps;

        public Builder yawDeltas(List<Double> v) {
            yawDeltas = v != null ? new ArrayList<>(v) : new ArrayList<>();
            return this;
        }

        public Builder pitchDeltas(List<Double> v) {
            pitchDeltas = v != null ? new ArrayList<>(v) : new ArrayList<>();
            return this;
        }

        public Builder gcdValues(List<Double> v) {
            gcdValues = v != null ? new ArrayList<>(v) : new ArrayList<>();
            return this;
        }

        public Builder rotationEntropy(double v) { rotationEntropy = v; return this; }
        public Builder avgYawSpeed(double v) { avgYawSpeed = v; return this; }
        public Builder avgPitchSpeed(double v) { avgPitchSpeed = v; return this; }
        public Builder cps(double v) { cps = v; return this; }
        public Builder clickVariance(double v) { clickVariance = v; return this; }
        public Builder clickConsistency(double v) { clickConsistency = v; return this; }

        public Builder reachValues(List<Double> v) {
            reachValues = v != null ? new ArrayList<>(v) : new ArrayList<>();
            return this;
        }

        public Builder avgReach(double v) { avgReach = v; return this; }
        public Builder maxReach(double v) { maxReach = v; return this; }

        public Builder strafeDeltas(List<Double> v) {
            strafeDeltas = v != null ? new ArrayList<>(v) : new ArrayList<>();
            return this;
        }

        public Builder avgSpeed(double v) { avgSpeed = v; return this; }
        public Builder maxSpeed(double v) { maxSpeed = v; return this; }
        public Builder speedVariance(double v) { speedVariance = v; return this; }
        public Builder jumpFrequency(double v) { jumpFrequency = v; return this; }
        public Builder sprintRatio(double v) { sprintRatio = v; return this; }
        public Builder dataPoints(int v) { dataPoints = v; return this; }
        public Builder collectionTimeMs(long v) { collectionTimeMs = v; return this; }
        public Builder tps(double v) { tps = v; return this; }

        public AiFeatureSet build() {
            return new AiFeatureSet(this);
        }
    }
}