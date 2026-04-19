package com.smalldaydc.friendlycreeper.goal;

import com.smalldaydc.friendlycreeper.ITamedCreeper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.EnumSet;

/**
 * Keeps the creeper's current non-player target alive so vanilla
 * MeleeAttackGoal + CreeperIgniteGoal can do their jobs.
 * Only holds TARGET control — MOVE is left free for CreeperIgniteGoal.
 */
public class CreeperDefendOwnerGoal extends Goal {

    private final CreeperEntity creeper;

    public CreeperDefendOwnerGoal(CreeperEntity creeper) {
        this.creeper = creeper;
        // Only TARGET — never block MOVE so CreeperIgniteGoal can run
        this.setControls(EnumSet.of(Control.TARGET));
    }

    @Override
    public boolean canStart() {
        if (!((ITamedCreeper)(Object)creeper).friendlycreeper$isTamed()) return false;
        if (((ITamedCreeper)(Object)creeper).friendlycreeper$isSitting()) return false;
        LivingEntity target = creeper.getTarget();
        return target != null && !(target instanceof PlayerEntity) && !target.isDead();
    }

    @Override
    public boolean shouldContinue() {
        LivingEntity target = creeper.getTarget();
        return target != null && !target.isDead() && !creeper.isDead();
    }
}
