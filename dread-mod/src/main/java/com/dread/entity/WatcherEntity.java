package com.dread.entity;

import com.dread.util.DreadUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** A tall, silent figure that just stands and stares. Get close, stare back, or hit it, and it shows you why you shouldn't. */
public class WatcherEntity extends HostileEntity implements DreadMob {
    private int stareTicks = 0;
    private int vanishTimer = -1;
    private int lifetime = 3600;

    public WatcherEntity(EntityType<? extends WatcherEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 64.0f));
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) return;

        if (vanishTimer >= 0) {
            if (--vanishTimer <= 0) vanish();
            return;
        }
        if (--lifetime <= 0) {
            vanish();
            return;
        }

        PlayerEntity near = null;
        PlayerEntity starer = null;
        for (PlayerEntity p : getWorld().getPlayers()) {
            if (p.isSpectator() || !p.isAlive()) continue;
            double d = p.distanceTo(this);
            if (d > 64) continue;
            if (d < 10) near = p;
            if (DreadUtil.isLookingAt(p, this, 0.96)) starer = p;
        }

        stareTicks = starer != null ? stareTicks + 1 : Math.max(0, stareTicks - 2);

        if (near != null) {
            scare(near);
        } else if (stareTicks > 100 && starer != null) {
            // it was only waiting for you to notice it
            Vec3d f = DreadUtil.front(starer, 3.0);
            BlockPos fp = BlockPos.ofFloored(f.x, starer.getY(), f.z);
            if (DreadUtil.roomFor(getWorld(), fp, 3)) {
                float yaw = starer.getYaw() + 180f;
                refreshPositionAndAngles(f.x, starer.getY(), f.z, yaw, 0);
                setHeadYaw(yaw);
                setBodyYaw(yaw);
            }
            scare(starer);
        }
    }

    public void scare(PlayerEntity p) {
        if (vanishTimer >= 0) return;
        vanishTimer = 12;
        getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE,
                SoundCategory.HOSTILE, 1.5f, 0.6f);
        getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.ENTITY_ENDERMAN_SCREAM,
                SoundCategory.HOSTILE, 1.4f, 0.3f);
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 100, 0));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 15, 0));
    }

    private void vanish() {
        if (getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.5, getZ(), 30, 0.3, 1.0, 0.3, 0.01);
        }
        discard();
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!getWorld().isClient && source.getAttacker() instanceof PlayerEntity p) {
            scare(p);
        }
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }
}
