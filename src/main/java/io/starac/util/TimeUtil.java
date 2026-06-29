package io.starac.util;

public final class TimeUtil {

    private TimeUtil() {}

    public static final long TICK_MS = 50L;

    public static long now() {
        return System.currentTimeMillis();
    }

    public static long elapsed(long timestamp) {
        return now() - timestamp;
    }

    public static boolean hasExpired(long timestamp, long ms) {
        return elapsed(timestamp) >= ms;
    }

    public static long ticksToMs(int ticks) {
        return ticks * TICK_MS;
    }

    public static int msToTicks(long ms) {
        return (int) (ms / TICK_MS);
    }

    public static double ratePerSecond(long[] timestamps) {
        if (timestamps.length < 2) return 0;
        long span = timestamps[timestamps.length - 1] - timestamps[0];
        if (span <= 0) return 0;
        return (timestamps.length - 1) * 1000.0 / span;
    }

    public static double averageInterval(long[] timestamps) {
        if (timestamps.length < 2) return 0;
        long totalInterval = 0;
        for (int i = 1; i < timestamps.length; i++) {
            totalInterval += timestamps[i] - timestamps[i - 1];
        }
        return (double) totalInterval / (timestamps.length - 1);
    }
}