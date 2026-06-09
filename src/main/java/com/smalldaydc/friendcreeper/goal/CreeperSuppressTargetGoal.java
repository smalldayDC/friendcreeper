package com.smalldaydc.friendcreeper.goal;

import com.smalldaydc.friendcreeper.ITamedCreeper;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;

public class CreeperSuppressTargetGoal extends Goal {

    private final Creeper creeper;

    public CreeperSuppressTargetGoal(Creeper creeper) {
        this.creeper = creeper;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    private ITamedCreeper asTamed() {
        return (ITamedCreeper)(Object) creeper;
    }

    @Override
    public boolean canUse() {
        return asTamed().friendcreeper$isTamed();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        LivingEntity target = creeper.getTarget();
        if (target == null) return;

        // Keep non-player targets (mobs for defending owner/self)
        if (!(target instanceof Player)) return;

        // Keep avenge target
        UUID avengeUUID = asTamed().friendcreeper$getAvengeTargetUUID();
        if (avengeUUID != null && avengeUUID.equals(target.getUUID())) return;

        // Clear all other player targets
        creeper.setTarget(null);
    }
}
