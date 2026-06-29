package io.starac.ai.feature;

import io.starac.ai.AiFeatureSet;
import io.starac.analysis.features.RotationFeatures;
import io.starac.data.PlayerData;
import io.starac.util.TimeUtil;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public final class FeatureExtractor {

    private FeatureExtractor() {}

    public static AiFeatureSet extract(PlayerData data) {
        if (data == null) return null;

        long startTime = TimeUtil.now();
        AiFeatureSet.Builder b = new AiFeatureSet.Builder();

        List<Double> yaws = data.getYawDeltas() != null ? data.getYawDeltas() : new ArrayList<>();
        List<Double> pitches = data.getPitchDeltas() != null ? data.getPitchDeltas() : new ArrayList<>();
        List<Double> gcds = data.getGcdValues() != null ? data.getGcdValues() : new ArrayList<>();

        b.yawDeltas(yaws);
        b.pitchDeltas(pitches);
        b.gcdValues(gcds);

        RotationFeatures.Result rotResult = RotationFeatures.extract(data);
        if (rotResult != null) {
            b.rotationEntropy(rotResult.entropy());
            b.avgYawSpeed(rotResult.avgYawSpeed());
            b.avgPitchSpeed(rotResult.avgPitchSpeed());
        } else {
            b.rotationEntropy(calculateEntropy(yaws));
            b.avgYawSpeed(calculateAverage(yaws));
            b.avgPitchSpeed(calculateAverage(pitches));
        }

        b.cps(data.getCps());
        b.clickVariance(data.getClickVariance());
        b.clickConsistency(calculateClickConsistency(data));

        List<Double> reaches = data.getReachValues() != null ? data.getReachValues() : new ArrayList<>();
        b.reachValues(reaches);
        b.avgReach(calculateAverage(reaches));
        b.maxReach(calculateMax(reaches));

        List<Double> strafes = data.getStrafeDelta() != null ? data.getStrafeDelta() : new ArrayList<>();
        b.strafeDeltas(strafes);

        List<Double> speeds = data.getSpeedValues() != null ? data.getSpeedValues() : new ArrayList<>();
        b.avgSpeed(calculateAverage(speeds));
        b.maxSpeed(calculateMax(speeds));
        b.speedVariance(calculateVariance(speeds));

        b.jumpFrequency(data.getJumpFrequency());
        b.sprintRatio(calculateSprintRatio(data));

        b.dataPoints(Math.max(yaws.size(), reaches.size()));
        b.collectionTimeMs(TimeUtil.now() - startTime);
        b.tps(Bukkit.getServer().getTPS().length > 0 ? Bukkit.getServer().getTPS()[0] : 20.0);

        return b.build();
    }

    private static double calculateEntropy(List<Double> values) {
        if (values.isEmpty()) return 0.0;

        int bins = 36;
        int[] counts = new int[bins];
        for (double v : values) {
            int bin = (int) ((Math.abs(v) % 360.0) / 10.0);
            bin = Math.min(bin, bins - 1);
            counts[bin]++;
        }

        double entropy = 0.0;
        int total = values.size();
        for (int count : counts) {
            if (count > 0) {
                double p = (double) count / total;
                entropy -= p * Math.log(p);
            }
        }

        return entropy;
    }

    private static double calculateAverage(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        double sum = 0;
        for (double v : values) sum += v;
        return sum / values.size();
    }

    private static double calculateMax(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        double max = Double.MIN_VALUE;
        for (double v : values) {
            if (v > max) max = v;
        }
        return max;
    }

    private static double calculateVariance(List<Double> values) {
        if (values.size() < 2) return 0.0;
        double mean = calculateAverage(values);
        double sumSquaredDiffs = 0.0;
        for (double v : values) {
            double diff = v - mean;
            sumSquaredDiffs += diff * diff;
        }
        return sumSquaredDiffs / values.size();
    }

    private static double calculateClickConsistency(PlayerData data) {
        double variance = data.getClickVariance();
        if (variance < 0.1) return 0.1;
        if (variance > 5.0) return 1.0;
        return Math.min(1.0, variance / 3.0);
    }

    private static double calculateSprintRatio(PlayerData data) {
        int sprintTicks = data.getSprintTicks();
        int totalTicks = data.getTotalTicks();
        if (totalTicks == 0) return 0.0;
        return (double) sprintTicks / totalTicks;
    }
}