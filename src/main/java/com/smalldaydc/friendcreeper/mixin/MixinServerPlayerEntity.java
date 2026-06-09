package com.smalldaydc.friendcreeper.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.smalldaydc.friendcreeper.ITamedCreeper;
import net.minecraft.advancements.criterion.KilledTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayerEntity {

    @WrapOperation(method = "awardKillScore",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/advancements/criterion/KilledTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void friendcreeper$skipKillCriterionForTamedCreeper(KilledTrigger criterion,
                                                                 ServerPlayer player,
                                                                 Entity entityKilled,
                                                                 DamageSource damageSource,
                                                                 Operation<Void> original) {
        if (entityKilled instanceof Creeper creeper
                && ((ITamedCreeper) (Object) creeper).friendcreeper$isTamed()) {
            return;
        }
        original.call(criterion, player, entityKilled, damageSource);
    }
}
