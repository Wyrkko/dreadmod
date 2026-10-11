package com.dread;

import com.dread.entity.CrawlerEntity;
import com.dread.entity.StalkerEntity;
import com.dread.entity.WailerEntity;
import com.dread.entity.WatcherEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModEntities {
    public static final EntityType<StalkerEntity> STALKER = Registry.register(
            Registries.ENTITY_TYPE, DreadMod.id("stalker"),
            EntityType.Builder.create(StalkerEntity::new, SpawnGroup.MONSTER)
                    .setDimensions(0.6f, 2.0f).maxTrackingRange(10).build("stalker"));

    public static final EntityType<CrawlerEntity> CRAWLER = Registry.register(
            Registries.ENTITY_TYPE, DreadMod.id("crawler"),
            EntityType.Builder.create(CrawlerEntity::new, SpawnGroup.MONSTER)
                    .setDimensions(1.4f, 0.9f).maxTrackingRange(8).build("crawler"));

    public static final EntityType<WatcherEntity> WATCHER = Registry.register(
            Registries.ENTITY_TYPE, DreadMod.id("watcher"),
            EntityType.Builder.create(WatcherEntity::new, SpawnGroup.MONSTER)
                    .setDimensions(0.6f, 2.9f).maxTrackingRange(10).build("watcher"));

    public static final EntityType<WailerEntity> WAILER = Registry.register(
            Registries.ENTITY_TYPE, DreadMod.id("wailer"),
            EntityType.Builder.create(WailerEntity::new, SpawnGroup.MONSTER)
                    .setDimensions(0.6f, 1.95f).maxTrackingRange(10).build("wailer"));

    public static void init() {
        FabricDefaultAttributeRegistry.register(STALKER, StalkerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(CRAWLER, CrawlerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(WATCHER, WatcherEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(WAILER, WailerEntity.createAttributes());
    }
}
