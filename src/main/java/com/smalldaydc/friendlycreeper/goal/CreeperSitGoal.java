package com.smalldaydc.friendlycreeper.goal;

import com.smalldaydc.friendlycreeper.ITamedCreeper;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.CreeperEntity;

import java.util.EnumSet;

public class CreeperSitGoal extends Goal {

    private final CreeperEntity creeper;

    public CreeperSitGoal(CreeperEntity creeper) {
        this.creeper = creeper;
        this.setControls(EnumSet.of(Control.MOVE, Control.JUMP));
    }

    @Override
    public boolean canStart() {
        return ((ITamedCreeper)(Object) creeper).friendlycreeper$isSitting();
    }

    @Override
    public boolean shouldContinue() {
        return ((ITamedCreeper)(Object) creeper).friendlycreeper$isSitting();
    }
}
