package io.starac.violation.history;

import io.starac.StarAC;
import io.starac.violation.Violation;
import io.starac.violation.ViolationType;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ViolationHistory {

    private final StarAC plugin;
    private final Logger logger;
    private final Connection connection;
    private final boolean enabled;

    public ViolationHistory(StarAC plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.enabled = plugin.getConfig().getBoolean("history.enabled", true);

        if (!enabled) {
            this.connection = null;
            return;
        }

        this.connection = initConnection();
        if (connection != null) {
            createTable();
        }
    }

    private Connection initConnection() {
        String type = plugin.getConfig().getString("history.database-type", "SQLITE").toUpperCase();

        try {
            if ("MYSQL".equals(type)) {
                String host = plugin.getConfig().getString("history.mysql.host", "localhost");
                int port = plugin.getConfig().getInt("history.mysql.port", 3306);
                String database = plugin.getConfig().getString("history.mysql.database", "starac");
                String username = plugin.getConfig().getString("history.mysql.username", "root");
                String password = plugin.getConfig().getString("history.mysql.password", "");

                String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false", host, port, database);
                return DriverManager.getConnection(url, username, password);

            } else {
                String dbFile = plugin.getConfig().getString("history.sqlite.file", "violations.db");
                String url = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/" + dbFile;
                return DriverManager.getConnection(url);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[ViolationHistory] Ошибка подключения к БД", e);
            return null;
        }
    }

    private void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS violations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    uuid VARCHAR(36) NOT NULL,
                    player_name VARCHAR(16) NOT NULL,
                    type VARCHAR(32) NOT NULL,
                    severity VARCHAR(16) NOT NULL,
                    score DOUBLE NOT NULL,
                    check_name VARCHAR(64),
                    debug_json TEXT,
                    vl_at_flag INTEGER NOT NULL,
                    timestamp BIGINT NOT NULL
                )
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_uuid ON violations(uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_type ON violations(type)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_timestamp ON violations(timestamp)");

            logger.info("[ViolationHistory] Таблица violations создана/проверена.");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[ViolationHistory] Ошибка создания таблицы", e);
        }
    }

    public void save(Violation violation, int vlAtFlag) {
        if (!enabled || connection == null) return;

        String sql = "INSERT INTO violations (uuid, player_name, type, severity, score, check_name, debug_json, vl_at_flag, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, violation.playerUuid().toString());
            pstmt.setString(2, violation.playerName());
            pstmt.setString(3, violation.type().name());
            pstmt.setString(4, violation.severity().name());
            pstmt.setDouble(5, violation.score());
            pstmt.setString(6, violation.checkName());
            pstmt.setString(7, mapToJson(violation.debugData()));
            pstmt.setInt(8, vlAtFlag);
            pstmt.setLong(9, violation.timestamp());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            logger.log(Level.WARNING, "[ViolationHistory] Ошибка сохранения", e);
        }
    }

    public CompletableFuture<Void> saveAsync(Violation violation, int vlAtFlag) {
        return CompletableFuture.runAsync(() -> save(violation, vlAtFlag));
    }

    public List<Map<String, Object>> getHistory(UUID uuid, int limit) {
        if (!enabled || connection == null) return Collections.emptyList();

        String sql = "SELECT * FROM violations WHERE uuid = ? ORDER BY timestamp DESC LIMIT ?";
        List<Map<String, Object>> results = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setInt(2, limit);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("type", rs.getString("type"));
                row.put("severity", rs.getString("severity"));
                row.put("score", rs.getDouble("score"));
                row.put("check_name", rs.getString("check_name"));
                row.put("debug_json", rs.getString("debug_json"));
                row.put("vl_at_flag", rs.getInt("vl_at_flag"));
                row.put("timestamp", rs.getLong("timestamp"));
                results.add(row);
            }

        } catch (SQLException e) {
            logger.log(Level.WARNING, "[ViolationHistory] Ошибка чтения истории", e);
        }

        return results;
    }

    public int getTotalFlags(UUID uuid) {
        if (!enabled || connection == null) return 0;

        String sql = "SELECT COUNT(*) FROM violations WHERE uuid = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            logger.log(Level.WARNING, "[ViolationHistory] Ошибка подсчёта флагов", e);
        }

        return 0;
    }

    public List<Map<String, Object>> search(ViolationType type, long fromTimestamp, long toTimestamp, int limit) {
        if (!enabled || connection == null) return Collections.emptyList();

        String sql = "SELECT * FROM violations WHERE type = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp DESC LIMIT ?";
        List<Map<String, Object>> results = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, type.name());
            pstmt.setLong(2, fromTimestamp);
            pstmt.setLong(3, toTimestamp);
            pstmt.setInt(4, limit);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("uuid", rs.getString("uuid"));
                row.put("player_name", rs.getString("player_name"));
                row.put("score", rs.getDouble("score"));
                row.put("vl_at_flag", rs.getInt("vl_at_flag"));
                row.put("timestamp", rs.getLong("timestamp"));
                results.add(row);
            }

        } catch (SQLException e) {
            logger.log(Level.WARNING, "[ViolationHistory] Ошибка поиска", e);
        }

        return results;
    }

    private String mapToJson(Map<String, String> map) {
        if (map == null || map.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("[ViolationHistory] Соединение с БД закрыто.");
            } catch (SQLException e) {
                logger.log(Level.WARNING, "[ViolationHistory] Ошибка закрытия соединения", e);
            }
        }
    }
}