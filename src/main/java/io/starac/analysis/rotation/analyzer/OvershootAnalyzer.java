package io.starac.analysis.rotation.analyzer;

import io.starac.data.PlayerData;
import java.util.List;

/**
 * Анализ перелётов (overshoot) при наведении.
 *
 * <p>Человек при быстром повороте к цели почти всегда пролетает мимо
 * и корректируется обратно. Бот либо останавливается идеально точно,
 * либо корректируется с неестественной точностью.
 */
public final class OvershootAnalyzer {

    public record Result(
            int overshootCount,
            double avgCorrectionAngle,
            double correctionRatio,
            double correctionPrecision,
            boolean isTooPerfect
    ) {
        public double[] toFeatureArray() {
            return new double[]{overshootCount, avgCorrectionAngle, correctionRatio, correctionPrecision};
        }
        public static int featureCount() { return 4; }
    }

    public static Result analyze(PlayerData data) {
        List<Double> yaws = data.getYawDeltas();
        if (yaws == null || yaws.size() < 4) {
            return new Result(0, 0, 0, 0.5, false);
        }

        int overshoots = 0;
        double sumCorrection = 0;
        double sumPrecision = 0;

        for (int i = 1; i < yaws.size() - 1; i++) {
            double curr = yaws.get(i);
            double next = yaws.get(i + 1);

            if ((curr > 0 && next < 0) || (curr < 0 && next > 0)) {
                double correctionSize = Math.abs(next);
                double originalSize = Math.abs(curr);

                if (correctionSize < originalSize && correctionSize > 0.5) {
                    overshoots++;
                    sumCorrection += correctionSize;
                    sumPrecision += correctionSize;
                }
            }
        }

        double avgCorrection = overshoots > 0 ? sumCorrection / overshoots : 0;
        double ratio = (double) overshoots / Math.max(1, yaws.size() / 2);
        double precision = overshoots > 0 ? sumPrecision / overshoots : 0.5;

        boolean tooPerfect = overshoots > 0 && precision < 1.0;

        return new Result(overshoots, avgCorrection, ratio, precision, tooPerfect);
    }
}