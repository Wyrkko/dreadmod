package com.dread.client;

import com.dread.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class DreadClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.STALKER, StalkerRenderer::new);
        EntityRendererRegistry.register(ModEntities.CRAWLER, CrawlerRenderer::new);
        EntityRendererRegistry.register(ModEntities.WATCHER, WatcherRenderer::new);
        EntityRendererRegistry.register(ModEntities.WAILER, WailerRenderer::new);
    }
}
