package com.dread.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

/** Draws a fullbright "eyes" texture on top of a mob, so its eyes glow in the dark. */
public class GlowEyesFeature<T extends Entity, M extends EntityModel<T>> extends EyesFeatureRenderer<T, M> {
    private final RenderLayer layer;

    public GlowEyesFeature(FeatureRendererContext<T, M> context, Identifier texture) {
        super(context);
        this.layer = RenderLayer.getEyes(texture);
    }

    @Override
    public RenderLayer getEyesTexture() {
        return layer;
    }
}
