package io.starac.util.math;

public final class GCD {

    private static final double EPSILON = 1E-4;

    private GCD() {
    }

    public static double compute(double a, double b) {
        a = Math.abs(a);
        b = Math.abs(b);

        if (a < EPSILON) {
            return b;
        }

        if (b < EPSILON) {
            return a;
        }

        while (b > EPSILON) {
            double temp = b;
            b = a % b;
            a = temp;
        }

        return a;
    }

    public static double compute(double[] values) {
        if (values.length == 0) {
            return 0.0;
        }

        double gcd = Math.abs(values[0]);

        for (int i = 1; i < values.length; i++) {
            gcd = compute(gcd, Math.abs(values[i]));

            if (gcd < EPSILON) {
                return gcd;
            }
        }

        return gcd;
    }

    public static double compute(float[] values) {
        if (values.length == 0) {
            return 0.0;
        }

        double gcd = Math.abs(values[0]);

        for (int i = 1; i < values.length; i++) {
            gcd = compute(gcd, Math.abs(values[i]));

            if (gcd < EPSILON) {
                return gcd;
            }
        }

        return gcd;
    }

}