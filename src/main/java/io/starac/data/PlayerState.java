package io.starac.data;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public record PlayerState(
        Location location,
        Vector velocity,
        float yaw,
        float pitch,
        boolean onGround,
        boolean isSprinting,
        boolean isSneaking,
        boolean isFlying,
        boolean isGliding,
        boolean isInWater,
        boolean isInLava,
        boolean isBlocking,
        boolean isUsingItem,
        long serverTick,
        long timestampMs
) {

    public static PlayerState capture(Player player, long serverTick) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }

        return new PlayerState(
                player.getLocation().clone(),
                player.getVelocity().clone(),
                player.getLocation().getYaw(),
                player.getLocation().getPitch(),
                player.isOnGround(),
                player.isSprinting(),
                player.isSneaking(),
                player.isFlying(),
                player.isGliding(),
                player.isInWater(),
                player.isInLava(),
                player.isBlocking(),
                player.isHandRaised(),
                serverTick,
                System.currentTimeMillis()
        );
    }

    public double distanceTo(PlayerState other) {
        if (other == null || other.location() == null || this.location() == null) return 0.0;
        if (this.location().getWorld() != other.location().getWorld()) return Double.MAX_VALUE;
        return this.location().distance(other.location());
    }

    public double horizontalDistanceTo(PlayerState other) {
        if (other == null || other.location() == null || this.location() == null) return 0.0;
        if (this.location().getWorld() != other.location().getWorld()) return Double.MAX_VALUE;
        double dx = this.location().getX() - other.location().getX();
        double dz = this.location().getZ() - other.location().getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    public float yawDelta(PlayerState other) {
        if (other == null) return 0.0f;
        float delta = this.yaw - other.yaw;
        while (delta > 180) delta -= 360;
        while (delta < -180) delta += 360;
        return delta;
    }

    public float pitchDelta(PlayerState other) {
        if (other == null) return 0.0f;
        return this.pitch - other.pitch;
    }

    public long deltaTimeMs(PlayerState other) {
        if (other == null) return 0;
        return Math.abs(this.timestampMs - other.timestampMs);
    }

    public double speedRelativeTo(PlayerState other) {
        long dt = deltaTimeMs(other);
        if (dt <= 0) return 0.0;
        double dist = distanceTo(other);
        return (dist / dt) * 1000.0;
    }

    public boolean isValid() {
        return location != null && location.getWorld() != null;
    }

    public static PlayerState empty() {
        return new PlayerState(
                null, new Vector(0, 0, 0),
                0.0f, 0.0f,
                false, false, false, false, false,
                false, false, false, false,
                -1, 0
        );
    }
}