package io.starac.ai.model;

import io.starac.ai.AiFeatureSet;
import io.starac.ai.AiVerdict;

public interface AiModel {

    AiVerdict predict(AiFeatureSet features) throws ModelException;

    String getName();

    boolean isLoaded();

    int getExpectedFeatureCount();

    void close();

    class ModelException extends Exception {
        public ModelException(String message) {
            super(message);
        }

        public ModelException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}