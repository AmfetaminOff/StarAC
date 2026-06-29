package io.starac.analysis.rotation.analyzer;

import io.starac.analysis.rotation.window.RotationSample;
import io.starac.analysis.rotation.window.RotationWindow;

import java.util.HashMap;
import java.util.Map;

public final class EntropyAnalyzer {

    public double analyze(RotationWindow window) {

        if (window.size() < 5)
            return 0;

        Map<Integer, Integer> bins = new HashMap<>();

        for (int i = 0; i < window.size(); i++) {

            RotationSample sample = window.get(i);

            int bucket = (int) (sample.getDeltaYaw() * 10);

            bins.merge(bucket, 1, Integer::sum);

        }

        double entropy = 0;

        for (int count : bins.values()) {

            double p = (double) count / window.size();

            entropy -= p * (Math.log(p) / Math.log(2));

        }

        return entropy;

    }

}