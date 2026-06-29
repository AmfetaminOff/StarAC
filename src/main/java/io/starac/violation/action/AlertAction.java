package io.starac.violation.action;

import io.starac.alert.AlertManager;
import io.starac.alert.Alert;
import io.starac.alert.AlertType;
import io.starac.violation.Violation;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class AlertAction implements Action {

    private final AlertManager alertManager;
    private final int requiredVL;
    private final boolean includeDebugData;

    public AlertAction(AlertManager alertManager, int requiredVL, boolean includeDebugData) {
        this.alertManager = alertManager;
        this.requiredVL = requiredVL;
        this.includeDebugData = includeDebugData;
    }

    @Override
    public void execute(Violation violation, Player player, int currentVL) {
        if (alertManager == null) return;

        Alert.Builder builder = new Alert.Builder()
                .player(violation.playerUuid(), violation.playerName())
                .type(violation.severity() == io.starac.violation.Severity.CRITICAL
                        ? AlertType.CHEAT : AlertType.SUSPICIOUS)
                .score(violation.score())
                .checkName(violation.checkName());

        if (includeDebugData && !violation.debugData().isEmpty()) {
            List<String> flags = new ArrayList<>();
            violation.debugData().forEach((k, v) -> flags.add(k + "=" + v));
            builder.flags(flags);
        }

        Alert alert = builder.build();
        alertManager.send(alert);
    }

    @Override
    public String getName() {
        return "ALERT";
    }

    @Override
    public boolean isSyncRequired() {
        return false;
    }

    @Override
    public int getRequiredVL() {
        return requiredVL;
    }

    public static Builder builder(AlertManager alertManager) {
        return new Builder(alertManager);
    }

    public static final class Builder {
        private final AlertManager alertManager;
        private int requiredVL = 0;
        private boolean includeDebugData = true;

        public Builder(AlertManager alertManager) {
            this.alertManager = alertManager;
        }

        public Builder requiredVL(int vl) {
            this.requiredVL = Math.max(0, vl);
            return this;
        }

        public Builder includeDebugData(boolean v) {
            this.includeDebugData = v;
            return this;
        }

        public AlertAction build() {
            return new AlertAction(alertManager, requiredVL, includeDebugData);
        }
    }
}