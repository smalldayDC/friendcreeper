package com.smalldaydc.friendcreeper.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.smalldaydc.friendcreeper.ITamedCreeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootTable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    /**
     * For tamed creepers, replace each gunpowder stack with at most one
     * poppy. The vanilla drop probability is preserved (a rolled count of
     * 0 stays empty and is not dropped); when something would drop, it is
     * always exactly one poppy regardless of looting. Non-gunpowder drops
     * (e.g. music discs from skeleton kills) pass through unchanged.
     */
    @WrapOperation(method = "dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;ZLnet/minecraft/resources/ResourceKey;)V",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/LivingEntity;dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;ZLnet/minecraft/resources/ResourceKey;Ljava/util/function/Consumer;)V"))
    private void friendcreeper$swapGunpowderForPoppyOnTamedCreeper(LivingEntity self,
                                                                    ServerLevel world,
                                                                    DamageSource source,
                                                                    boolean causedByPlayer,
                                                                    ResourceKey<LootTable> key,
                                                                    Consumer<ItemStack> consumer,
                                                                    Operation<Void> original) {
        if (self instanceof Creeper creeper
                && ((ITamedCreeper) (Object) creeper).friendcreeper$isTamed()) {
            Consumer<ItemStack> wrapped = stack -> {
                if (stack.is(Items.GUNPOWDER)) {
                    consumer.accept(new ItemStack(Items.POPPY, Math.min(1, stack.getCount())));
                } else {
                    consumer.accept(stack);
                }
            };
            original.call(self, world, source, causedByPlayer, key, wrapped);
            return;
        }
        original.call(self, world, source, causedByPlayer, key, consumer);
    }
}
