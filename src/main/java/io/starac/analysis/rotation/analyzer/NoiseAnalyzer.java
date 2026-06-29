package io.starac.analysis.rotation.analyzer;

import io.starac.analysis.rotation.window.RotationSample;
import io.starac.analysis.rotation.window.RotationWindow;

public final class NoiseAnalyzer {

    public double analyze(RotationWindow window) {

        if (window.size() < 5)
            return 0.0;

        double noise = 0.0;

        for (int i = 2; i < window.size() - 2; i++) {

            RotationSample previous = window.get(i - 1);
            RotationSample current = window.get(i);
            RotationSample next = window.get(i + 1);

            double predictedYaw =
                    (previous.getDeltaYaw() + next.getDeltaYaw()) * 0.5;

            double predictedPitch =
                    (previous.getDeltaPitch() + next.getDeltaPitch()) * 0.5;

            noise += Math.abs(current.getDeltaYaw() - predictedYaw);
            noise += Math.abs(current.getDeltaPitch() - predictedPitch);

        }

        return noise / ((window.size() - 4) * 2.0);

    }

}