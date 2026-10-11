package com.dread.command;

import com.dread.entity.DreadMob;
import com.dread.event.DreadDirector;
import com.dread.event.DreadEvents;
import com.dread.util.Scheduler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class DreadCommands {
    private DreadCommands() {}

    public static void register(CommandDispatcher<ServerCommandSource> d) {
        d.register(CommandManager.literal("horror")
                .requires(src -> src.hasPermissionLevel(2))
                .executes(DreadCommands::help)
                .then(CommandManager.literal("help").executes(DreadCommands::help))
                .then(CommandManager.literal("spawn")
                        .then(CommandManager.argument("creature", StringArgumentType.word())
                                .suggests((c, b) -> CommandSource.suggestMatching(creatureNames(), b))
                                .executes(c -> spawn(c, 1))
                                .then(CommandManager.argument("count", IntegerArgumentType.integer(1, 20))
                                        .executes(c -> spawn(c, IntegerArgumentType.getInteger(c, "count"))))))
                .then(CommandManager.literal("event")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .suggests((c, b) -> CommandSource.suggestMatching(DreadEvents.EVENTS.keySet(), b))
                                .executes(c -> event(c, List.of(c.getSource().getPlayerOrThrow())))
                                .then(CommandManager.argument("targets", EntityArgumentType.players())
                                        .executes(c -> event(c, EntityArgumentType.getPlayers(c, "targets"))))))
                .then(CommandManager.literal("auto")
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(c -> {
                                    DreadDirector.auto = BoolArgumentType.getBool(c, "enabled");
                                    return say(c, "Automatic haunting is now " + (DreadDirector.auto ? "ON" : "OFF") + ".");
                                })))
                .then(CommandManager.literal("intensity")
                        .then(CommandManager.argument("level", IntegerArgumentType.integer(1, 5))
                                .executes(c -> {
                                    DreadDirector.intensity = IntegerArgumentType.getInteger(c, "level");
                                    return say(c, "Intensity set to " + DreadDirector.intensity + "/5.");
                                })))
                .then(CommandManager.literal("griefing")
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(c -> {
                                    DreadDirector.griefing = BoolArgumentType.getBool(c, "enabled");
                                    return say(c, "Torch snuffing is now " + (DreadDirector.griefing ? "ON" : "OFF") + ".");
                                })))
                .then(CommandManager.literal("clear").executes(DreadCommands::clear))
                .then(CommandManager.literal("status").executes(c -> say(c,
                        "auto=" + DreadDirector.auto + ", intensity=" + DreadDirector.intensity
                                + ", griefing=" + DreadDirector.griefing
                                + ", bloodmoon=" + (DreadDirector.bloodMoonTicks > 0
                                ? (DreadDirector.bloodMoonTicks / 20) + "s left" : "no")))));
    }

    private static List<String> creatureNames() {
        List<String> names = new ArrayList<>(DreadEvents.CREATURES.keySet());
        names.add("random");
        return names;
    }

    private static int say(CommandContext<ServerCommandSource> c, String msg) {
        c.getSource().sendFeedback(() -> Text.literal(msg), true);
        return 1;
    }

    private static int help(CommandContext<ServerCommandSource> c) {
        say(c, "/horror spawn <" + String.join("|", creatureNames()) + "> [count]");
        say(c, "/horror event <" + String.join("|", DreadEvents.EVENTS.keySet()) + "> [players]");
        say(c, "/horror auto <true|false>   /horror intensity <1-5>   /horror griefing <true|false>");
        say(c, "/horror clear   /horror status");
        return 1;
    }

    private static int spawn(CommandContext<ServerCommandSource> c, int count) throws CommandSyntaxException {
        ServerCommandSource src = c.getSource();
        ServerPlayerEntity p = src.getPlayerOrThrow();
        String name = StringArgumentType.getString(c, "creature").toLowerCase(Locale.ROOT);
        if (!name.equals("random") && !DreadEvents.CREATURES.containsKey(name)) {
            src.sendError(Text.literal("Unknown creature '" + name + "'. Try: " + String.join(", ", creatureNames())));
            return 0;
        }
        int spawned = 0;
        for (int i = 0; i < count; i++) {
            EntityType<? extends MobEntity> type =
                    name.equals("random") ? DreadEvents.randomCreature(p) : DreadEvents.CREATURES.get(name);
            if (DreadEvents.spawnCreature(p, type)) spawned++;
        }
        if (spawned == 0) {
            src.sendError(Text.literal("Couldn't find room to spawn anything nearby."));
            return 0;
        }
        final int n = spawned;
        src.sendFeedback(() -> Text.literal("Something stirs... (" + n + " spawned)"), true);
        return spawned;
    }

    private static int event(CommandContext<ServerCommandSource> c, Collection<ServerPlayerEntity> targets) {
        String name = StringArgumentType.getString(c, "name").toLowerCase(Locale.ROOT);
        if (!DreadEvents.EVENTS.containsKey(name)) {
            c.getSource().sendError(Text.literal("Unknown event '" + name + "'. Try: "
                    + String.join(", ", DreadEvents.EVENTS.keySet())));
            return 0;
        }
        for (ServerPlayerEntity p : targets) DreadEvents.run(name, p);
        final int n = targets.size();
        c.getSource().sendFeedback(() -> Text.literal("Event '" + name + "' unleashed on " + n + " player(s)."), true);
        return n;
    }

    private static int clear(CommandContext<ServerCommandSource> c) {
        Scheduler.clear();
        DreadDirector.bloodMoonTicks = 0;
        Set<MobEntity> found = new HashSet<>();
        for (ServerWorld w : c.getSource().getServer().getWorlds()) {
            for (ServerPlayerEntity p : w.getPlayers()) {
                found.addAll(w.getEntitiesByClass(MobEntity.class, p.getBoundingBox().expand(200),
                        e -> e instanceof DreadMob));
            }
        }
        for (MobEntity m : found) m.discard();
        final int n = found.size();
        c.getSource().sendFeedback(() -> Text.literal("The darkness recedes. Removed " + n + " creature(s)."), true);
        return n;
    }
}
