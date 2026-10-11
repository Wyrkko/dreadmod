package com.dread.client;

import com.dread.DreadMod;
import com.dread.entity.CrawlerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SpiderEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class CrawlerRenderer extends MobEntityRenderer<CrawlerEntity, SpiderEntityModel<CrawlerEntity>> {
    private static final Identifier TEXTURE = DreadMod.id("textures/entity/crawler.png");
    private static final Identifier EYES = DreadMod.id("textures/entity/crawler_eyes.png");

    public CrawlerRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new SpiderEntityModel<>(ctx.getPart(EntityModelLayers.SPIDER)), 0.8f);
        this.addFeature(new GlowEyesFeature<>(this, EYES));
    }

    @Override
    protected void scale(CrawlerEntity entity, MatrixStack matrices, float amount) {
        matrices.scale(1.15f, 1.15f, 1.15f);
    }

    @Override
    protected float getLyingAngle(CrawlerEntity entity) {
        return 180.0f;
    }

    @Override
    public Identifier getTexture(CrawlerEntity entity) {
        return TEXTURE;
    }
}
