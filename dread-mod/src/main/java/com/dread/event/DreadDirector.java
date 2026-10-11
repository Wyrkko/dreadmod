package com.dread.event;

import com.dread.ModEntities;
import com.dread.entity.DreadMob;
import com.dread.util.DreadUtil;
import com.dread.util.Scheduler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** The "director": holds settings, runs the scheduler and occasionally decides to haunt you. */
public final class DreadDirector {
    private DreadDirector() {}

    public static boolean auto = false;
    public static int intensity = 2; // 1 (rare) .. 5 (relentless)
    public static boolean griefing = true; // lights-out may snuff nearby torches

    public static int bloodMoonTicks = 0;

    private static long counter = 0;
    private static long lastAutoEvent = -100000;

    public static void tick(MinecraftServer server) {
        Scheduler.tick();
        counter++;

        if (bloodMoonTicks > 0) {
            bloodMoonTicks--;
            if (bloodMoonTicks % 80 == 0) bloodMoonSpawn(server);
            if (bloodMoonTicks % 300 == 0) {
                for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                    if (p.isAlive() && !p.isSpectator()) DreadEvents.whispers(p);
                }
            }
            if (bloodMoonTicks == 0) {
                server.getPlayerManager().broadcast(
                        Text.literal("The red moon sets... for now.").formatted(Formatting.DARK_RED, Formatting.ITALIC),
                        false);
            }
        }

        if (auto && counter % 100 == 0) autoCheck(server);
    }

    private static void bloodMoonSpawn(MinecraftServer server) {
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (p.isSpectator() || !p.isAlive()) continue;
            ServerWorld w = DreadUtil.world(p);
            int near = w.getEntitiesByClass(MobEntity.class, p.getBoundingBox().expand(48),
                    e -> e instanceof DreadMob).size();
            if (near >= 6) continue;
            EntityType<? extends MobEntity> type;
            if (w.getRandom().nextInt(3) == 0) {
                type = ModEntities.STALKER;
            } else {
                type = ModEntities.CRAWLER;
            }
            DreadEvents.spawnCreature(p, type);
        }
    }

    private static void autoCheck(MinecraftServer server) {
        // minimum gap between automatic scares: shorter at higher intensity
        long minGap = 20L * (130 - intensity * 20);
        if (counter - lastAutoEvent < minGap) return;

        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (p.isSpectator() || !p.isAlive()) continue;
            ServerWorld w = DreadUtil.world(p);
            double chance = 0.012 * intensity;
            if (w.getLightLevel(p.getBlockPos()) < 6) chance *= 2;
            if (w.isNight()) chance *= 1.5;
            if (w.getRandom().nextDouble() > chance) continue;
            lastAutoEvent = counter;
            pickRandom(p);
            return;
        }
    }

    /** Picks a weighted-random event for this player. Also used by the Haunted Bell. */
    public static void pickRandom(ServerPlayerEntity p) {
        var r = DreadUtil.world(p).getRandom();
        double roll = r.nextDouble();
        String name;
        if (roll < 0.55) name = DreadEvents.MINOR.get(r.nextInt(DreadEvents.MINOR.size()));
        else if (roll < 0.72) name = "sighting";
        else if (roll < 0.84) name = "lightsout";
        else if (roll < 0.93) name = "ambush";
        else if (roll < 0.98) name = "jumpscare";
        else name = "nightmare";
        DreadEvents.run(name, p);
    }

    public static void reset() {
        Scheduler.clear();
        bloodMoonTicks = 0;
    }
}
