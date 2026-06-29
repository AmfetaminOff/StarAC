package io.starac.data;

import io.starac.util.CircularBuffer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class PlayerData {

    private static final int MAX_STATE_HISTORY = 20;
    private static final int MAX_DELTA_BUFFER = 40;
    private final UUID uuid;
    private final String name;
    private final CircularBuffer<PlayerState> stateHistory;
    private PlayerState currentState;
    private PlayerState previousState;
    private final CircularBuffer<Double> yawDeltas;
    private final CircularBuffer<Double> pitchDeltas;
    private final CircularBuffer<Double> reachValues;
    private final CircularBuffer<Double> speedValues;
    private final CircularBuffer<Double> strafeDeltas;
    private final CircularBuffer<Double> gcdValues;
    private final CircularBuffer<Long> clickTimestamps;
    private double cps = 0.0;
    private double clickVariance = 0.0;
    private double jumpFrequency = 0.0;
    private int sprintTicks = 0;
    private int totalTicks = 0;
    private long lastAttackTime = 0;
    private UUID lastTargetUuid = null;
    private Location lastSafeLocation = null;
    private int combatStreak = 0;
    private boolean wasOnGround = false;
    private int airTicks = 0;
    private int groundTicks = 0;
    private double lastFallDistance = 0.0;

    public PlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();

        this.stateHistory = new CircularBuffer<>(MAX_STATE_HISTORY);
        this.currentState = PlayerState.empty();
        this.previousState = PlayerState.empty();

        this.yawDeltas = new CircularBuffer<>(MAX_DELTA_BUFFER);
        this.pitchDeltas = new CircularBuffer<>(MAX_DELTA_BUFFER);
        this.reachValues = new CircularBuffer<>(MAX_DELTA_BUFFER);
        this.speedValues = new CircularBuffer<>(MAX_DELTA_BUFFER);
        this.strafeDeltas = new CircularBuffer<>(MAX_DELTA_BUFFER);
        this.gcdValues = new CircularBuffer<>(MAX_DELTA_BUFFER);
        this.clickTimestamps = new CircularBuffer<>(MAX_DELTA_BUFFER);
        this.lastSafeLocation = player.getLocation().clone();
    }

    public void updateState(Player player, long serverTick) {
        this.previousState = this.currentState;
        this.currentState = PlayerState.capture(player, serverTick);

        stateHistory.add(currentState);

        if (previousState.isValid() && currentState.isValid()) {
            double yawDelta = currentState.yawDelta(previousState);
            double pitchDelta = currentState.pitchDelta(previousState);
            double speed = currentState.speedRelativeTo(previousState);
            double hDist = currentState.horizontalDistanceTo(previousState);

            yawDeltas.add(yawDelta);
            pitchDeltas.add(pitchDelta);
            speedValues.add(speed);
            strafeDeltas.add(hDist);

            if (currentState.onGround()) {
                groundTicks++;
                airTicks = 0;
            } else {
                airTicks++;
                groundTicks = 0;
            }

            if (currentState.isSprinting()) {
                sprintTicks++;
            }
            totalTicks++;
        }

        if (currentState.onGround() && !isInCombat()) {
            this.lastSafeLocation = currentState.location().clone();
        }
    }

    public void recordClick() {
        long now = System.currentTimeMillis();
        clickTimestamps.add(now);
        recalculateCps();
    }

    public void recordAttack(Player target, double reach) {
        this.lastAttackTime = System.currentTimeMillis();
        this.lastTargetUuid = target.getUniqueId();
        this.combatStreak++;

        if (reach > 0 && reach < 10.0) {
            reachValues.add(reach);
        }

        if (previousState != null && currentState != null) {
            float yawDelta = Math.abs(currentState.yawDelta(previousState));
            float pitchDelta = Math.abs(currentState.pitchDelta(previousState));
            double gcd = calculateGCD(yawDelta, pitchDelta);
            if (gcd > 0) {
                gcdValues.add(gcd);
            }
        }
    }

    private void recalculateCps() {
        if (clickTimestamps.size() < 2) {
            cps = 0.0;
            clickVariance = 0.0;
            return;
        }

        long now = System.currentTimeMillis();
        long oneSecondAgo = now - 1000;

        int clicks = 0;
        for (Long ts : clickTimestamps.toList()) {
            if (ts >= oneSecondAgo) clicks++;
        }
        cps = clicks;

        if (clickTimestamps.size() >= 3) {
            var list = clickTimestamps.toList();
            double sum = 0;
            double sumSquared = 0;
            int intervals = 0;

            for (int i = 1; i < list.size(); i++) {
                if (list.get(i) >= oneSecondAgo) {
                    long interval = list.get(i) - list.get(i - 1);
                    sum += interval;
                    sumSquared += interval * interval;
                    intervals++;
                }
            }

            if (intervals > 1) {
                double mean = sum / intervals;
                double variance = (sumSquared / intervals) - (mean * mean);
                clickVariance = variance;
            }
        }
    }

    private double calculateGCD(float a, float b) {
        if (a <= 0 || b <= 0) return 0;
        double x = Math.abs(a);
        double y = Math.abs(b);
        double precision = 0.0001;
        while (y > precision) {
            double temp = y;
            y = x % y;
            x = temp;
        }
        return x;
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }

    public PlayerState getCurrentState() { return currentState; }
    public PlayerState getPreviousState() { return previousState; }
    public CircularBuffer<PlayerState> getStateHistory() { return stateHistory; }

    public java.util.List<Double> getYawDeltas() { return yawDeltas.toList(); }
    public java.util.List<Double> getPitchDeltas() { return pitchDeltas.toList(); }
    public java.util.List<Double> getReachValues() { return reachValues.toList(); }
    public java.util.List<Double> getSpeedValues() { return speedValues.toList(); }
    public java.util.List<Double> getStrafeDelta() { return strafeDeltas.toList(); }
    public java.util.List<Double> getGcdValues() { return gcdValues.toList(); }

    public double getCps() { return cps; }
    public double getClickVariance() { return clickVariance; }
    public double getJumpFrequency() { return jumpFrequency; }
    public int getSprintTicks() { return sprintTicks; }
    public int getTotalTicks() { return totalTicks; }

    public long getLastAttackTime() { return lastAttackTime; }
    public UUID getLastTargetUuid() { return lastTargetUuid; }
    public Location getLastSafeLocation() { return lastSafeLocation; }

    public boolean isInCombat() {
        return System.currentTimeMillis() - lastAttackTime < 3000;
    }

    public int getAirTicks() { return airTicks; }
    public int getGroundTicks() { return groundTicks; }

    public void clear() {
        stateHistory.clear();
        yawDeltas.clear();
        pitchDeltas.clear();
        reachValues.clear();
        speedValues.clear();
        strafeDeltas.clear();
        gcdValues.clear();
        clickTimestamps.clear();
    }
}