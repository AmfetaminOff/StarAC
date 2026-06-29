package io.starac.util.math;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class Geometry {

    private Geometry() {
    }

    public static double distance(Location first, Location second) {
        if (first == null || second == null) {
            return 0.0;
        }

        if (first.getWorld() != second.getWorld()) {
            return Double.MAX_VALUE;
        }

        return first.distance(second);
    }

    public static double horizontalDistance(Location first, Location second) {
        if (first == null || second == null) {
            return 0.0;
        }

        double dx = first.getX() - second.getX();
        double dz = first.getZ() - second.getZ();

        return Math.sqrt(dx * dx + dz * dz);
    }

    public static double verticalDistance(Location first, Location second) {
        if (first == null || second == null) {
            return 0.0;
        }

        return Math.abs(first.getY() - second.getY());
    }

    public static Vector direction(Location from, Location to) {
        return to.toVector().subtract(from.toVector()).normalize();
    }

    public static double angle(Vector first, Vector second) {
        double denominator = first.length() * second.length();

        if (denominator <= 1E-9) {
            return 0.0;
        }

        double dot = first.dot(second) / denominator;
        dot = Math.max(-1.0, Math.min(1.0, dot));

        return Math.toDegrees(Math.acos(dot));
    }

    public static double angleTo(Location eye, Location target) {
        return angle(
                eye.getDirection(),
                direction(eye, target)
        );
    }

    public static Vector project(Vector vector, Vector onto) {
        double length = onto.lengthSquared();

        if (length <= 1E-9) {
            return new Vector();
        }

        return onto.clone().multiply(vector.dot(onto) / length);
    }

    public static double dot(Vector first, Vector second) {
        return first.dot(second);
    }

    public static double length(Vector vector) {
        return vector.length();
    }

    public static double lengthSquared(Vector vector) {
        return vector.lengthSquared();
    }

}