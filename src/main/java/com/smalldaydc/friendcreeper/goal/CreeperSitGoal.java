package com.smalldaydc.friendcreeper.goal;

import com.smalldaydc.friendcreeper.ITamedCreeper;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;

public class CreeperSitGoal extends Goal {

    private final Creeper creeper;

    public CreeperSitGoal(Creeper creeper) {
        this.creeper = creeper;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return ((ITamedCreeper)(Object) creeper).friendcreeper$isSitting();
    }

    @Override
    public boolean canContinueToUse() {
        return ((ITamedCreeper)(Object) creeper).friendcreeper$isSitting();
    }
}
