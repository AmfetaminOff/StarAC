package io.starac.analysis.rotation.window;

public final class RotationSample {

    private final float yaw;
    private final float pitch;

    private final float deltaYaw;
    private final float deltaPitch;

    private final float accelerationYaw;
    private final float accelerationPitch;

    private final long timestamp;

    public RotationSample(
            float yaw,
            float pitch,
            float deltaYaw,
            float deltaPitch,
            float accelerationYaw,
            float accelerationPitch,
            long timestamp
    ) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.deltaYaw = deltaYaw;
        this.deltaPitch = deltaPitch;
        this.accelerationYaw = accelerationYaw;
        this.accelerationPitch = accelerationPitch;
        this.timestamp = timestamp;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getDeltaYaw() {
        return deltaYaw;
    }

    public float getDeltaPitch() {
        return deltaPitch;
    }

    public float getAccelerationYaw() {
        return accelerationYaw;
    }

    public float getAccelerationPitch() {
        return accelerationPitch;
    }

    public long getTimestamp() {
        return timestamp;
    }

}