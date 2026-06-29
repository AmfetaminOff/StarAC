package io.starac.analysis.rotation.analyzer;

import io.starac.data.PlayerData;
import java.util.List;

public final class MicroCorrectionAnalyzer {

    public record Result(
            double microVariance,
            double microFrequency,
            double idleRatio,
            double microRegularity,
            boolean isDeadStill
    ) {
        public double[] toFeatureArray() {
            return new double[]{microVariance, microFrequency, idleRatio, microRegularity};
        }
        public static int featureCount() { return 4; }
    }

    private static final double MICRO_THRESHOLD = 2.0;

    public static Result analyze(PlayerData data) {
        List<Double> yaws = data.getYawDeltas();
        if (yaws == null || yaws.isEmpty()) {
            return new Result(0, 0, 0, 0, false);
        }

        int idleTicks = 0;
        int microCorrections = 0;
        double sumMicro = 0;
        double sumMicroSq = 0;

        for (double delta : yaws) {
            double abs = Math.abs(delta);
            if (abs < 0.01) {
                idleTicks++;
            } else if (abs < MICRO_THRESHOLD) {
                microCorrections++;
                sumMicro += abs;
                sumMicroSq += abs * abs;
            }
        }

        int total = yaws.size();
        double idleRatio = (double) idleTicks / total;
        double microFreq = (double) microCorrections / total;

        double mean = microCorrections > 0 ? sumMicro / microCorrections : 0;
        double variance = microCorrections > 0
                ? (sumMicroSq / microCorrections) - (mean * mean)
                : 0;

        double regularity = microFreq > 0.1 && variance < 0.1 ? 1.0 : 0.0;

        boolean deadStill = idleRatio > 0.8;

        return new Result(variance, microFreq, idleRatio, regularity, deadStill);
    }
}
