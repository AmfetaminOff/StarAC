package io.starac.analysis;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public final class RayCasting {

    private RayCasting() {}

    public record RaycastResult(
            boolean hit,
            Entity hitEntity,
            Block hitBlock,
            Location hitLocation,
            double distance
    ) {
        public boolean hitEntity() {
            return hit && hitEntity != null;
        }

        public boolean hitBlock() {
            return hit && hitBlock != null;
        }

        public static RaycastResult empty() {
            return new RaycastResult(false, null, null, null, 0.0);
        }
    }

    public static RaycastResult cast(Player player, double maxDistance) {
        if (player == null || player.getWorld() == null) {
            return RaycastResult.empty();
        }

        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection();

        return cast(eye, direction, maxDistance, player.getWorld());
    }

    public static RaycastResult cast(Location start, Vector direction, double maxDistance, World world) {
        if (start == null || direction == null || world == null) {
            return RaycastResult.empty();
        }

        Vector dir = direction.clone().normalize();
        Location current = start.clone();
        double traveled = 0.0;
        double step = 0.1;

        while (traveled < maxDistance) {
            current.add(dir.clone().multiply(step));
            traveled += step;

            Block block = world.getBlockAt(current);
            if (block != null && block.getType().isSolid()) {
                return new RaycastResult(true, null, block, current.clone(), traveled);
            }

            for (Entity entity : world.getNearbyEntities(current, 0.5, 0.5, 0.5)) {
                if (entity != null && entity.isValid()) {
                    return new RaycastResult(true, entity, null, current.clone(), traveled);
                }
            }
        }

        return RaycastResult.empty();
    }

    public static boolean hasLineOfSight(Location from, Location to) {
        if (from == null || to == null || from.getWorld() != to.getWorld()) {
            return false;
        }

        Vector direction = to.toVector().subtract(from.toVector());
        double distance = direction.length();
        direction.normalize();

        RaycastResult result = cast(from, direction, distance, from.getWorld());
        return !result.hit();
    }

    public static boolean isInsideHitbox(Entity entity, Location point, double margin) {
        if (entity == null || point == null) return false;

        Location entityLoc = entity.getLocation();

        double width = 0.6 + margin;
        double height = 1.8 + margin;

        double halfWidth = width / 2.0;

        double dx = Math.abs(point.getX() - entityLoc.getX());
        double dy = point.getY() - entityLoc.getY();
        double dz = Math.abs(point.getZ() - entityLoc.getZ());

        return dx <= halfWidth && dy >= 0 && dy <= height && dz <= halfWidth;
    }

    public static boolean isValidReach(Player attacker, Player target, double reach, int ping, double tps) {
        if (attacker == null || target == null) return false;

        double maxVanillaReach = 3.0;

        double pingCompensation = (ping / 1000.0) * (tps / 20.0) * 0.5;

        double tpsCompensation = (20.0 - tps) * 0.05;

        double maxAllowedReach = maxVanillaReach + pingCompensation + tpsCompensation + 0.1;

        return reach <= maxAllowedReach;
    }

    public static double getAngleToTarget(Player player, Entity target) {
        if (player == null || target == null) return 0.0;

        Location eye = player.getEyeLocation();
        Location targetLoc = target.getLocation().add(0, target.getHeight() / 2, 0);

        Vector direction = targetLoc.toVector().subtract(eye.toVector()).normalize();
        Vector look = eye.getDirection();

        double dot = direction.dot(look);
        double angleRad = Math.acos(MathUtil.clamp(dot, -1.0, 1.0));
        return Math.toDegrees(angleRad);
    }

    public static boolean isLookingAt(Player player, Entity target, double threshold) {
        double angle = getAngleToTarget(player, target);
        return angle <= threshold;
    }

    public static RaycastResult castPrecise(Player player, double maxDistance) {
        if (player == null || player.getWorld() == null) {
            return RaycastResult.empty();
        }

        Location eye = player.getEyeLocation();
        BlockIterator iterator = new BlockIterator(player.getWorld(), eye.toVector(),
                eye.getDirection(), 0.0, (int) maxDistance);

        while (iterator.hasNext()) {
            Block block = iterator.next();

            if (block.getType().isSolid()) {
                return new RaycastResult(true, null, block, block.getLocation(),
                        eye.distance(block.getLocation()));
            }
        }

        return RaycastResult.empty();
    }
}