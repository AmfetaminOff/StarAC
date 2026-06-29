package io.starac.violation.action;

import io.starac.StarAC;
import io.starac.violation.Violation;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LogAction implements Action {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final StarAC plugin;
    private final Logger logger;
    private final int requiredVL;
    private final LogTarget target;
    private final File logFile;

    public enum LogTarget {
        FILE,
        CONSOLE,
        BOTH
    }

    public LogAction(StarAC plugin, int requiredVL, LogTarget target) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.requiredVL = requiredVL;
        this.target = target;
        this.logFile = new File(plugin.getDataFolder(), "logs/violations.log");

        if (!logFile.getParentFile().exists()) {
            logFile.getParentFile().mkdirs();
        }
    }

    @Override
    public void execute(Violation violation, Player player, int currentVL) {
        String timestamp = FORMATTER.format(Instant.now());
        String logEntry = String.format("[%s] %s | VL=%d",
                timestamp, violation.toLogString(), currentVL);

        if (target == LogTarget.CONSOLE || target == LogTarget.BOTH) {
            logger.info(logEntry);
        }

        if (target == LogTarget.FILE || target == LogTarget.BOTH) {
            writeToFile(logEntry);
        }
    }

    private synchronized void writeToFile(String entry) {
        try (PrintWriter writer = new PrintWriter(
                new BufferedWriter(new FileWriter(logFile, true)))) {
            writer.println(entry);
        } catch (IOException e) {
            logger.log(Level.WARNING, "[LogAction] Ошибка записи в " + logFile.getAbsolutePath(), e);
        }
    }

    @Override
    public String getName() {
        return "LOG";
    }

    @Override
    public boolean isSyncRequired() {
        return false;
    }

    @Override
    public int getRequiredVL() {
        return requiredVL;
    }

    public static Builder builder(StarAC plugin) {
        return new Builder(plugin);
    }

    public static final class Builder {
        private final StarAC plugin;
        private int requiredVL = 0;
        private LogTarget target = LogTarget.BOTH;

        public Builder(StarAC plugin) {
            this.plugin = plugin;
        }

        public Builder requiredVL(int vl) {
            this.requiredVL = Math.max(0, vl);
            return this;
        }

        public Builder target(LogTarget target) {
            this.target = target;
            return this;
        }

        public LogAction build() {
            return new LogAction(plugin, requiredVL, target);
        }
    }
}