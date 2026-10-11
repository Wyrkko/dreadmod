package com.dread.item;

import com.dread.event.DreadDirector;
import com.dread.util.Scheduler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Ring it and something answers. Right-click to trigger a random horror event on yourself. */
public class HauntedBellItem extends Item {
    public HauntedBellItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && user instanceof ServerPlayerEntity sp) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_BELL_USE,
                    SoundCategory.PLAYERS, 1.5f, 0.5f);
            Scheduler.later(30, () -> {
                if (sp.isAlive() && !sp.isRemoved()) DreadDirector.pickRandom(sp);
            });
            user.getItemCooldownManager().set(this, 20 * 45);
        }
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
