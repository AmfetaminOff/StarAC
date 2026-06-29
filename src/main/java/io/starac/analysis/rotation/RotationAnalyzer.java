package io.starac.analysis.rotation;

import io.starac.analysis.rotation.analyzer.*;
import io.starac.analysis.features.RotationFeatures;
import io.starac.analysis.rotation.window.RotationWindow;

public final class RotationAnalyzer {

    private final DeltaAnalyzer deltaAnalyzer = new DeltaAnalyzer();
    private final AccelerationAnalyzer accelerationAnalyzer = new AccelerationAnalyzer();
    private final JerkAnalyzer jerkAnalyzer = new JerkAnalyzer();
    private final GCDAnalyzer gcdAnalyzer = new GCDAnalyzer();
    private final EntropyAnalyzer entropyAnalyzer = new EntropyAnalyzer();
    private final SmoothnessAnalyzer smoothnessAnalyzer = new SmoothnessAnalyzer();
    private final SnapAnalyzer snapAnalyzer = new SnapAnalyzer();
    private final OvershootAnalyzer overshootAnalyzer = new OvershootAnalyzer();
    private final MicroCorrectionAnalyzer microCorrectionAnalyzer = new MicroCorrectionAnalyzer();
    private final PrecisionAnalyzer precisionAnalyzer = new PrecisionAnalyzer();
    private final ConsistencyAnalyzer consistencyAnalyzer = new ConsistencyAnalyzer();
    private final NoiseAnalyzer noiseAnalyzer = new NoiseAnalyzer();

    public RotationFeatures analyze(RotationWindow window) {

        var delta = deltaAnalyzer.analyze(window);
        var accel = accelerationAnalyzer.analyze(window);

        return new RotationFeatures(

                delta.averageYaw(),
                delta.averagePitch(),
                delta.stdYaw(),
                delta.stdPitch(),

                accel.averageYaw(),
                accel.averagePitch(),

                jerkAnalyzer.analyze(window),

                gcdAnalyzer.analyze(window),
                entropyAnalyzer.analyze(window),
                smoothnessAnalyzer.analyze(window),
                consistencyAnalyzer.analyze(window),
                noiseAnalyzer.analyze(window),

                snapAnalyzer.analyze(window),
                overshootAnalyzer.analyze(window),
                microCorrectionAnalyzer.analyze(window),
                precisionAnalyzer.analyze(window)

        );

    }

}
