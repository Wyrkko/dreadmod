package com.dread.client;

import com.dread.DreadMod;
import com.dread.entity.StalkerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class StalkerRenderer extends MobEntityRenderer<StalkerEntity, ZombieEntityModel<StalkerEntity>> {
    private static final Identifier TEXTURE = DreadMod.id("textures/entity/stalker.png");
    private static final Identifier EYES = DreadMod.id("textures/entity/stalker_eyes.png");

    public StalkerRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.5f);
        this.addFeature(new GlowEyesFeature<>(this, EYES));
    }

    @Override
    protected void scale(StalkerEntity entity, MatrixStack matrices, float amount) {
        matrices.scale(0.9f, 1.12f, 0.9f); // tall and gaunt
    }

    @Override
    public Identifier getTexture(StalkerEntity entity) {
        return TEXTURE;
    }
}
