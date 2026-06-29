package io.starac.analysis.rotation.analyzer;

import io.starac.data.PlayerData;
import java.util.List;

public final class SnapAnalyzer {

    public record Result(
            int snapCount,
            double maxSnapAngle,
            double avgSnapAngle,
            double snapRatio,
            boolean hasSuspiciousSnaps
    ) {
        public double[] toFeatureArray() {
            return new double[]{snapCount, maxSnapAngle, avgSnapAngle, snapRatio};
        }
        public static int featureCount() { return 4; }
    }

    private static final double SNAP_THRESHOLD = 30.0;

    public static Result analyze(PlayerData data) {
        List<Double> yaws = data.getYawDeltas();
        if (yaws == null || yaws.size() < 3) {
            return new Result(0, 0, 0, 0, false);
        }

        int snaps = 0;
        double maxSnap = 0;
        double sumSnap = 0;

        for (int i = 1; i < yaws.size() - 1; i++) {
            double prev = Math.abs(yaws.get(i - 1));
            double curr = Math.abs(yaws.get(i));
            double next = Math.abs(yaws.get(i + 1));

            if (curr > SNAP_THRESHOLD && prev < 5.0 && next < 5.0) {
                snaps++;
                sumSnap += curr;
                if (curr > maxSnap) maxSnap = curr;
            }
        }

        double avgSnap = snaps > 0 ? sumSnap / snaps : 0;
        double ratio = (double) snaps / yaws.size();

        return new Result(snaps, maxSnap, avgSnap, ratio, snaps >= 2);
    }
}