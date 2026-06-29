package io.starac.ai.data;

import io.starac.StarAC;
import io.starac.ai.AiFeatureSet;
import io.starac.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Асинхронный рекордер сессий для обучения AI-моделей.
 *
 * <p>Записывает последовательности фичей игроков в JSONL файлы.
 * Используется для:
 * <ul>
 *   <li>Обучения облачной модели (тяжелые паттерны)</li>
 *   <li>Валидации локальной ONNX модели</li>
 *   <li>Анализа ложных срабатываний</li>
 * </ul>
 *
 * <p>Формат файла: одна строка = один тик сессии в JSON.
 * Файлы ротируются при достижении лимита размера.
 */
public final class SessionRecorder extends BukkitRunnable {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");

    private final StarAC plugin;
    private final Logger logger;
    private final File sessionsDir;
    private final ConcurrentLinkedQueue<SessionFrame> writeQueue;

    private final int maxFileSizeMB;
    private final int flushIntervalTicks;
    private final boolean recordCleanPlayers;
    private final int minDataPointsForRecord;

    private BufferedWriter currentWriter;
    private File currentFile;
    private long currentFileSize = 0;
    private volatile boolean recording = false;

    /**
     * Один кадр сессии (один тик игрока).
     */
    public record SessionFrame(
            UUID playerUuid,
            String playerName,
            long timestamp,
            long serverTick,
            AiFeatureSet features,
            String label,      // "CLEAN", "SUSPICIOUS", "CHEATING", "CONFIRMED"
            int currentVL,
            String metadata    // доп. инфо в JSON формате
    ) {}

    public SessionRecorder(StarAC plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.sessionsDir = new File(plugin.getDataFolder(), "sessions");
        this.writeQueue = new ConcurrentLinkedQueue<>();
        this.maxFileSizeMB = plugin.getConfig().getInt("ai.session-recorder.max-file-size-mb", 50);
        this.flushIntervalTicks = plugin.getConfig().getInt("ai.session-recorder.flush-interval-ticks", 100);
        this.recordCleanPlayers = plugin.getConfig().getBoolean("ai.session-recorder.record-clean-players", false);
        this.minDataPointsForRecord = plugin.getConfig().getInt("ai.session-recorder.min-data-points", 10);

        if (!sessionsDir.exists()) {
            sessionsDir.mkdirs();
        }
    }

    public void start() {
        if (!plugin.getAiConfig().isDataCollectionEnabled()) {
            logger.info("[SessionRecorder] Запись сессий отключена в конфиге.");
            return;
        }

        recording = true;
        runTaskTimerAsynchronously(plugin, flushIntervalTicks, flushIntervalTicks);
        logger.info("[SessionRecorder] Запись сессий запущена (интервал: " + flushIntervalTicks + " тиков).");
    }

    @Override
    public void run() {
        if (!recording || writeQueue.isEmpty()) return;

        try {
            ensureWriterOpen();

            SessionFrame frame;
            int flushed = 0;
            while ((frame = writeQueue.poll()) != null && flushed < 1000) {
                String json = serializeFrame(frame);
                currentWriter.write(json);
                currentWriter.newLine();

                currentFileSize += json.length() + 1;
                flushed++;

                if (currentFileSize > maxFileSizeMB * 1024L * 1024L) {
                    rotateFile();
                }
            }

            currentWriter.flush();

        } catch (IOException e) {
            logger.log(Level.SEVERE, "[SessionRecorder] Ошибка записи сессии", e);
            closeWriter();
        }
    }

    public void recordFrame(Player player, PlayerData data, AiFeatureSet features,
                            String label, int currentVL, String metadata) {
        if (!recording) return;
        if (features == null || features.getDataPoints() < minDataPointsForRecord) return;

        if (!recordCleanPlayers && "CLEAN".equals(label)) return;

        SessionFrame frame = new SessionFrame(
                player.getUniqueId(),
                player.getName(),
                System.currentTimeMillis(),
                Bukkit.getCurrentTick(),
                features,
                label != null ? label : "UNKNOWN",
                currentVL,
                metadata != null ? metadata : "{}"
        );

        writeQueue.offer(frame);
    }

    public void recordFrame(Player player, PlayerData data, AiFeatureSet features,
                            String label, int currentVL) {
        recordFrame(player, data, features, label, currentVL, null);
    }


    private void ensureWriterOpen() throws IOException {
        if (currentWriter == null) {
            String timestamp = DATE_FORMAT.format(new Date());
            currentFile = new File(sessionsDir, "session_" + timestamp + ".jsonl");
            currentWriter = new BufferedWriter(new FileWriter(currentFile, true));
            currentFileSize = currentFile.exists() ? currentFile.length() : 0;
            logger.fine("[SessionRecorder] Открыт файл: " + currentFile.getName());
        }
    }

    private void rotateFile() throws IOException {
        closeWriter();
        logger.info("[SessionRecorder] Ротация файла (достигнут лимит " + maxFileSizeMB + "MB)");
        ensureWriterOpen();
    }

    private void closeWriter() {
        if (currentWriter != null) {
            try {
                currentWriter.flush();
                currentWriter.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "[SessionRecorder] Ошибка закрытия файла", e);
            }
            currentWriter = null;
            currentFile = null;
            currentFileSize = 0;
        }
    }

    private String serializeFrame(SessionFrame frame) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("{");
        sb.append("\"uuid\":\"").append(frame.playerUuid()).append("\",");
        sb.append("\"name\":\"").append(escapeJson(frame.playerName())).append("\",");
        sb.append("\"ts\":").append(frame.timestamp()).append(",");
        sb.append("\"tick\":").append(frame.serverTick()).append(",");
        sb.append("\"label\":\"").append(frame.label()).append("\",");
        sb.append("\"vl\":").append(frame.currentVL()).append(",");
        sb.append("\"metadata\":").append(frame.metadata()).append(",");
        sb.append("\"features\":").append(frame.features().toJson());
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public void stop() {
        recording = false;
        cancel();
        closeWriter();
        logger.info("[SessionRecorder] Запись остановлена.");
    }

    public boolean isRecording() {
        return recording;
    }

    public int getQueueSize() {
        return writeQueue.size();
    }

    public File getSessionsDir() {
        return sessionsDir;
    }

    public File[] getSessionFiles() {
        return sessionsDir.listFiles((dir, name) -> name.endsWith(".jsonl"));
    }

    public int cleanupOldSessions(int daysToKeep) {
        long cutoff = System.currentTimeMillis() - (daysToKeep * 24L * 60 * 60 * 1000);
        File[] files = getSessionFiles();
        if (files == null) return 0;

        int deleted = 0;
        for (File file : files) {
            if (file.lastModified() < cutoff) {
                try {
                    Files.delete(file.toPath());
                    deleted++;
                } catch (IOException e) {
                    logger.warning("[SessionRecorder] Не удалось удалить " + file.getName());
                }
            }
        }

        if (deleted > 0) {
            logger.info("[SessionRecorder] Удалено " + deleted + " старых файлов сессий.");
        }
        return deleted;
    }
}