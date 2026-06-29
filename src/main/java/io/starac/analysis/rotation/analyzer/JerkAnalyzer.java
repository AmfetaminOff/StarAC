package io.starac.analysis.rotation.analyzer;

import io.starac.analysis.rotation.window.RotationSample;
import io.starac.analysis.rotation.window.RotationWindow;

public final class JerkAnalyzer {

    public double analyze(RotationWindow window) {

        if (window.size() < 2)
            return 0;

        double jerk = 0;

        for (int i = 1; i < window.size(); i++) {

            RotationSample previous = window.get(i - 1);
            RotationSample current = window.get(i);

            jerk += Math.abs(
                    current.getAccelerationYaw() - previous.getAccelerationYaw());

            jerk += Math.abs(
                    current.getAccelerationPitch() - previous.getAccelerationPitch());

        }

        return jerk / ((window.size() - 1) * 2D);

    }

}