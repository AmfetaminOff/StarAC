package io.starac.analysis.rotation.analyzer;

import io.starac.analysis.rotation.window.RotationSample;
import io.starac.analysis.rotation.window.RotationWindow;

public final class GCDAnalyzer {

    public double analyze(RotationWindow window) {

        if (window.size() < 2)
            return 0;

        double gcd = 0;

        for (int i = 1; i < window.size(); i++) {

            RotationSample sample = window.get(i);

            double delta = Math.abs(sample.getDeltaYaw());

            if (delta < 1E-4)
                continue;

            if (gcd == 0)
                gcd = delta;
            else
                gcd = gcd(gcd, delta);

        }

        return gcd;

    }

    private double gcd(double a, double b) {

        while (Math.abs(b) > 1E-4) {

            double t = b;
            b = a % b;
            a = t;

        }

        return Math.abs(a);

    }

}