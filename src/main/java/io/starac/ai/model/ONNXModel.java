package io.starac.ai.model;

import ai.onnxruntime.*;
import io.starac.ai.AiFeatureSet;
import io.starac.ai.AiVerdict;

import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

public final class ONNXModel implements AiModel {

    private final Logger logger = Logger.getLogger("StarAC-ONNX");

    private final String name;
    private final String modelPath;
    private final OrtEnvironment env;
    private final OrtSession session;
    private final int expectedFeatureCount;
    private final String inputName;

    public ONNXModel(String modelPath, String name) throws ModelException {
        this.modelPath = modelPath;
        this.name = name;

        try {
            this.env = OrtEnvironment.getEnvironment();

            OrtSession.SessionOptions options = new OrtSession.SessionOptions();

            this.session = env.createSession(modelPath, options);

            Map<String, NodeInfo> inputInfo = session.getInputInfo();
            if (inputInfo.isEmpty()) {
                throw new ModelException("Модель не имеет входных нодов");
            }

            NodeInfo firstInput = inputInfo.values().iterator().next();
            this.inputName = firstInput.getName();

            TensorInfo tensorInfo = (TensorInfo) firstInput.getInfo();
            long[] shape = tensorInfo.shape;

            this.expectedFeatureCount = (int) shape[1];

            logger.info("[ONNXModel] Модель '" + name + "' загружена. Input: "
                    + inputName + " [" + expectedFeatureCount + " features]");

        } catch (OrtException e) {
            throw new ModelException("Ошибка загрузки ONNX модели: " + e.getMessage(), e);
        }
    }

    @Override
    public AiVerdict predict(AiFeatureSet features) throws ModelException {
        if (!isLoaded()) {
            throw new ModelException("Модель не загружена");
        }

        if (features.getFeatureCount() != expectedFeatureCount) {
            throw new ModelException("Ожидалось " + expectedFeatureCount
                    + " фичей, получено " + features.getFeatureCount());
        }

        try {
            float[] inputArray = featuresToFloatArray(features);

            OnnxTensor inputTensor = OnnxTensor.createTensor(
                    env,
                    FloatBuffer.wrap(inputArray),
                    new long[]{1, expectedFeatureCount}
            );

            Map<String, OnnxTensor> inputs = Collections.singletonMap(inputName, inputTensor);
            OrtSession.Result result = session.run(inputs);

            OnnxTensor outputTensor = (OnnxTensor) result.get(0);
            float[][] output = (float[][]) outputTensor.getValue();
            float[] probabilities = output[0];

            AiVerdict verdict = interpretProbabilities(probabilities);

            inputTensor.close();
            result.close();

            return verdict;

        } catch (OrtException e) {
            throw new ModelException("Ошибка инференса: " + e.getMessage(), e);
        }
    }

    private float[] featuresToFloatArray(AiFeatureSet features) {
        float[] array = new float[expectedFeatureCount];
        int idx = 0;

        array[idx++] = (float) features.getRotationEntropy();
        array[idx++] = (float) features.getAvgYawSpeed();
        array[idx++] = (float) features.getAvgPitchSpeed();
        array[idx++] = (float) features.getCps();
        array[idx++] = (float) features.getClickVariance();
        array[idx++] = (float) features.getClickConsistency();
        array[idx++] = (float) features.getAvgReach();
        array[idx++] = (float) features.getMaxReach();
        array[idx++] = (float) features.getAvgSpeed();
        array[idx++] = (float) features.getMaxSpeed();
        array[idx++] = (float) features.getSpeedVariance();
        array[idx++] = (float) features.getJumpFrequency();
        array[idx++] = (float) features.getSprintRatio();
        array[idx++] = (float) average(features.getYawDeltas());
        array[idx++] = (float) average(features.getPitchDeltas());
        array[idx++] = (float) average(features.getGcdValues());
        array[idx++] = (float) average(features.getReachValues());
        array[idx++] = (float) average(features.getStrafeDeltas());
        array[idx++] = (float) features.getTps();

        return array;
    }

    private double average(java.util.List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private AiVerdict interpretProbabilities(float[] probabilities) {
        if (probabilities.length < 4) {
            logger.warning("[ONNXModel] Неожиданный размер output: " + probabilities.length);
            return AiVerdict.UNKNOWN;
        }

        int maxIdx = 0;
        float maxProb = probabilities[0];
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i];
                maxIdx = i;
            }
        }

        return switch (maxIdx) {
            case 0 -> AiVerdict.CLEAN;
            case 1 -> AiVerdict.SUSPICIOUS;
            case 2 -> AiVerdict.CHEATING;
            case 3 -> AiVerdict.CONFIRMED;
            default -> AiVerdict.UNKNOWN;
        };
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isLoaded() {
        return session != null && env != null;
    }

    @Override
    public int getExpectedFeatureCount() {
        return expectedFeatureCount;
    }

    @Override
    public void close() {
        try {
            if (session != null) session.close();
            logger.info("[ONNXModel] Модель '" + name + "' закрыта.");
        } catch (OrtException e) {
            logger.warning("[ONNXModel] Ошибка закрытия сессии: " + e.getMessage());
        }
    }

    public String getModelPath() {
        return modelPath;
    }
}