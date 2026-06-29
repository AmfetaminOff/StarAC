package io.starac.analysis.features;

import io.starac.data.PlayerData;

import java.util.List;

public final class RotationFeatures {

    private RotationFeatures() {} // utility class

    public record Result(
            double entropy,
            double avgYawSpeed,
            double avgPitchSpeed,
            double yawVariance,
            double pitchVariance,
            double gcd,
            double smoothness,
            double accelerationVariance,
            boolean isSuspicious
    ) {

        public double[] toFeatureArray() {
            return new double[]{
                    entropy,
                    avgYawSpeed,
                    avgPitchSpeed,
                    yawVariance,
                    pitchVariance,
                    gcd,
                    smoothness,
                    accelerationVariance
            };
        }

        public static int featureCount() {
            return 8;
        }
    }

    public static Result extract(PlayerData data) {
        if (data == null) return null;

        List<Double> yawDeltas = data.getYawDeltas();
        List<Double> pitchDeltas = data.getPitchDeltas();
        List<Double> gcdValues = data.getGcdValues();

        if (yawDeltas.size() < 5 || pitchDeltas.size() < 5) {
            return null;
        }

        double entropy = MathUtil.entropy(yawDeltas, 36);
        double avgYawSpeed = MathUtil.mean(yawDeltas);
        double avgPitchSpeed = MathUtil.mean(pitchDeltas);
        double yawVariance = MathUtil.variance(yawDeltas);
        double pitchVariance = MathUtil.variance(pitchDeltas);
        double gcd = gcdValues.isEmpty() ? 0.0 : MathUtil.gcd(gcdValues);
        double smoothness = calculateSmoothness(yawDeltas, pitchDeltas);
        double accelVariance = calculateAccelerationVariance(yawDeltas);
        boolean suspicious = isSuspiciousPattern(entropy, gcd, smoothness, accelVariance);

        return new Result(
                entropy,
                avgYawSpeed,
                avgPitchSpeed,
                yawVariance,
                pitchVariance,
                gcd,
                smoothness,
                accelVariance,
                suspicious
        );
    }

    private static double calculateSmoothness(List<Double> yawDeltas, List<Double> pitchDeltas) {
        if (yawDeltas.size() < 3) return 0.5;

        double sumAbsDelta = 0.0;
        double sumSquaredDelta = 0.0;

        for (double delta : yawDeltas) {
            sumAbsDelta += Math.abs(delta);
            sumSquaredDelta += delta * delta;
        }

        double meanAbs = sumAbsDelta / yawDeltas.size();
        double rms = Math.sqrt(sumSquaredDelta / yawDeltas.size());

        if (rms == 0) return 0.5;

        double ratio = meanAbs / rms;

        return MathUtil.clamp(ratio, 0.0, 1.0);
    }

    private static double calculateAccelerationVariance(List<Double> yawDeltas) {
        if (yawDeltas.size() < 3) return 0.0;

        double[] accelerations = new double[yawDeltas.size() - 1];
        for (int i = 1; i < yawDeltas.size(); i++) {
            accelerations[i - 1] = yawDeltas.get(i) - yawDeltas.get(i - 1);
        }

        java.util.List<Double> accelList = new java.util.ArrayList<>(accelerations.length);
        for (double a : accelerations) accelList.add(a);

        return MathUtil.variance(accelList);
    }

    private static boolean isSuspiciousPattern(double entropy, double gcd,
                                               double smoothness, double accelVariance) {
        int suspicionScore = 0;

        if (entropy < 1.5) suspicionScore++;
        if (gcd > 0 && gcd < 0.005) suspicionScore++;
        if (smoothness < 0.15 || smoothness > 0.95) suspicionScore++;
        if (accelVariance < 0.01) suspicionScore++;

        return suspicionScore >= 2;
    }

    public static boolean isQuickSuspicious(PlayerData data) {
        Result result = extract(data);
        return result != null && result.isSuspicious();
    }
}