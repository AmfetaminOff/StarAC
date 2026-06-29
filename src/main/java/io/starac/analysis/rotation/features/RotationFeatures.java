package io.starac.analysis.rotation.features;

public record RotationFeatures(

        double averageDeltaYaw,
        double averageDeltaPitch,

        double deltaYawStd,
        double deltaPitchStd,

        double averageAccelerationYaw,
        double averageAccelerationPitch,

        double jerk,

        double gcd,

        double entropy,

        double smoothness,

        double snapRate,

        double overshootRate,

        double microCorrectionRate,

        double precision,

        double consistency,

        double noise

) {

    public static final RotationFeatures EMPTY = new RotationFeatures(
            0, 0,
            0, 0,
            0, 0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0
    );

}