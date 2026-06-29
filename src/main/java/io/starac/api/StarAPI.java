package io.starac.api;

import io.starac.StarAC;
import io.starac.ai.AiVerdict;
import io.starac.violation.ViolationType;
import io.starac.violation.manager.ViolationManager;

import java.util.Map;
import java.util.UUID;

public final class StarAPI {

    private static StarAPI instance;
    private final StarAC plugin;

    private StarAPI(StarAC plugin) {
        this.plugin = plugin;
    }

    public static StarAPI getInstance() {
        return instance;
    }

    public static void init(StarAC plugin) {
        instance = new StarAPI(plugin);
    }

    public static void cleanup() {
        instance = null;
    }
    
    public int getVL(UUID uuid, ViolationType type) {
        if (plugin.getViolationManager() == null) return 0;
        return plugin.getViolationManager().getVL(uuid, type);
    }
    
    public int getTotalVL(UUID uuid) {
        if (plugin.getViolationManager() == null) return 0;
        return plugin.getViolationManager().getTotalVL(uuid);
    }

    public Map<ViolationType, Integer> getActiveVLs(UUID uuid) {
        if (plugin.getViolationManager() == null) return Map.of();
        return plugin.getViolationManager().getActiveVLs(uuid);
    }

    public boolean hasViolations(UUID uuid) {
        return getTotalVL(uuid) > 0;
    }
    
    public void resetVL(UUID uuid, ViolationType type) {
        if (plugin.getViolationManager() != null) {
            plugin.getViolationManager().resetVL(uuid, type);
        }
    }

    public void resetAllVL(UUID uuid) {
        if (plugin.getViolationManager() != null) {
            plugin.getViolationManager().resetAllVL(uuid);
        }
    }

    public AiVerdict getLastAiVerdict(UUID uuid) {
        if (plugin.getAiCheck() == null) return AiVerdict.UNKNOWN;
        var response = plugin.getAiCheck().getLastResponse(uuid);
        return response != null ? response.getVerdict() : AiVerdict.UNKNOWN;
    }

    public boolean isAiModelLoaded() {
        return plugin.getAiManager() != null && plugin.getAiManager().isPrimaryModelLoaded();
    }

    public String getAiModelName() {
        if (plugin.getAiManager() == null) return "none";
        return plugin.getAiManager().getPrimaryModelName();
    }

    public int getActivePlayerCount() {
        if (plugin.getViolationManager() == null) return 0;
        return plugin.getViolationManager().getActivePlayerCount();
    }

    public long getTotalAiInferences() {
        if (plugin.getAiManager() == null) return 0;
        return plugin.getAiManager().getTotalInferences();
    }

    public double getAverageAiInferenceTime() {
        if (plugin.getAiManager() == null) return 0.0;
        return plugin.getAiManager().getAverageInferenceTimeMs();
    }

    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
}