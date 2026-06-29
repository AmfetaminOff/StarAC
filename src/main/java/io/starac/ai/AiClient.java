package io.starac.ai;

import io.starac.data.PlayerData;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

public class AiClient {

    private final String baseUrl;
    private final int connectTimeout;
    private final int readTimeout;

    public AiClient(String baseUrl, int connectTimeout, int readTimeout) {
        this.baseUrl        = baseUrl;
        this.connectTimeout = connectTimeout;
        this.readTimeout    = readTimeout;
    }

    public static class AnalysisResult {
        public final String verdict;
        public final double probability;
        public final double rfProbability;
        public final double nnProbability;
        public final List<String> flags;

        public AnalysisResult(String verdict, double probability,
                              double rfProbability, double nnProbability,
                              List<String> flags) {
            this.verdict       = verdict;
            this.probability   = probability;
            this.rfProbability = rfProbability;
            this.nnProbability = nnProbability;
            this.flags         = flags;
        }
    }

    public AnalysisResult analyze(String uuid, String name, PlayerData data) throws Exception {
        String body = buildJson(uuid, name, data);

        URL url = new URL(baseUrl + "/analyze");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("AI server returned HTTP " + code);
        }

        String response;
        try (Scanner sc = new Scanner(conn.getInputStream())) {
            response = sc.useDelimiter("\\A").next();
        }

        return parseResult(response);
    }

    public boolean isHealthy() {
        try {
            URL url = new URL(baseUrl + "/health");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private String buildJson(String uuid, String name, PlayerData data) {
        List<Float>  yaws    = data.getYawDeltas();
        List<Float>  pitches = data.getPitchDeltas();
        List<Float>  gcds    = data.getGcdValues();
        List<Double> reaches = data.getReachValues();
        List<Double> strafes = data.getStrafeDelta();

        int size = yaws.size();

        double cps           = data.getCps();
        double clickVariance = data.getClickVariance();

        StringBuilder sb = new StringBuilder();
        sb.append("{\"player_uuid\":\"").append(uuid).append("\",");
        sb.append("\"player_name\":\"").append(name).append("\",");
        sb.append("\"ticks\":[");

        for (int i = 0; i < size; i++) {
            double dyaw   = i < yaws.size()    ? yaws.get(i)    : 0f;
            double dpitch = i < pitches.size() ? pitches.get(i) : 0f;
            double gcd    = i < gcds.size()    ? gcds.get(i)    : 0.1f;
            double reach  = i < reaches.size() ? reaches.get(i) : 2.5;
            double strafe = i < strafes.size() ? strafes.get(i) : 0.0;

            sb.append(String.format(
                    "{\"dyaw\":%.4f,\"dpitch\":%.4f,\"gcd\":%.4f,\"cps\":%.1f,\"click_variance\":%.2f,\"reach\":%.4f,\"strafe\":%.4f}",
                    dyaw, dpitch, gcd, cps, clickVariance, reach, strafe
            ));
            if (i < size - 1) sb.append(",");
        }

        sb.append("]}");
        return sb.toString();
    }

    private AnalysisResult parseResult(String json) {
        String verdict     = parseString(json, "verdict");
        double probability = parseDouble(json, "probability");
        double rfProb      = parseDouble(json, "rf_probability");
        double nnProb      = parseDouble(json, "nn_probability");
        List<String> flags = parseFlags(json);

        return new AnalysisResult(verdict, probability, rfProb, nnProb, flags);
    }

    private String parseString(String json, String key) {
        try {
            String search = "\"" + key + "\":\"";
            int idx = json.indexOf(search);
            if (idx < 0) return "UNKNOWN";
            int start = idx + search.length();
            int end   = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private double parseDouble(String json, String key) {
        try {
            String search = "\"" + key + "\":";
            int idx = json.indexOf(search);
            if (idx < 0) return -1;
            int start = idx + search.length();
            int end   = json.indexOf(",", start);
            if (end < 0) end = json.indexOf("}", start);
            return Double.parseDouble(json.substring(start, end).trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private List<String> parseFlags(String json) {
        List<String> result = new java.util.ArrayList<>();
        try {
            int start = json.indexOf("\"flags\":[");
            if (start < 0) return result;
            start += 9;
            int end = json.indexOf("]", start);
            String flagsStr = json.substring(start, end);
            if (flagsStr.isBlank()) return result;
            for (String part : flagsStr.split(",")) {
                String flag = part.replace("\"", "").trim();
                if (!flag.isEmpty()) result.add(flag);
            }
        } catch (Exception ignored) {}
        return result;
    }
}