package com.dread.event;

import com.dread.ModEntities;
import com.dread.util.DreadUtil;
import com.dread.util.Scheduler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Every scripted scare lives here. Add your own by writing a method and putting it in EVENTS. */
public final class DreadEvents {
    private DreadEvents() {}

    public interface Event {
        void run(ServerPlayerEntity player);
    }

    public static final Map<String, Event> EVENTS = new LinkedHashMap<>();
    public static final Map<String, EntityType<? extends MobEntity>> CREATURES = new LinkedHashMap<>();
    public static final List<String> MINOR =
            List.of("whispers", "footsteps", "knocking", "creeperhiss", "heartbeat", "message");

    private static final List<SoundEvent> WHISPER_SOUNDS = List.of(
            SoundEvents.ENTITY_VEX_AMBIENT, SoundEvents.ENTITY_GHAST_AMBIENT,
            SoundEvents.ENTITY_ZOMBIE_VILLAGER_AMBIENT, SoundEvents.ENTITY_PHANTOM_AMBIENT,
            SoundEvents.ENTITY_ELDER_GUARDIAN_AMBIENT, SoundEvents.ENTITY_WARDEN_AMBIENT,
            SoundEvents.ENTITY_ENDERMAN_STARE);

    private static final List<String> MESSAGES = List.of(
            "I can see you, %s.",
            "Don't turn around.",
            "You are not alone in here.",
            "Why did you leave me down there?",
            "%s... %s... %s...",
            "Look up.",
            "It's standing right behind you.",
            "Nice base. I'll be inside soon.",
            "Did you hear that?",
            "Stop digging. Stop digging. Stop digging.");

    private static final Set<Block> TORCHES =
            Set.of(Blocks.TORCH, Blocks.WALL_TORCH, Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH);

    static {
        EVENTS.put("whispers", DreadEvents::whispers);
        EVENTS.put("footsteps", DreadEvents::footsteps);
        EVENTS.put("knocking", DreadEvents::knocking);
        EVENTS.put("creeperhiss", DreadEvents::creeperHiss);
        EVENTS.put("heartbeat", DreadEvents::heartbeat);
        EVENTS.put("message", DreadEvents::message);
        EVENTS.put("lightsout", DreadEvents::lightsOut);
        EVENTS.put("sighting", DreadEvents::sighting);
        EVENTS.put("ambush", DreadEvents::ambush);
        EVENTS.put("jumpscare", DreadEvents::jumpscare);
        EVENTS.put("nightmare", DreadEvents::nightmare);
        EVENTS.put("bloodmoon", DreadEvents::bloodMoon);
        EVENTS.put("swarm", DreadEvents::swarm);

        CREATURES.put("stalker", ModEntities.STALKER);
        CREATURES.put("crawler", ModEntities.CRAWLER);
        CREATURES.put("watcher", ModEntities.WATCHER);
        CREATURES.put("wailer", ModEntities.WAILER);
    }

    public static boolean run(String name, ServerPlayerEntity p) {
        Event e = EVENTS.get(name);
        if (e == null) return false;
        e.run(p);
        return true;
    }

    // ------------------------------------------------------------------ helpers

    private static boolean alive(ServerPlayerEntity p) {
        return p.isAlive() && !p.isRemoved();
    }

    public static void play(ServerWorld w, double x, double y, double z, SoundEvent s, float vol, float pitch) {
        w.playSound(null, x, y, z, s, SoundCategory.AMBIENT, vol, pitch);
    }

    public static EntityType<? extends MobEntity> randomCreature(ServerPlayerEntity p) {
        List<EntityType<? extends MobEntity>> all = new ArrayList<>(CREATURES.values());
        return all.get(DreadUtil.world(p).getRandom().nextInt(all.size()));
    }

    public static MobEntity spawnNear(ServerPlayerEntity p, EntityType<? extends MobEntity> type, int min, int max) {
        ServerWorld w = DreadUtil.world(p);
        int h = (int) Math.ceil(type.getHeight());
        BlockPos spot = DreadUtil.findSpot(w, p.getBlockPos(), min, max, 24, h);
        if (spot == null) return null;
        MobEntity e = type.spawn(w, spot, SpawnReason.COMMAND);
        if (e != null) e.lookAtEntity(p, 360f, 360f);
        return e;
    }

