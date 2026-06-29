package io.starac.analysis;

import io.starac.data.PlayerData;
import java.util.List;

public final class GcdAnalysis {

    public record Result(
            double gcd,
            boolean isValid,
            double confidence,
            String verdict
    ) {

        public boolean isSuspicious() {
            return isValid && gcd < 0.005 && confidence > 0.7;
        }

        public boolean isInvalid() {
            return !isValid;
        }
    }

    public static Result analyze(PlayerData data) {
        if (data == null) {
            return new Result(0.0, false, 0.0, "No data");
        }

        List<Double> yawDeltas = data.getYawDeltas();
        List<Double> pitchDeltas = data.getPitchDeltas();

        if (yawDeltas.size() < 10 || pitchDeltas.size() < 10) {
            return new Result(0.0, false, 0.0, "Insufficient data");
        }

        double yawGcd = MathUtil.gcd(yawDeltas);
        double pitchGcd = MathUtil.gcd(pitchDeltas);

        boolean yawValid = yawGcd > 0.001 && yawGcd < 1.0;
        boolean pitchValid = pitchGcd > 0.001 && pitchGcd < 1.0;

        if (!yawValid && !pitchValid) {
            return new Result(0.0, false, 0.0, "No valid GCD found");
        }

        double gcd = Math.min(
                yawValid ? yawGcd : Double.MAX_VALUE,
                pitchValid ? pitchGcd : Double.MAX_VALUE
        );

        double confidence = calculateConfidence(yawDeltas, pitchDeltas, gcd);

        String verdict = determineVerdict(gcd, confidence);

        return new Result(gcd, true, confidence, verdict);
    }

    private static double calculateConfidence(List<Double> yawDeltas, List<Double> pitchDeltas, double gcd) {
        if (gcd <= 0) return 0.0;

        int totalChecks = 0;
        int validChecks = 0;

        for (double delta : yawDeltas) {
            if (Math.abs(delta) < 0.001) continue;
            double remainder = delta % gcd;
            double normalizedRemainder = Math.min(remainder, gcd - remainder);

            totalChecks++;
            if (normalizedRemainder < 0.001) {
                validChecks++;
            }
        }

        for (double delta : pitchDeltas) {
            if (Math.abs(delta) < 0.001) continue;
            double remainder = delta % gcd;
            double normalizedRemainder = Math.min(remainder, gcd - remainder);

            totalChecks++;
            if (normalizedRemainder < 0.001) {
                validChecks++;
            }
        }

        if (totalChecks == 0) return 0.0;
        return (double) validChecks / totalChecks;
    }

    private static String determineVerdict(double gcd, double confidence) {
        if (confidence < 0.5) {
            return "Inconsistent GCD";
        }

        if (gcd < 0.005) {
            return "Suspiciously low GCD (possible Aimbot)";
        }

        if (gcd < 0.01) {
            return "Low GCD (high sensitivity)";
        }

        if (gcd > 0.1) {
            return "High GCD (low sensitivity)";
        }

        return "Normal GCD";
    }

    public static boolean isAimbotSuspicious(PlayerData data) {
        Result result = analyze(data);
        return result.isSuspicious();
    }

    public static double estimateSensitivity(double gcd) {
        return gcd / 0.15;
    }

    public static boolean isVanillaSensitivity(double gcd) {
        double sensitivity = estimateSensitivity(gcd);
        return sensitivity >= 0.1 && sensitivity <= 2.0;
    }
}