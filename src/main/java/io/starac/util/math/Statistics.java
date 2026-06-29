package io.starac.util.math;

import java.util.Arrays;

public final class Statistics {

    private Statistics() {
    }

    public static double mean(double[] values) {
        if (values.length == 0) {
            return 0.0;
        }

        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }

        return sum / values.length;
    }

    public static double variance(double[] values) {
        if (values.length < 2) {
            return 0.0;
        }

        double mean = mean(values);

        double variance = 0.0;
        for (double value : values) {
            double diff = value - mean;
            variance += diff * diff;
        }

        return variance / values.length;
    }

    public static double standardDeviation(double[] values) {
        return Math.sqrt(variance(values));
    }

    public static double min(double[] values) {
        if (values.length == 0) {
            return 0.0;
        }

        double min = values[0];

        for (int i = 1; i < values.length; i++) {
            if (values[i] < min) {
                min = values[i];
            }
        }

        return min;
    }

    public static double max(double[] values) {
        if (values.length == 0) {
            return 0.0;
        }

        double max = values[0];

        for (int i = 1; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }

        return max;
    }

    public static double range(double[] values) {
        return max(values) - min(values);
    }

    public static double median(double[] values) {
        if (values.length == 0) {
            return 0.0;
        }

        double[] copy = values.clone();
        Arrays.sort(copy);

        int middle = copy.length / 2;

        return (copy.length & 1) == 0
                ? (copy[middle - 1] + copy[middle]) / 2.0
                : copy[middle];
    }

    public static double percentile(double[] values, double percentile) {
        if (values.length == 0) {
            return 0.0;
        }

        double[] copy = values.clone();
        Arrays.sort(copy);

        int index = (int) Math.ceil(percentile / 100.0 * copy.length) - 1;
        index = Math.max(0, Math.min(index, copy.length - 1));

        return copy[index];
    }

    public static double entropy(double[] values, int bins) {
        if (values.length == 0 || bins <= 1) {
            return 0.0;
        }

        double min = min(values);
        double max = max(values);

        double range = max - min;

        if (range <= 1E-9) {
            return 0.0;
        }

        int[] histogram = new int[bins];

        for (double value : values) {
            int index = (int) (((value - min) / range) * (bins - 1));
            histogram[index]++;
        }

        double entropy = 0.0;

        for (int count : histogram) {
            if (count == 0) {
                continue;
            }

            double probability = (double) count / values.length;
            entropy -= probability * (Math.log(probability) / Math.log(2));
        }

        return entropy;
    }

    public static double coefficientOfVariation(double[] values) {
        double mean = mean(values);

        if (Math.abs(mean) < 1E-9) {
            return 0.0;
        }

        return standardDeviation(values) / mean;
    }

    public static double correlation(double[] x, double[] y) {
        if (x.length != y.length || x.length < 2) {
            return 0.0;
        }

        double meanX = mean(x);
        double meanY = mean(y);

        double numerator = 0.0;
        double denominatorX = 0.0;
        double denominatorY = 0.0;

        for (int i = 0; i < x.length; i++) {
            double dx = x[i] - meanX;
            double dy = y[i] - meanY;

            numerator += dx * dy;
            denominatorX += dx * dx;
            denominatorY += dy * dy;
        }

        double denominator = Math.sqrt(denominatorX * denominatorY);

        return denominator == 0.0 ? 0.0 : numerator / denominator;
    }
}