package com.dread.entity;

import com.dread.util.DreadUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;

/** Hunts you relentlessly, but freezes solid while you are looking at it. Never look away. */
public class StalkerEntity extends HostileEntity implements DreadMob {
    private boolean frozen;

    public StalkerEntity(EntityType<? extends StalkerEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 10;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.34)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new FrozenGoal(this));
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 0.7));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 32.0f));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
    }

    public boolean isFrozenByGaze() {
        return frozen;
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        frozen = false;
        LivingEntity target = getTarget();
        if (target == null) return;

        frozen = DreadUtil.anyPlayerLooking(this, 80, 0.88);
        if (frozen) {
            getNavigation().stop();
            return;
        }

        // Out of sight and far away? Slip closer while nobody is watching.
        if (squaredDistanceTo(target) > 18 * 18 && random.nextInt(140) == 0 && getWorld() instanceof ServerWorld sw) {
            BlockPos spot = DreadUtil.findSpot(sw, target.getBlockPos(), 9, 13, 12, 2);
            if (spot != null) {
                Vec3d p = Vec3d.ofBottomCenter(spot);
                refreshPositionAndAngles(p.x, p.y, p.z, getYaw(), 0);
                getNavigation().stop();
            }
        }
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (frozen) {
            setVelocity(0, getVelocity().y, 0);
            super.travel(new Vec3d(0, movementInput.y, 0));
            setVelocity(0, getVelocity().y, 0);
            return;
        }
        super.travel(movementInput);
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);
        if (hit && target instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 120, 0), this);
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 1), this);
            playSound(SoundEvents.ENTITY_ENDERMAN_SCREAM, 1.0f, 0.4f);
        }
        return hit;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return frozen ? null : SoundEvents.ENTITY_ZOMBIE_VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.entity.damage.DamageSource source) {
        return SoundEvents.ENTITY_ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOMBIE_DEATH;
    }

    @Override
    public float getSoundPitch() {
        return 0.5f;
    }

    /** Stands perfectly still while any player looks at the stalker. */
    static class FrozenGoal extends Goal {
        private final StalkerEntity mob;

        FrozenGoal(StalkerEntity mob) {
            this.mob = mob;
            setControls(EnumSet.of(Control.MOVE, Control.JUMP));
        }

        @Override
        public boolean canStart() {
            return mob.frozen;
        }

        @Override
        public boolean shouldContinue() {
            return mob.frozen;
        }

        @Override
        public void start() {
            mob.getNavigation().stop();
        }
    }
}
