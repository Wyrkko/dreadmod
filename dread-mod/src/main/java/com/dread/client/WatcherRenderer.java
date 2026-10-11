package com.dread.client;

import com.dread.DreadMod;
import com.dread.entity.WatcherEntity;
import net.minecraft.client.render.entity.model.EndermanEntityModel;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

public class WatcherRenderer extends MobEntityRenderer<WatcherEntity, EndermanEntityModel<WatcherEntity>> {
    private static final Identifier TEXTURE = DreadMod.id("textures/entity/watcher.png");
    private static final Identifier EYES = DreadMod.id("textures/entity/watcher_eyes.png");

    public WatcherRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new EndermanEntityModel<>(ctx.getPart(EntityModelLayers.ENDERMAN)), 0.5f);
        this.addFeature(new GlowEyesFeature<>(this, EYES));
    }

    @Override
    public Identifier getTexture(WatcherEntity entity) {
        return TEXTURE;
    }
}
