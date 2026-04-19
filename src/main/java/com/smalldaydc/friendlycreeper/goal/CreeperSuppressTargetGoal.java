package com.smalldaydc.friendlycreeper.goal;

import com.smalldaydc.friendlycreeper.ITamedCreeper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;

import java.util.EnumSet;
import java.util.UUID;

public class CreeperSuppressTargetGoal extends Goal {

    private final CreeperEntity creeper;

    public CreeperSuppressTargetGoal(CreeperEntity creeper) {
        this.creeper = creeper;
        this.setControls(EnumSet.of(Control.TARGET));
    }

    private ITamedCreeper asTamed() {
        return (ITamedCreeper)(Object) creeper;
    }

    @Override
    public boolean canStart() {
        // Always active when tamed — must hold TARGET to block vanilla goals
        if (asTamed().friendlycreeper$isTamed()) return true;

        // For untamed creepers: suppress when nearby player holds gunpowder
        if (!creeper.getWorld().isClient()) {
            PlayerEntity nearest = creeper.getWorld().getClosestPlayer(creeper, 16.0);
            if (nearest != null && isHoldingGunpowder(nearest)) return true;
        }
        return false;
    }

    @Override
    public boolean shouldContinue() {
        return canStart();
    }

    @Override
    public void tick() {
        LivingEntity target = creeper.getTarget();
        if (target == null) return;

        // Sitting: clear all targets
        if (asTamed().friendlycreeper$isSitting()) {
            creeper.setTarget(null);
            return;
        }

        // Non-player target (mob attacking owner or creeper itself): always keep
        if (!(target instanceof PlayerEntity)) return;

        // Player target: only keep if it's the avenge target
        UUID avengeUUID = asTamed().friendlycreeper$getAvengeTargetUUID();
        if (avengeUUID != null && avengeUUID.equals(target.getUuid())) return;

        // All other player targets: clear
        creeper.setTarget(null);
    }

    private boolean isHoldingGunpowder(PlayerEntity player) {
        return player.getMainHandStack().isOf(Items.GUNPOWDER)
                || player.getOffHandStack().isOf(Items.GUNPOWDER);
    }
}
