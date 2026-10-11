package com.dread.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
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
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** A floating ghost that drifts through walls toward you, wailing and draining the light. */
public class WailerEntity extends HostileEntity implements DreadMob {
    private int attackCooldown = 0;
    private int wailTimer = 60;

    public WailerEntity(EntityType<? extends WailerEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
        this.experiencePoints = 12;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 24.0f));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
    }

    @Override
    public void tick() {
        this.noClip = true;
        super.tick();
        this.noClip = false;
        this.setNoGravity(true);

        if (getWorld() instanceof ServerWorld sw && age % 4 == 0) {
            sw.spawnParticles(ParticleTypes.SOUL, getX(), getY() + 1.0, getZ(), 1, 0.2, 0.4, 0.2, 0.01);
        }
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if (attackCooldown > 0) attackCooldown--;

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) {
            setAttacking(false);
            setVelocity(getVelocity().multiply(0.8));
            return;
        }
        setAttacking(true);
        getLookControl().lookAt(target, 30f, 30f);

        Vec3d aim = target.getPos().add(0, 0.8, 0);
        Vec3d dir = aim.subtract(getPos());
        double dist = dir.length();
        if (dist > 1.2) {
            setVelocity(dir.normalize().multiply(0.075));
        } else {
            setVelocity(Vec3d.ZERO);
        }

        if (dist < 1.9 && attackCooldown <= 0) {
            target.damage(getDamageSources().mobAttack(this), 4.0f);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 80, 0), this);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1), this);
            attackCooldown = 40;
        }

        if (--wailTimer <= 0) {
            wailTimer = 100 + random.nextInt(100);
            playSound(SoundEvents.ENTITY_GHAST_AMBIENT, 1.6f, 0.5f);
            for (PlayerEntity p : getWorld().getEntitiesByClass(PlayerEntity.class,
                    getBoundingBox().expand(10), PlayerEntity::isAlive)) {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 60, 0));
            }
        }
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_VEX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_VEX_DEATH;
    }

    @Override
    public float getSoundPitch() {
        return 0.5f;
    }
}
