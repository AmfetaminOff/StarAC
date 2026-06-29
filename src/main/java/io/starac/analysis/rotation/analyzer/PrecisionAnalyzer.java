package io.starac.analysis.rotation.analyzer;

import io.starac.data.PlayerData;
import java.util.List;

public final class PrecisionAnalyzer {

    public record Result(
            double gcdAlignment,
            double subPixelRatio,
            double precisionScore,
            double residualVariance,
            boolean isSuperhuman
    ) {
        public double[] toFeatureArray() {
            return new double[]{gcdAlignment, subPixelRatio, precisionScore, residualVariance};
        }
        public static int featureCount() { return 4; }
    }

    public static Result analyze(PlayerData data) {
        List<Double> yaws = data.getYawDeltas();
        List<Double> gcdValues = data.getGcdValues();

        if (yaws == null || yaws.isEmpty() || gcdValues == null || gcdValues.isEmpty()) {
            return new Result(0.5, 0, 0.5, 0, false);
        }

        double gcd = MathUtil.gcd(gcdValues);
        if (gcd <= 0.0001) {
            return new Result(0.5, 0, 0.5, 0, false);
        }

        int aligned = 0;
        int subPixel = 0;
        double sumResidual = 0;
        double sumResidualSq = 0;
        int validSamples = 0;

        for (double delta : yaws) {
            double abs = Math.abs(delta);
            if (abs < 0.001) continue;

            double remainder = abs % gcd;
            double normalizedRemainder = Math.min(remainder, gcd - remainder);

            validSamples++;
            sumResidual += normalizedRemainder;
            sumResidualSq += normalizedRemainder * normalizedRemainder;

            if (normalizedRemainder < 0.001) {
                aligned++;
            }
            if (abs < gcd * 0.5) {
                subPixel++;
            }
        }

        if (validSamples == 0) {
            return new Result(0.5, 0, 0.5, 0, false);
        }

        double alignmentRatio = (double) aligned / validSamples;
        double subPixelRatio = (double) subPixel / validSamples;
        double meanResidual = sumResidual / validSamples;
        double residualVar = (sumResidualSq / validSamples) - (meanResidual * meanResidual);

        double precisionScore = alignmentRatio * (1.0 - Math.min(1.0, residualVar * 100));

        boolean superhuman = alignmentRatio > 0.95 && residualVar < 0.0001;

        return new Result(alignmentRatio, subPixelRatio, precisionScore, residualVar, superhuman);
    }
}
