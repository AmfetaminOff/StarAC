package io.starac.ai.model;

import io.starac.ai.AiFeatureSet;
import io.starac.ai.AiVerdict;

import java.util.List;

public final class HeuristicFallBack implements AiModel {

    private static final String NAME = "heuristic-fallback";

    @Override
    public AiVerdict predict(AiFeatureSet features) {
        if (features == null || features.getDataPoints() < 5) {
            return AiVerdict.UNKNOWN;
        }

        int suspicionScore = 0;

        if (features.getCps() > 20.0 && features.getClickVariance() < 0.5) {
            suspicionScore += 2;
        } else if (features.getCps() > 15.0 && features.getClickConsistency() < 0.3) {
            suspicionScore += 1;
        }

        if (features.getMaxReach() > 4.0) {
            suspicionScore += 3;
        } else if (features.getAvgReach() > 3.5) {
            suspicionScore += 2;
        }

        if (features.getRotationEntropy() < 0.3) {
            suspicionScore += 2;
        }

        if (features.getMaxSpeed() > 1.5 && features.getSpeedVariance() < 0.1) {
            suspicionScore += 2;
        }

        if (features.getJumpFrequency() > 5.0) {
            suspicionScore += 1;
        }

        if (suspicionScore >= 6) {
            return AiVerdict.CONFIRMED;
        } else if (suspicionScore >= 4) {
            return AiVerdict.CHEATING;
        } else if (suspicionScore >= 2) {
            return AiVerdict.SUSPICIOUS;
        } else {
            return AiVerdict.CLEAN;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public int getExpectedFeatureCount() {
        return 19;
    }

    @Override
    public void close() {
    }
}