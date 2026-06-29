package io.starac.ai.data;

import io.starac.StarAC;
import io.starac.ai.AiFeatureSet;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TrainingTask extends BukkitRunnable {

    private final StarAC plugin;
    private final DataCollector collector;
    private final Logger logger;
    private final File datasetDir;

    public TrainingTask(StarAC plugin, DataCollector collector) {
        this.plugin = plugin;
        this.collector = collector;
        this.logger = plugin.getLogger();
        this.datasetDir = new File(plugin.getDataFolder(), "datasets");

        if (!datasetDir.exists()) {
            datasetDir.mkdirs();
        }
    }

    public void startAutoExport(int intervalMinutes) {
        long intervalTicks = intervalMinutes * 60L * 20L;
        runTaskTimerAsynchronously(plugin, intervalTicks, intervalTicks);
        logger.info("[TrainingTask] Авто-экспорт датасета запущен (каждые " + intervalMinutes + " мин).");
    }

    @Override
    public void run() {
        if (collector.getBufferSize() < 10) {
            return;
        }
        exportNow();
    }

    public File exportNow() {
        List<DataCollector.TrainingSample> samples = collector.drain();
        if (samples.isEmpty()) return null;

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File file = new File(datasetDir, "dataset_" + timestamp + ".csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("uuid,timestamp,label,rotation_entropy,avg_yaw_speed,avg_pitch_speed,");
            writer.write("cps,click_variance,click_consistency,avg_reach,max_reach,");
            writer.write("avg_speed,max_speed,speed_variance,jump_frequency,sprint_ratio,tps\n");
            for (DataCollector.TrainingSample sample : samples) {
                AiFeatureSet f = sample.features();
                writer.write(String.format("%s,%d,%s,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.2f\n",
                        sample.playerUuid(),
                        sample.timestamp(),
                        sample.label(),
                        f.getRotationEntropy(),
                        f.getAvgYawSpeed(),
                        f.getAvgPitchSpeed(),
                        f.getCps(),
                        f.getClickVariance(),
                        f.getClickConsistency(),
                        f.getAvgReach(),
                        f.getMaxReach(),
                        f.getAvgSpeed(),
                        f.getMaxSpeed(),
                        f.getSpeedVariance(),
                        f.getJumpFrequency(),
                        f.getSprintRatio(),
                        f.getTps()
                ));
            }

            logger.info("[TrainingTask] Датасет экспортирован: " + file.getName() + " (" + samples.size() + " сэмплов)");
            return file;

        } catch (IOException e) {
            logger.log(Level.SEVERE, "[TrainingTask] Ошибка записи датасета", e);
            return null;
        }
    }
}