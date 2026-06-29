package io.starac.violation.manager;

import io.starac.util.TimeUtil;
import io.starac.violation.Violation;
import io.starac.violation.ViolationType;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class DecayableVL {

    public enum DecayStrategy {
        LINEAR,
        EXPONENTIAL,
        STEP
    }

    private final ViolationType type;
    private final AtomicInteger rawVL;
    private final AtomicLong lastFlagTime;
    private final AtomicLong lastDecayTime;
    private final int maxVL;
    private final DecayStrategy strategy;
    private final int decayIntervalTicks;
    private final double decayAmount;
    private final double decayFactor;
    private final long inactivityResetMs;

    public DecayableVL(ViolationType type, int maxVL, DecayStrategy strategy,
                       int decayIntervalTicks, double decayAmount, double decayFactor,
                       long inactivityResetMs) {
        this.type = type;
        this.maxVL = maxVL;
        this.strategy = strategy;
        this.decayIntervalTicks = decayIntervalTicks;
        this.decayAmount = decayAmount;
        this.decayFactor = decayFactor;
        this.inactivityResetMs = inactivityResetMs;
        this.rawVL = new AtomicInteger(0);
        this.lastFlagTime = new AtomicLong(0);
        this.lastDecayTime = new AtomicLong(TimeUtil.now());
    }

    public int addViolation(Violation violation) {
        if (isInactive()) {
            reset();
        }

        double weight = violation.calculateWeight();
        int increment = Math.max(1, (int) Math.round(weight));

        int newVL;
        int currentVL;
        do {
            currentVL = rawVL.get();
            newVL = Math.min(maxVL, currentVL + increment);
        } while (!rawVL.compareAndSet(currentVL, newVL));

        lastFlagTime.set(TimeUtil.now());
        return newVL;
    }

    public int applyDecay() {
        long now = TimeUtil.now();
        if (!shouldDecay(now)) return getVL();

        int currentVL = rawVL.get();
        if (currentVL <= 0) {
            lastDecayTime.set(now);
            return 0;
        }

        int newVL = switch (strategy) {
            case LINEAR -> Math.max(0, currentVL - (int) decayAmount);
            case EXPONENTIAL -> (int) Math.round(currentVL * decayFactor);
            case STEP -> computeStepDecay(currentVL);
        };

        rawVL.set(newVL);
        lastDecayTime.set(now);
        return newVL;
    }

    private int computeStepDecay(int currentVL) {
        if (currentVL > 15) return 15;
        if (currentVL > 10) return 10;
        if (currentVL > 5) return 5;
        return 0;
    }

    private boolean shouldDecay(long now) {
        long elapsed = now - lastDecayTime.get();
        return elapsed >= decayIntervalTicks * 50L;
    }

    public boolean isInactive() {
        long lastFlag = lastFlagTime.get();
        return lastFlag > 0 && TimeUtil.hasExpired(lastFlag, inactivityResetMs);
    }

    public void reset() {
        rawVL.set(0);
    }

    public int getVL() {
        return rawVL.get();
    }

    public int getMaxVL() {
        return maxVL;
    }

    public ViolationType getType() {
        return type;
    }

    public double getProgress() {
        return maxVL > 0 ? (double) getVL() / maxVL : 0.0;
    }

    public long getLastFlagTime() {
        return lastFlagTime.get();
    }

    @Override
    public String toString() {
        return String.format("%s: %d/%d (%.0f%%)",
                type.getName(), getVL(), maxVL, getProgress() * 100);
    }

    public static Builder builder(ViolationType type) {
        return new Builder(type);
    }

    public static final class Builder {
        private final ViolationType type;
        private int maxVL = 20;
        private DecayStrategy strategy = DecayStrategy.EXPONENTIAL;
        private int decayIntervalTicks = 1200;
        private double decayAmount = 1.0;
        private double decayFactor = 0.9;
        private long inactivityResetMs = 300_000L;

        public Builder(ViolationType type) {
            this.type = type;
            this.maxVL = type.getMaxVL();
        }

        public Builder maxVL(int maxVL) {
            this.maxVL = Math.max(1, maxVL);
            return this;
        }

        public Builder strategy(DecayStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public Builder decayIntervalTicks(int ticks) {
            this.decayIntervalTicks = Math.max(1, ticks);
            return this;
        }

        public Builder decayAmount(double amount) {
            this.decayAmount = Math.max(0.1, amount);
            return this;
        }

        public Builder decayFactor(double factor) {
            this.decayFactor = Math.max(0.1, Math.min(0.99, factor));
            return this;
        }

        public Builder inactivityResetMs(long ms) {
            this.inactivityResetMs = Math.max(1000, ms);
            return this;
        }

        public DecayableVL build() {
            return new DecayableVL(type, maxVL, strategy, decayIntervalTicks,
                    decayAmount, decayFactor, inactivityResetMs);
        }
    }
}