    /** Spawns a creature at a sensible distance for its type. */
    public static boolean spawnCreature(ServerPlayerEntity p, EntityType<? extends MobEntity> type) {
        int min = 8, max = 18;
        if (type == ModEntities.WATCHER) { min = 24; max = 36; }
        else if (type == ModEntities.WAILER) { min = 16; max = 26; }
        else if (type == ModEntities.STALKER) { min = 14; max = 24; }
        return spawnNear(p, type, min, max) != null;
    }

    // ------------------------------------------------------------------ minor events

    public static void whispers(ServerPlayerEntity p) {
        for (int i = 0; i < 9; i++) {
            int delay = i * 18 + DreadUtil.world(p).getRandom().nextInt(10);
            Scheduler.later(delay, () -> {
                if (!alive(p)) return;
                ServerWorld w = DreadUtil.world(p);
                var r = w.getRandom();
                double ang = r.nextDouble() * Math.PI * 2;
                double d = 3 + r.nextDouble() * 5;
                SoundEvent s = WHISPER_SOUNDS.get(r.nextInt(WHISPER_SOUNDS.size()));
                play(w, p.getX() + Math.cos(ang) * d, p.getY() + 1, p.getZ() + Math.sin(ang) * d,
                        s, 0.7f, 0.5f + r.nextFloat() * 0.3f);
            });
        }
    }

    public static void footsteps(ServerPlayerEntity p) {
        int steps = 14;
        for (int i = 0; i < steps; i++) {
            final double dist = 14 - i * (11.0 / (steps - 1));
            Scheduler.later(i * 11, () -> {
                if (!alive(p)) return;
                ServerWorld w = DreadUtil.world(p);
                Vec3d pos = DreadUtil.behind(p, dist);
                BlockPos below = BlockPos.ofFloored(pos.x, p.getY() - 0.5, pos.z);
                BlockSoundGroup g = w.getBlockState(below).getSoundGroup();
                play(w, pos.x, p.getY(), pos.z, g.getStepSound(), 0.9f, 0.8f);
            });
        }
        // ...and then they simply stop.
        Scheduler.later(steps * 11 + 30, () -> {
            if (!alive(p)) return;
            ServerWorld w = DreadUtil.world(p);
            play(w, p.getX(), p.getY(), p.getZ(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, 1.0f, 1.0f);
        });
    }

    public static void knocking(ServerPlayerEntity p) {
        int[] times = {0, 10, 20, 80, 90, 100, 110};
        for (int t : times) {
            Scheduler.later(t, () -> {
                if (!alive(p)) return;
                ServerWorld w = DreadUtil.world(p);
                Vec3d pos = DreadUtil.front(p, 2.5);
                play(w, pos.x, p.getY() + 1, pos.z, SoundEvents.BLOCK_WOOD_HIT, 1.6f, 0.6f);
            });
        }
    }

    public static void creeperHiss(ServerPlayerEntity p) {
        ServerWorld w = DreadUtil.world(p);
        Vec3d pos = DreadUtil.behind(p, 2.0);
        play(w, pos.x, p.getY(), pos.z, SoundEvents.ENTITY_CREEPER_PRIMED, 1.0f, 1.0f);
    }

    public static void heartbeat(ServerPlayerEntity p) {
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 150, 0, false, false, false));
        int[] beats = {0, 24, 46, 66, 84, 100, 114, 126, 136, 144};
        for (int t : beats) {
            Scheduler.later(t, () -> {
                if (!alive(p)) return;
                ServerWorld w = DreadUtil.world(p);
                play(w, p.getX(), p.getY(), p.getZ(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, 1.3f, 1.0f);
            });
        }
    }

    public static void message(ServerPlayerEntity p) {
        var r = DreadUtil.world(p).getRandom();
        String name = p.getName().getString();
        String line = MESSAGES.get(r.nextInt(MESSAGES.size())).replace("%s", name);
        p.sendMessage(Text.literal("<???> " + line).formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
    }

    // ------------------------------------------------------------------ bigger events

    public static void lightsOut(ServerPlayerEntity p) {
        ServerWorld w = DreadUtil.world(p);
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 240, 0, false, false, false));
        play(w, p.getX(), p.getY(), p.getZ(), SoundEvents.ENTITY_WARDEN_HEARTBEAT, 1.0f, 0.8f);
        if (!DreadDirector.griefing) return;

