package com.dread.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class DreadUtil {
    private DreadUtil() {}

    public static ServerWorld world(ServerPlayerEntity p) {
        return (ServerWorld) p.getWorld();
    }

    /** True if the player's crosshair is roughly pointed at the entity and they have line of sight. */
    public static boolean isLookingAt(PlayerEntity p, Entity e, double cos) {
        Vec3d look = p.getRotationVec(1.0F).normalize();
        Vec3d to = new Vec3d(e.getX() - p.getX(), e.getEyeY() - p.getEyeY(), e.getZ() - p.getZ());
        double len = to.length();
        if (len < 0.001) return true;
        return look.dotProduct(to.multiply(1.0 / len)) > cos && p.canSee(e);
    }

    public static boolean anyPlayerLooking(Entity e, double range, double cos) {
        for (PlayerEntity p : e.getWorld().getPlayers()) {
            if (p.isSpectator() || !p.isAlive()) continue;
            if (p.squaredDistanceTo(e) > range * range) continue;
            if (isLookingAt(p, e, cos)) return true;
        }
        return false;
    }

    public static Vec3d front(PlayerEntity p, double d) {
        double yaw = Math.toRadians(p.getYaw());
        return p.getPos().add(-Math.sin(yaw) * d, 0, Math.cos(yaw) * d);
    }

    public static Vec3d behind(PlayerEntity p, double d) {
        return front(p, -d);
    }

    public static boolean roomFor(World w, BlockPos pos, int height) {
        for (int i = 0; i < height; i++) {
            if (!w.getBlockState(pos.up(i)).isAir()) return false;
        }
        return true;
    }

    /** Finds solid ground with headroom at a random spot between minR and maxR blocks from center. */
    public static BlockPos findSpot(ServerWorld w, BlockPos center, int minR, int maxR, int tries, int height) {
        var r = w.getRandom();
        for (int i = 0; i < tries; i++) {
            double ang = r.nextDouble() * Math.PI * 2;
            double dist = minR + r.nextDouble() * Math.max(1, maxR - minR);
            int x = center.getX() + (int) Math.round(Math.cos(ang) * dist);
            int z = center.getZ() + (int) Math.round(Math.sin(ang) * dist);
            if (w.getChunkManager().getWorldChunk(x >> 4, z >> 4) == null) continue;
            for (int dy = 8; dy >= -8; dy--) {
                BlockPos pos = new BlockPos(x, center.getY() + dy, z);
                BlockPos below = pos.down();
                if (w.getBlockState(below).isSolidBlock(w, below) && roomFor(w, pos, height)) {
                    return pos;
                }
            }
        }
        return null;
    }
}
