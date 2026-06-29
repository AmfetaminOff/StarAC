package io.starac.util.math;

public final class RotationMath {

    private RotationMath() {
    }

    public static float wrapAngle(float angle) {
        angle %= 360.0F;

        if (angle >= 180.0F) {
            angle -= 360.0F;
        }

        if (angle < -180.0F) {
            angle += 360.0F;
        }

        return angle;
    }

    public static float delta(float from, float to) {
        return wrapAngle(to - from);
    }

    public static double sensitivityGcd(float sensitivity) {
        float f = sensitivity * 0.6F + 0.2F;
        return Math.pow(f, 3.0) * 8.0;
    }

    public static double normalizeDelta(double delta, double gcd) {
        if (gcd <= 0.0) {
            return delta;
        }

        return delta / gcd;
    }

    public static boolean isSnap(double delta, double threshold) {
        return Math.abs(delta) >= threshold;
    }

    public static boolean isMicroCorrection(double delta, double threshold) {
        return Math.abs(delta) <= threshold;
    }

}