        List<BlockPos> found = new ArrayList<>();
        BlockPos c = p.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(c.add(-14, -8, -14), c.add(14, 8, 14))) {
            if (found.size() >= 40) break;
            BlockState s = w.getBlockState(pos);
            if (TORCHES.contains(s.getBlock())) found.add(pos.toImmutable());
        }
        var r = w.getRandom();
        for (BlockPos pos : found) {
            Scheduler.later(5 + r.nextInt(70), () -> {
                if (TORCHES.contains(w.getBlockState(pos).getBlock())) {
                    w.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                    play(w, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            SoundEvents.BLOCK_FIRE_EXTINGUISH, 0.6f, 0.8f);
                }
            });
        }
    }

    public static void sighting(ServerPlayerEntity p) {
        ServerWorld w = DreadUtil.world(p);
        Vec3d f = DreadUtil.front(p, 28);
        BlockPos spot = DreadUtil.findSpot(w, BlockPos.ofFloored(f.x, p.getY(), f.z), 0, 8, 24, 3);
        if (spot == null) spot = DreadUtil.findSpot(w, p.getBlockPos(), 22, 34, 24, 3);
        if (spot == null) return;
        MobEntity e = ModEntities.WATCHER.spawn(w, spot, SpawnReason.COMMAND);
        if (e != null) e.lookAtEntity(p, 360f, 360f);
    }

    public static void ambush(ServerPlayerEntity p) {
        ServerWorld w = DreadUtil.world(p);
        play(w, p.getX(), p.getY(), p.getZ(), SoundEvents.ENTITY_SPIDER_AMBIENT, 1.2f, 0.5f);
        if (w.getRandom().nextBoolean()) {
            spawnCreature(p, ModEntities.STALKER);
        } else {
            int n = 2 + w.getRandom().nextInt(2);
            for (int i = 0; i < n; i++) spawnCreature(p, ModEntities.CRAWLER);
        }
    }

    public static void jumpscare(ServerPlayerEntity p) {
        ServerWorld w = DreadUtil.world(p);
        Vec3d f = DreadUtil.front(p, 3.0);
        BlockPos fp = BlockPos.ofFloored(f.x, p.getY(), f.z);
        if (!DreadUtil.roomFor(w, fp, 3)) {
            Vec3d b = DreadUtil.behind(p, 3.0);
            fp = BlockPos.ofFloored(b.x, p.getY(), b.z);
            if (!DreadUtil.roomFor(w, fp, 3)) return;
        }
        var watcher = ModEntities.WATCHER.spawn(w, fp, SpawnReason.COMMAND);
        if (watcher != null) {
            watcher.lookAtEntity(p, 360f, 360f);
            watcher.scare(p);
        }
    }

    public static void nightmare(ServerPlayerEntity p) {
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 400, 0, false, false, false));
        whispers(p);
        Scheduler.later(60, () -> { if (alive(p)) heartbeat(p); });
        Scheduler.later(200, () -> { if (alive(p)) footsteps(p); });
        Scheduler.later(380, () -> { if (alive(p)) lightsOut(p); });
        Scheduler.later(460, () -> { if (alive(p)) message(p); });
        Scheduler.later(520, () -> { if (alive(p)) spawnCreature(p, ModEntities.STALKER); });
        Scheduler.later(700, () -> { if (alive(p)) jumpscare(p); });
    }

    public static void swarm(ServerPlayerEntity p) {
        spawnCreature(p, ModEntities.STALKER);
        spawnCreature(p, ModEntities.CRAWLER);
        spawnCreature(p, ModEntities.CRAWLER);
        spawnCreature(p, ModEntities.WAILER);
        spawnCreature(p, ModEntities.WATCHER);
    }

    public static void bloodMoon(ServerPlayerEntity p) {
        MinecraftServer server = p.getServer();
        if (server == null || DreadDirector.bloodMoonTicks > 0) return;

        ServerWorld ow = server.getOverworld();
        long time = ow.getTimeOfDay();
        long tod = time % 24000;
        if (tod < 13000 || tod > 22000) {
            ow.setTimeOfDay(time - tod + (tod > 22000 ? 24000 : 0) + 14000);
        }
        ow.setWeather(0, 6000, true, true);
        DreadDirector.bloodMoonTicks = 2400;

        for (ServerPlayerEntity sp : server.getPlayerManager().getPlayerList()) {
            sp.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 70, 30));
            sp.networkHandler.sendPacket(new TitleS2CPacket(
                    Text.literal("THE BLOOD MOON RISES").formatted(Formatting.DARK_RED, Formatting.BOLD)));
            ServerWorld w = DreadUtil.world(sp);
            play(w, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 0.5f);
        }
    }
}
