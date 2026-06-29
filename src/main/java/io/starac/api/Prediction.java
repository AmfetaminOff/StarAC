package io.starac.api;

public record Prediction(

        double score,
        String label,
        long timestamp

) {}