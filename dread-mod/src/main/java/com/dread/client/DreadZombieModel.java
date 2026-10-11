package com.dread.client;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AbstractZombieModel;
import net.minecraft.entity.mob.HostileEntity;

/** Zombie-style biped model (arms raise while attacking) that works for any hostile mob. */
public class DreadZombieModel<T extends HostileEntity> extends AbstractZombieModel<T> {
    public DreadZombieModel(ModelPart root) {
        super(root);
    }

    @Override
    public boolean isAttacking(T entity) {
        return entity.isAttacking();
    }
}
