package io.starac;

import io.starac.ai.*;
import io.starac.ai.data.DataCollector;
import io.starac.ai.data.SessionRecorder;
import io.starac.ai.data.TrainingTask;
import io.starac.ai.inference.AsyncInferenceTask;
import io.starac.ai.inference.InferenceEngine;
import io.starac.alert.AlertManager;
import io.starac.api.StarAPI;
import io.starac.command.StarCommand;
import io.starac.config.AlertConfig;
import io.starac.data.DataManager;
import io.starac.listener.PlayerListener;
import io.starac.listener.PacketListener;
import io.starac.task.DecayTask;
import io.starac.violation.ViolationType;
import io.starac.violation.history.AppealManager;
import io.starac.violation.history.ViolationHistory;
import io.starac.violation.manager.PunishmentProfile;
import io.starac.violation.manager.ViolationManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.util.logging.Logger;

public final class StarAC extends JavaPlugin {

    private static StarAC instance;

    private DataManager dataManager;
    private AlertManager alertManager;
    private ViolationManager violationManager;
    private AiManager aiManager;
    private AiCheck aiCheck;
    private InferenceEngine inferenceEngine;
    private AsyncInferenceTask asyncInferenceTask;
    private DataCollector dataCollector;
    private TrainingTask trainingTask;
    private AlertConfig alertConfig;
    private AiConfig aiConfig;
    private ViolationHistory violationHistory;
    private AppealManager appealManager;
    private DecayTask decayTask;
    private PacketListener packetListener;
    private SessionRecorder sessionRecorder;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        Logger logger = getLogger();
        logger.info("§a[StarAC] §fЗапуск AI-античита...");

        alertConfig = AlertConfig.load(getConfig());
        aiConfig = AiConfig.load(getConfig());

        dataManager = new DataManager();

        violationHistory = new ViolationHistory(this);

        PunishmentProfile punishmentProfile = PunishmentProfile.load(
                this, alertManager, dataManager, getConfig());

        alertManager = new AlertManager(this, alertConfig);

        violationManager = new ViolationManager(this, dataManager, punishmentProfile, violationHistory);

        if (aiConfig.isEnabled()) {
            initAiSystem();
        } else {
            logger.info("§e[StarAC] §fAI система отключена в конфиге.");
        }
        sessionRecorder = new SessionRecorder(this);
        sessionRecorder.start();

        packetListener = new PacketListener(this, dataManager, violationManager);
        packetListener.register();

        getServer().getPluginManager().registerEvents(
                new PlayerListener(this, dataManager, violationManager), this);

        decayTask = new DecayTask(this, dataManager, violationManager);
        decayTask.start(1200);

        getCommand("starac").setExecutor(new StarCommand(this));
        getCommand("starac").setTabCompleter(new StarCommand(this));
        StarAPI.init(this);

        logger.info("§a[StarAC] §fАнтичит успешно запущен!");
        logger.info("§a[StarAC] §7Версия: §f" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        Logger logger = getLogger();
        logger.info("§c[StarAC] §fОстановка античита...");

        if (sessionRecorder != null) sessionRecorder.stop();
        if (decayTask != null) decayTask.cancel();
        if (asyncInferenceTask != null) asyncInferenceTask.cancel();
        if (trainingTask != null) trainingTask.cancel();
        if (aiCheck != null) aiCheck.cancel();
        if (packetListener != null) packetListener.unregister();
        if (aiManager != null && aiManager.isPrimaryModelLoaded()) {
        }

        if (violationHistory != null) violationHistory.close();

        StarAPI.cleanup();

        logger.info("§c[StarAC] §fАнтичит остановлен.");
    }

    private void initAiSystem() {
        Logger logger = getLogger();

        aiManager = AiManager.getInstance(this);

        if (aiConfig.isDataCollectionEnabled()) {
            dataCollector = new DataCollector(aiConfig.getMaxBufferSize());
            trainingTask = new TrainingTask(this, dataCollector);
            trainingTask.startAutoExport(aiConfig.getAutoExportIntervalMins());
            logger.info("§a[StarAC] §fСбор данных для обучения включен.");
        }

        inferenceEngine = new InferenceEngine(aiManager, aiConfig.getMaxQueueSize(), aiConfig.getBatchSize());

        asyncInferenceTask = new AsyncInferenceTask(this, inferenceEngine);
        asyncInferenceTask.start(aiConfig.getTickInterval());

        aiCheck = new AiCheck(this, dataManager, aiManager, violationManager, aiConfig);
        aiCheck.start();

        logger.info("§a[StarAC] §fAI система инициализирована.");
    }


    public static StarAC getInstance() {
        return instance;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }

    public ViolationManager getViolationManager() {
        return violationManager;
    }

    public AiManager getAiManager() {
        return aiManager;
    }

    public AiCheck getAiCheck() {
        return aiCheck;
    }

    public InferenceEngine getInferenceEngine() {
        return inferenceEngine;
    }

    public DataCollector getDataCollector() {
        return dataCollector;
    }

    public AlertConfig getAlertConfig() {
        return alertConfig;
    }

    public AiConfig getAiConfig() {
        return aiConfig;
    }

    public ViolationHistory getViolationHistory() {
        return violationHistory;
    }

    public AppealManager getAppealManager() {
        return appealManager;
    }

    public SessionRecorder getSessionRecorder() { return sessionRecorder; }
}