package com.dread;

import com.dread.item.HauntedBellItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

public class ModItems {
    public static final Item DREAD_SHARD = register("dread_shard", new Item(new Item.Settings()));
    public static final Item HAUNTED_BELL = register("haunted_bell",
            new HauntedBellItem(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));

    public static final Item STALKER_SPAWN_EGG = register("stalker_spawn_egg",
            new SpawnEggItem(ModEntities.STALKER, 0x9a9890, 0x120f10, new Item.Settings()));
    public static final Item CRAWLER_SPAWN_EGG = register("crawler_spawn_egg",
            new SpawnEggItem(ModEntities.CRAWLER, 0x1b1517, 0xb01818, new Item.Settings()));
    public static final Item WATCHER_SPAWN_EGG = register("watcher_spawn_egg",
            new SpawnEggItem(ModEntities.WATCHER, 0x0b0b0d, 0xe8e8e0, new Item.Settings()));
    public static final Item WAILER_SPAWN_EGG = register("wailer_spawn_egg",
            new SpawnEggItem(ModEntities.WAILER, 0x2e3c42, 0x78dcff, new Item.Settings()));

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, DreadMod.id(name), item);
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.add(STALKER_SPAWN_EGG);
            entries.add(CRAWLER_SPAWN_EGG);
            entries.add(WATCHER_SPAWN_EGG);
            entries.add(WAILER_SPAWN_EGG);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(DREAD_SHARD));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(HAUNTED_BELL));
    }
}
