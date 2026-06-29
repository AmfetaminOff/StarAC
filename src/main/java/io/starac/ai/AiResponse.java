package io.starac.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class AiResponse {

    private final String playerId;
    private final AiVerdict verdict;
    private final double confidence;
    private final Map<String, Double> featureImportance;
    private final List<String> explanations;
    private final String modelVersion;
    private final long inferenceTimeMs;
    private final Map<String, String> metadata;

    private AiResponse(Builder b) {
        this.playerId = b.playerId;
        this.verdict = b.verdict;
        this.confidence = b.confidence;
        this.featureImportance = Collections.unmodifiableMap(new HashMap<>(b.featureImportance));
        this.explanations = Collections.unmodifiableList(new ArrayList<>(b.explanations));
        this.modelVersion = b.modelVersion;
        this.inferenceTimeMs = b.inferenceTimeMs;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(b.metadata));
    }

    public String getPlayerId() { return playerId; }
    public AiVerdict getVerdict() { return verdict; }
    public double getConfidence() { return confidence; }
    public Map<String, Double> getFeatureImportance() { return featureImportance; }
    public List<String> getExplanations() { return explanations; }
    public String getModelVersion() { return modelVersion; }
    public long getInferenceTimeMs() { return inferenceTimeMs; }
    public Map<String, String> getMetadata() { return metadata; }

    public List<Map.Entry<String, Double>> getTopFeatures(int n) {
        return featureImportance.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    public boolean isValid() {
        return verdict != null && verdict != AiVerdict.ERROR && confidence >= 0.0;
    }

    public String toLogString() {
        return String.format("[AI] %s | %s (%.0f%%) | model=%s | %dms",
                playerId, verdict.getFormatted(), confidence * 100,
                modelVersion, inferenceTimeMs);
    }

    public String toStaffString() {
        StringBuilder sb = new StringBuilder();
        sb.append(verdict.getColor()).append("[AI] §f").append(playerId);
        sb.append(" §7| ").append(verdict.getFormattedWithConfidence(confidence));

        if (!explanations.isEmpty()) {
            sb.append("\n§7Причины:");
            for (int i = 0; i < Math.min(3, explanations.size()); i++) {
                sb.append("\n  §f• ").append(explanations.get(i));
            }
        }

        List<Map.Entry<String, Double>> top = getTopFeatures(3);
        if (!top.isEmpty()) {
            sb.append("\n§7Ключевые фичи:");
            for (Map.Entry<String, Double> e : top) {
                sb.append("\n  §f• ").append(e.getKey())
                        .append(": §e").append(String.format("%.3f", e.getValue()));
            }
        }

        return sb.toString();
    }

    public static AiResponse fromJson(String json) {
        try {
            Builder b = new Builder();

            b.playerId(extractString(json, "player_id"));
            b.verdict(AiVerdict.fromString(extractString(json, "verdict")));
            b.confidence(extractDouble(json, "confidence"));
            b.modelVersion(extractString(json, "model_version"));
            b.inferenceTimeMs(extractLong(json, "inference_time_ms"));

            String fiJson = extractObject(json, "feature_importance");
            if (fiJson != null) {
                Map<String, Double> fi = parseDoubleMap(fiJson);
                b.featureImportance(fi);
            }

            String explJson = extractArray(json, "explanations");
            if (explJson != null) {
                List<String> expls = parseStringArray(explJson);
                b.explanations(expls);
            }

            String metaJson = extractObject(json, "metadata");
            if (metaJson != null) {
                Map<String, String> meta = parseStringMap(metaJson);
                b.metadata(meta);
            }

            return b.build();

        } catch (Exception e) {
            return new Builder()
                    .verdict(AiVerdict.ERROR)
                    .explanations(Collections.singletonList("Ошибка парсинга: " + e.getMessage()))
                    .build();
        }
    }

    private static String extractString(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return null;
        int colon = json.indexOf(":", idx);
        int quote1 = json.indexOf("\"", colon);
        if (quote1 < 0) return null;
        int quote2 = json.indexOf("\"", quote1 + 1);
        if (quote2 < 0) return null;
        return json.substring(quote1 + 1, quote2);
    }

    private static double extractDouble(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return 0.0;
        int colon = json.indexOf(":", idx);
        int start = colon + 1;
        while (start < json.length() && !Character.isDigit(json.charAt(start))
                && json.charAt(start) != '-' && json.charAt(start) != '.') {
            start++;
        }
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end))
                || json.charAt(end) == '.' || json.charAt(end) == '-' || json.charAt(end) == 'e')) {
            end++;
        }
        if (start >= end) return 0.0;
        return Double.parseDouble(json.substring(start, end));
    }

    private static long extractLong(String json, String key) {
        return (long) extractDouble(json, key);
    }

    private static String extractObject(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return null;
        int colon = json.indexOf(":", idx);
        int brace1 = json.indexOf("{", colon);
        if (brace1 < 0) return null;
        int brace2 = findMatchingBrace(json, brace1);
        if (brace2 < 0) return null;
        return json.substring(brace1, brace2 + 1);
    }

    private static String extractArray(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return null;
        int colon = json.indexOf(":", idx);
        int bracket1 = json.indexOf("[", colon);
        if (bracket1 < 0) return null;
        int bracket2 = findMatchingBracket(json, bracket1);
        if (bracket2 < 0) return null;
        return json.substring(bracket1, bracket2 + 1);
    }

    private static int findMatchingBrace(String json, int openPos) {
        int depth = 1;
        for (int i = openPos + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private static int findMatchingBracket(String json, int openPos) {
        int depth = 1;
        for (int i = openPos + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private static Map<String, Double> parseDoubleMap(String json) {
        Map<String, Double> map = new HashMap<>();
        String content = json.substring(1, json.length() - 1);
        String[] pairs = content.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                String key = kv[0].trim().replace("\"", "");
                try {
                    double value = Double.parseDouble(kv[1].trim());
                    map.put(key, value);
                } catch (NumberFormatException ignored) {}
            }
        }
        return map;
    }

    private static Map<String, String> parseStringMap(String json) {
        Map<String, String> map = new HashMap<>();
        String content = json.substring(1, json.length() - 1);
        String[] pairs = content.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                String key = kv[0].trim().replace("\"", "");
                String value = kv[1].trim().replace("\"", "");
                map.put(key, value);
            }
        }
        return map;
    }

    private static List<String> parseStringArray(String json) {
        List<String> list = new ArrayList<>();
        String content = json.substring(1, json.length() - 1);
        String[] items = content.split(",");
        for (String item : items) {
            String trimmed = item.trim().replace("\"", "");
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String playerId;
        private AiVerdict verdict = AiVerdict.UNKNOWN;
        private double confidence = 0.0;
        private Map<String, Double> featureImportance = new HashMap<>();
        private List<String> explanations = new ArrayList<>();
        private String modelVersion = "unknown";
        private long inferenceTimeMs = 0;
        private Map<String, String> metadata = new HashMap<>();

        public Builder playerId(String v) { playerId = v; return this; }
        public Builder verdict(AiVerdict v) { verdict = v != null ? v : AiVerdict.UNKNOWN; return this; }
        public Builder confidence(double v) { confidence = Math.max(0.0, Math.min(1.0, v)); return this; }
        public Builder featureImportance(Map<String, Double> v) {
            featureImportance = v != null ? new HashMap<>(v) : new HashMap<>();
            return this;
        }
        public Builder explanations(List<String> v) {
            explanations = v != null ? new ArrayList<>(v) : new ArrayList<>();
            return this;
        }
        public Builder modelVersion(String v) { modelVersion = v != null ? v : "unknown"; return this; }
        public Builder inferenceTimeMs(long v) { inferenceTimeMs = Math.max(0, v); return this; }
        public Builder metadata(Map<String, String> v) {
            metadata = v != null ? new HashMap<>(v) : new HashMap<>();
            return this;
        }

        public AiResponse build() {
            return new AiResponse(this);
        }
    }
}