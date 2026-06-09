package com.smalldaydc.friendcreeper.mixin;

import com.smalldaydc.friendcreeper.ITamedCreeper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SwellGoal.class)
public class MixinCreeperIgniteGoal {

    @Shadow private Creeper creeper;

    @Inject(method = "canUse", at = @At("RETURN"), cancellable = true)
    private void friendcreeper$canStart(CallbackInfoReturnable<Boolean> cir) {
        ITamedCreeper tc = (ITamedCreeper)(Object) creeper;
        if (!tc.friendcreeper$isTamed()) return;

        // If sitting — never ignite
        if (tc.friendcreeper$isSitting()) {
            cir.setReturnValue(false);
            return;
        }

        LivingEntity target = creeper.getTarget();

        // No valid target → never ignite (prevents explosion after target dies)
        if (target == null || !target.isAlive()) {
            cir.setReturnValue(false);
            return;
        }

        // Non-player threat nearby → ignite
        if (!(target instanceof Player)
                && creeper.distanceToSqr(target) < 9.0) {
            cir.setReturnValue(true);
        }
    }
}
