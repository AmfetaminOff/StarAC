package io.starac.ai.feature;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

public final class FeatureVector {

    private final double[] values;
    private final int size;

    public FeatureVector(double[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Values cannot be null or empty");
        }
        this.values = Arrays.copyOf(values, values.length);
        this.size = values.length;
    }

    public FeatureVector(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        this.values = new double[size];
        this.size = size;
    }

    public int size() {
        return size;
    }

    public double get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return values[index];
    }

    public double[] toArray() {
        return Arrays.copyOf(values, size);
    }

    public FeatureVector add(FeatureVector other) {
        checkSameSize(other);
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = this.values[i] + other.values[i];
        }
        return new FeatureVector(result);
    }

    public FeatureVector subtract(FeatureVector other) {
        checkSameSize(other);
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = this.values[i] - other.values[i];
        }
        return new FeatureVector(result);
    }

    public FeatureVector multiply(double scalar) {
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = this.values[i] * scalar;
        }
        return new FeatureVector(result);
    }

    public FeatureVector multiply(FeatureVector other) {
        checkSameSize(other);
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = this.values[i] * other.values[i];
        }
        return new FeatureVector(result);
    }

    public FeatureVector divide(double scalar) {
        if (scalar == 0) {
            throw new ArithmeticException("Division by zero");
        }
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = this.values[i] / scalar;
        }
        return new FeatureVector(result);
    }

    public double dot(FeatureVector other) {
        checkSameSize(other);
        double sum = 0.0;
        for (int i = 0; i < size; i++) {
            sum += this.values[i] * other.values[i];
        }
        return sum;
    }

    public double norm() {
        double sum = 0.0;
        for (double v : values) {
            sum += v * v;
        }
        return Math.sqrt(sum);
    }

    public FeatureVector normalize() {
        double n = norm();
        if (n == 0) {
            return new FeatureVector(size);
        }
        return divide(n);
    }

    public double cosineSimilarity(FeatureVector other) {
        checkSameSize(other);
        double dotProduct = this.dot(other);
        double normA = this.norm();
        double normB = other.norm();

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (normA * normB);
    }

    public double euclideanDistance(FeatureVector other) {
        checkSameSize(other);
        double sum = 0.0;
        for (int i = 0; i < size; i++) {
            double diff = this.values[i] - other.values[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    public double mean() {
        double sum = 0.0;
        for (double v : values) {
            sum += v;
        }
        return sum / size;
    }

    public double variance() {
        double mean = mean();
        double sumSquared = 0.0;
        for (double v : values) {
            double diff = v - mean;
            sumSquared += diff * diff;
        }
        return sumSquared / size;
    }

    public double stdDev() {
        return Math.sqrt(variance());
    }

    public double min() {
        double min = Double.MAX_VALUE;
        for (double v : values) {
            if (v < min) min = v;
        }
        return min;
    }

    public double max() {
        double max = Double.MIN_VALUE;
        for (double v : values) {
            if (v > max) max = v;
        }
        return max;
    }

    public double sum() {
        double sum = 0.0;
        for (double v : values) {
            sum += v;
        }
        return sum;
    }

    public FeatureVector normalizeMinMax(double min, double max) {
        double range = max - min;
        if (range == 0) {
            return new FeatureVector(size);
        }
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = (values[i] - min) / range;
        }
        return new FeatureVector(result);
    }

    public FeatureVector normalizeZScore(double mean, double std) {
        if (std == 0) {
            return new FeatureVector(size);
        }
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = (values[i] - mean) / std;
        }
        return new FeatureVector(result);
    }

    public FeatureVector map(DoubleUnaryOperator function) {
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = function.applyAsDouble(values[i]);
        }
        return new FeatureVector(result);
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(String.format("%.6f", values[i]));
            if (i < size - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static FeatureVector fromJson(String json) {
        String trimmed = json.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            throw new IllegalArgumentException("Invalid JSON array: " + json);
        }

        String content = trimmed.substring(1, trimmed.length() - 1).trim();
        if (content.isEmpty()) {
            throw new IllegalArgumentException("Empty JSON array");
        }

        String[] parts = content.split(",");
        double[] values = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            values[i] = Double.parseDouble(parts[i].trim());
        }

        return new FeatureVector(values);
    }

    private void checkSameSize(FeatureVector other) {
        if (other == null) {
            throw new IllegalArgumentException("Other vector cannot be null");
        }
        if (this.size != other.size) {
            throw new IllegalArgumentException("Vector sizes do not match: "
                    + this.size + " vs " + other.size);
        }
    }

    @Override
    public String toString() {
        return "FeatureVector[size=" + size + ", values=" + Arrays.toString(values) + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FeatureVector other)) return false;
        return Arrays.equals(this.values, other.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    public static Builder builder(int size) {
        return new Builder(size);
    }

    public static final class Builder {
        private final double[] values;
        private int index = 0;

        public Builder(int size) {
            this.values = new double[size];
        }

        public Builder set(int index, double value) {
            if (index < 0 || index >= values.length) {
                throw new IndexOutOfBoundsException("Index: " + index);
            }
            values[index] = value;
            return this;
        }

        public Builder add(double value) {
            if (index >= values.length) {
                throw new IllegalStateException("Builder is full");
            }
            values[index++] = value;
            return this;
        }

        public Builder fill(double value) {
            Arrays.fill(values, value);
            return this;
        }

        public FeatureVector build() {
            return new FeatureVector(values);
        }
    }
}