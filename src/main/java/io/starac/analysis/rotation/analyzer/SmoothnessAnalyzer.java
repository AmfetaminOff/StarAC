package io.starac.analysis.rotation.analyzer;

import io.starac.analysis.rotation.window.RotationSample;
import io.starac.analysis.rotation.window.RotationWindow;

public final class SmoothnessAnalyzer {

    public double analyze(RotationWindow window) {

        if (window.size() < 3)
            return 0;

        double smooth = 0;

        for (int i = 1; i < window.size() - 1; i++) {

            RotationSample prev = window.get(i - 1);
            RotationSample curr = window.get(i);
            RotationSample next = window.get(i + 1);

            double expectedYaw = (prev.getDeltaYaw() + next.getDeltaYaw()) / 2D;
            double expectedPitch = (prev.getDeltaPitch() + next.getDeltaPitch()) / 2D;

            smooth += Math.abs(curr.getDeltaYaw() - expectedYaw);
            smooth += Math.abs(curr.getDeltaPitch() - expectedPitch);

        }

        return 1D / (1D + smooth);

    }

}