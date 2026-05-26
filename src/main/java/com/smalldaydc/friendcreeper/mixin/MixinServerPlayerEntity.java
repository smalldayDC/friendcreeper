package com.smalldaydc.friendcreeper.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.smalldaydc.friendcreeper.ITamedCreeper;
import net.minecraft.advancement.criterion.OnKilledCriterion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity {

    @WrapOperation(method = "updateKilledAdvancementCriterion",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/advancement/criterion/OnKilledCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;)V"))
    private void friendcreeper$skipKillCriterionForTamedCreeper(OnKilledCriterion criterion,
                                                                 ServerPlayerEntity player,
                                                                 Entity entityKilled,
                                                                 DamageSource damageSource,
                                                                 Operation<Void> original) {
        if (entityKilled instanceof CreeperEntity creeper
                && ((ITamedCreeper) (Object) creeper).friendcreeper$isTamed()) {
            return;
        }
        original.call(criterion, player, entityKilled, damageSource);
    }
}
