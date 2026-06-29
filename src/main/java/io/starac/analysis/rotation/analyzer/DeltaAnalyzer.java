package io.starac.analysis.rotation.analyzer;

import io.starac.analysis.rotation.window.RotationSample;
import io.starac.analysis.rotation.window.RotationWindow;

public final class DeltaAnalyzer {

    public Result analyze(RotationWindow window) {

        if (window.size() < 2)
            return new Result();

        double yawSum = 0;
        double pitchSum = 0;

        for (int i = 0; i < window.size(); i++) {
            RotationSample sample = window.get(i);

            yawSum += sample.getDeltaYaw();
            pitchSum += sample.getDeltaPitch();
        }

        double yawMean = yawSum / window.size();
        double pitchMean = pitchSum / window.size();

        double yawVar = 0;
        double pitchVar = 0;

        for (int i = 0; i < window.size(); i++) {
            RotationSample sample = window.get(i);

            yawVar += Math.pow(sample.getDeltaYaw() - yawMean, 2);
            pitchVar += Math.pow(sample.getDeltaPitch() - pitchMean, 2);
        }

        yawVar /= window.size();
        pitchVar /= window.size();

        return new Result(
                yawMean,
                pitchMean,
                Math.sqrt(yawVar),
                Math.sqrt(pitchVar)
        );
    }

    public record Result(
            double averageYaw,
            double averagePitch,
            double stdYaw,
            double stdPitch
    ) {
        public Result() {
            this(0, 0, 0, 0);
        }
    }
}