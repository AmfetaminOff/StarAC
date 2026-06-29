package io.starac.ai;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class AiModelVersion {

    private final String version;
    private final String trainedOn;
    private final int featureCount;
    private final long trainedDate;
    private final String description;

    public AiModelVersion(String version, String trainedOn, int featureCount,
                          long trainedDate, String description) {
        this.version = version != null ? version : "unknown";
        this.trainedOn = trainedOn != null ? trainedOn : "unknown";
        this.featureCount = featureCount;
        this.trainedDate = trainedDate;
        this.description = description != null ? description : "No description";
    }

    public String getVersion() { return version; }
    public String getTrainedOn() { return trainedOn; }
    public int getFeatureCount() { return featureCount; }
    public long getTrainedDate() { return trainedDate; }
    public String getDescription() { return description; }

    public boolean isCompatible(int currentFeatureCount) {
        return featureCount == currentFeatureCount;
    }

    public String getFormattedDate() {
        if (trainedDate <= 0) return "unknown";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new Date(trainedDate));
    }

    public int getAgeInDays() {
        if (trainedDate <= 0) return -1;
        long ageMs = System.currentTimeMillis() - trainedDate;
        return (int) (ageMs / (1000 * 60 * 60 * 24));
    }

    public boolean isOutdated() {
        int age = getAgeInDays();
        return age > 90;
    }

    @Override
    public String toString() {
        return String.format("AiModel[v%s, features=%d, trained=%s, dataset=%s]",
                version, featureCount, getFormattedDate(), trainedOn);
    }

    public String toShortString() {
        return String.format("v%s (%d features)", version, featureCount);
    }

    public static AiModelVersion fromString(String str) {
        if (str == null || str.isBlank()) {
            return new AiModelVersion("unknown", "unknown", 0, 0, "Unknown model");
        }

        String[] parts = str.split("\\|");
        if (parts.length >= 5) {
            try {
                return new AiModelVersion(
                        parts[0].trim(),
                        parts[1].trim(),
                        Integer.parseInt(parts[2].trim()),
                        Long.parseLong(parts[3].trim()),
                        parts[4].trim()
                );
            } catch (NumberFormatException e) {
                return new AiModelVersion("unknown", "unknown", 0, 0, "Parse error");
            }
        }

        return new AiModelVersion("unknown", "unknown", 0, 0, "Unknown model");
    }

    public static AiModelVersion fromJson(String json) {
        String version = extractSimpleString(json, "version");
        String trainedOn = extractSimpleString(json, "trained_on");
        int featureCount = extractSimpleInt(json, "feature_count");
        long trainedDate = extractSimpleLong(json, "trained_date");
        String description = extractSimpleString(json, "description");

        return new AiModelVersion(version, trainedOn, featureCount, trainedDate, description);
    }

    private static String extractSimpleString(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return null;
        int colon = json.indexOf(":", idx);
        int quote1 = json.indexOf("\"", colon);
        if (quote1 < 0) return null;
        int quote2 = json.indexOf("\"", quote1 + 1);
        if (quote2 < 0) return null;
        return json.substring(quote1 + 1, quote2);
    }

    private static int extractSimpleInt(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return 0;
        int colon = json.indexOf(":", idx);
        int start = colon + 1;
        while (start < json.length() && !Character.isDigit(json.charAt(start))) {
            start++;
        }
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        if (start >= end) return 0;
        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static long extractSimpleLong(String json, String key) {
        return extractSimpleInt(json, key);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String version = "unknown";
        private String trainedOn = "unknown";
        private int featureCount = 0;
        private long trainedDate = 0;
        private String description = "";

        public Builder version(String v) { version = v; return this; }
        public Builder trainedOn(String v) { trainedOn = v; return this; }
        public Builder featureCount(int v) { featureCount = v; return this; }
        public Builder trainedDate(long v) { trainedDate = v; return this; }
        public Builder description(String v) { description = v; return this; }

        public AiModelVersion build() {
            return new AiModelVersion(version, trainedOn, featureCount, trainedDate, description);
        }
    }
}