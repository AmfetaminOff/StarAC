package io.starac.analysis;

import io.starac.data.PlayerData;
import io.starac.violation.ViolationType;
import org.bukkit.entity.Player;

public interface Check {

    CheckResult check(Player player, PlayerData data);

    String getName();

    ViolationType getViolationType();

    default ViolationType.Category getCategory() {
        return getViolationType().getCategory();
    }

    default boolean isEnabled() {
        return true;
    }

    default int getPriority() {
        return 100;
    }

    default boolean supportsBypass() {
        return true;
    }

    default String getBypassPermission() {
        return "starac.bypass." + getViolationType().name().toLowerCase()
                + "." + getName().toLowerCase().replaceAll("\\s+", "");
    }
}