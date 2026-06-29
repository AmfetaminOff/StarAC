package io.starac.violation.action;

import io.starac.violation.Violation;
import org.bukkit.entity.Player;

public interface Action {

    void execute(Violation violation, Player player, int currentVL);

    String getName();

    boolean isSyncRequired();

    default int getDelayTicks() {
        return 0;
    }

    default int getRequiredVL() {
        return 0;
    }
}