package io.starac.analysis.rotation.analyzer;

import io.starac.data.PlayerData;
import java.util.ArrayList;
import java.util.List;

public final class ConsistencyAnalyzer {

    public record Result(
            double interWindowSimilarity,
            double patternRepetitionRate,
            double entropyStability,
            double cvOfMeans,
            boolean isRepetitive
    ) {
        public double[] toFeatureArray() {
            return new double[]{interWindowSimilarity, patternRepetitionRate, entropyStability, cvOfMeans};
        }
        public static int featureCount() { return 4; }
    }

    private static final int WINDOW_SIZE = 10;

    public static Result analyze(PlayerData data) {
        List<Double> yaws = data.getYawDeltas();
        if (yaws == null || yaws.size() < WINDOW_SIZE * 2) {
            return new Result(0.5, 0, 0.5, 0.5, false);
        }

        List<List<Double>> windows = new ArrayList<>();
        for (int i = 0; i + WINDOW_SIZE <= yaws.size(); i += WINDOW_SIZE) {
            windows.add(yaws.subList(i, i + WINDOW_SIZE));
        }

        if (windows.size() < 2) {
            return new Result(0.5, 0, 0.5, 0.5, false);
        }

        List<Double> windowMeans = new ArrayList<>();
        List<Double> windowVars = new ArrayList<>();
        List<Double> windowEntropies = new ArrayList<>();

        for (List<Double> window : windows) {
            windowMeans.add(MathUtil.mean(window));
            windowVars.add(MathUtil.variance(window));
            windowEntropies.add(MathUtil.entropy(window, 18));
        }

        double cvMeans = MathUtil.coefficientOfVariation(windowMeans);
        double entropyVar = MathUtil.variance(windowEntropies);
        double entropyStability = 1.0 - Math.min(1.0, entropyVar);
        double similaritySum = 0;
        int comparisons = 0;
        for (int i = 0; i < windows.size() - 1; i++) {
            double sim = windowSimilarity(windows.get(i), windows.get(i + 1));
            similaritySum += sim;
            comparisons++;
        }

        double avgSimilarity = comparisons > 0 ? similaritySum / comparisons : 0.5;

        int repetitions = 0;
        for (int i = 0; i < windows.size() - 1; i++) {
            if (windowSimilarity(windows.get(i), windows.get(i + 1)) > 0.8) {
                repetitions++;
            }
        }
        double repetitionRate = (double) repetitions / Math.max(1, windows.size() - 1);

        boolean repetitive = avgSimilarity > 0.7 && cvMeans < 0.3;

        return new Result(avgSimilarity, repetitionRate, entropyStability, cvMeans, repetitive);
    }

    private static double windowSimilarity(List<Double> a, List<Double> b) {
        double meanA = MathUtil.mean(a);
        double meanB = MathUtil.mean(b);
        double varA = MathUtil.variance(a);
        double varB = MathUtil.variance(b);
        double meanDiff = Math.abs(meanA - meanB);
        double varDiff = Math.abs(varA - varB);
        double score = 1.0 - Math.min(1.0, (meanDiff + varDiff) / 20.0);
        return Math.max(0, score);
    }
}
