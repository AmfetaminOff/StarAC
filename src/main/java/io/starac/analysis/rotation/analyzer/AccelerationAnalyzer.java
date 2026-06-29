package io.starac.analysis.rotation.analyzer;

import io.starac.analysis.rotation.window.RotationSample;
import io.starac.analysis.rotation.window.RotationWindow;

public final class AccelerationAnalyzer {

    public Result analyze(RotationWindow window) {

        if (window.size() < 2)
            return new Result();

        double yaw = 0;
        double pitch = 0;

        for (int i = 0; i < window.size(); i++) {

            RotationSample sample = window.get(i);

            yaw += Math.abs(sample.getAccelerationYaw());
            pitch += Math.abs(sample.getAccelerationPitch());

        }

        return new Result(
                yaw / window.size(),
                pitch / window.size()
        );
    }

    public record Result(
            double averageYaw,
            double averagePitch
    ) {
        public Result() {
            this(0, 0);
        }
    }
}