package com.dread.client;

import com.dread.DreadMod;
import com.dread.entity.WailerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class WailerRenderer extends MobEntityRenderer<WailerEntity, ZombieEntityModel<WailerEntity>> {
    private static final Identifier TEXTURE = DreadMod.id("textures/entity/wailer.png");
    private static final Identifier EYES = DreadMod.id("textures/entity/wailer_eyes.png");

    public WailerRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.0f);
        this.addFeature(new GlowEyesFeature<>(this, EYES));
    }

    @Override
    protected void scale(WailerEntity entity, MatrixStack matrices, float amount) {
        matrices.scale(1.05f, 1.1f, 1.05f);
    }

    @Override
    public Identifier getTexture(WailerEntity entity) {
        return TEXTURE;
    }
}
