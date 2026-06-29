package io.starac.violation.history;

import io.starac.StarAC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AppealManager {

    public enum AppealStatus {
        PENDING("§eОжидает"),
        ACCEPTED("§aПринята"),
        REJECTED("§cОтклонена");

        private final String displayName;

        AppealStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    private final StarAC plugin;
    private final Logger logger;
    private final Connection connection;

    public AppealManager(StarAC plugin, Connection connection) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.connection = connection;

        if (connection != null) {
            createTable();
        }
    }

    private void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS appeals (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    uuid VARCHAR(36) NOT NULL,
                    player_name VARCHAR(16) NOT NULL,
                    reason TEXT NOT NULL,
                    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
                    reviewer VARCHAR(16),
                    reviewer_comment TEXT,
                    created_at BIGINT NOT NULL,
                    reviewed_at BIGINT
                )
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("[AppealManager] Таблица appeals создана.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[AppealManager] Ошибка создания таблицы", e);
        }
    }

    public CompletableFuture<Boolean> submitAppeal(UUID uuid, String playerName, String reason) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO appeals (uuid, player_name, reason, status, created_at) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                pstmt.setString(2, playerName);
                pstmt.setString(3, reason);
                pstmt.setString(4, AppealStatus.PENDING.name());
                pstmt.setLong(5, System.currentTimeMillis());

                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    String msg = "§6[StarAC] §f" + playerName + " §7подал апелляцию. §e/starac appeal list";
                    Bukkit.getOnlinePlayers().stream()
                            .filter(p -> p.hasPermission("starac.appeals"))
                            .forEach(p -> p.sendMessage(msg));
                    return true;
                }

            } catch (SQLException e) {
                logger.log(Level.WARNING, "[AppealManager] Ошибка подачи апелляции", e);
            }

            return false;
        });
    }

    public CompletableFuture<Boolean> acceptAppeal(int appealId, String reviewer, String comment) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE appeals SET status = ?, reviewer = ?, reviewer_comment = ?, reviewed_at = ? WHERE id = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, AppealStatus.ACCEPTED.name());
                pstmt.setString(2, reviewer);
                pstmt.setString(3, comment);
                pstmt.setLong(4, System.currentTimeMillis());
                pstmt.setInt(5, appealId);

                int rows = pstmt.executeUpdate();
                return rows > 0;

            } catch (SQLException e) {
                logger.log(Level.WARNING, "[AppealManager] Ошибка принятия апелляции", e);
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> rejectAppeal(int appealId, String reviewer, String comment) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE appeals SET status = ?, reviewer = ?, reviewer_comment = ?, reviewed_at = ? WHERE id = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, AppealStatus.REJECTED.name());
                pstmt.setString(2, reviewer);
                pstmt.setString(3, comment);
                pstmt.setLong(4, System.currentTimeMillis());
                pstmt.setInt(5, appealId);

                int rows = pstmt.executeUpdate();
                return rows > 0;

            } catch (SQLException e) {
                logger.log(Level.WARNING, "[AppealManager] Ошибка отклонения апелляции", e);
                return false;
            }
        });
    }

    public List<Map<String, Object>> getPendingAppeals() {
        String sql = "SELECT * FROM appeals WHERE status = ? ORDER BY created_at ASC";
        List<Map<String, Object>> results = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, AppealStatus.PENDING.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("uuid", rs.getString("uuid"));
                row.put("player_name", rs.getString("player_name"));
                row.put("reason", rs.getString("reason"));
                row.put("created_at", rs.getLong("created_at"));
                results.add(row);
            }

        } catch (SQLException e) {
            logger.log(Level.WARNING, "[AppealManager] Ошибка чтения апелляций", e);
        }

        return results;
    }

    public Optional<Map<String, Object>> getAppeal(int appealId) {
        String sql = "SELECT * FROM appeals WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, appealId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("uuid", rs.getString("uuid"));
                row.put("player_name", rs.getString("player_name"));
                row.put("reason", rs.getString("reason"));
                row.put("status", rs.getString("status"));
                row.put("reviewer", rs.getString("reviewer"));
                row.put("reviewer_comment", rs.getString("reviewer_comment"));
                row.put("created_at", rs.getLong("created_at"));
                row.put("reviewed_at", rs.getLong("reviewed_at"));
                return Optional.of(row);
            }

        } catch (SQLException e) {
            logger.log(Level.WARNING, "[AppealManager] Ошибка чтения апелляции", e);
        }

        return Optional.empty();
    }

    public boolean hasPendingAppeal(UUID uuid) {
        String sql = "SELECT COUNT(*) FROM appeals WHERE uuid = ? AND status = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, AppealStatus.PENDING.name());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            logger.log(Level.WARNING, "[AppealManager] Ошибка проверки апелляции", e);
        }

        return false;
    }
}