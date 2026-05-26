package com.smalldaydc.friendcreeper.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.smalldaydc.friendcreeper.FriendCreeperConfig;
import com.smalldaydc.friendcreeper.ITamedCreeper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    /**
     * For tamed creepers, wrap the loot consumer so vanilla gunpowder drops
     * are intercepted:
     *   - dropPoppy = true  → replace each gunpowder stack with at most one
     *                          poppy. The vanilla drop probability is
     *                          preserved (a rolled count of 0 stays empty
     *                          and is not dropped); when something would
     *                          drop, it is always exactly one poppy
     *                          regardless of looting.
     *   - dropPoppy = false → swallow gunpowder entirely (tamed creepers
     *                          drop no gunpowder).
     * Non-gunpowder drops (e.g. music discs from skeleton kills) always
     * pass through unchanged.
     */
    @WrapOperation(method = "dropLoot(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;ZLnet/minecraft/registry/RegistryKey;)V",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/entity/LivingEntity;generateLoot(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;ZLnet/minecraft/registry/RegistryKey;Ljava/util/function/Consumer;)V"))
    private void friendcreeper$swapGunpowderForPoppyOnTamedCreeper(LivingEntity self,
                                                                    ServerWorld world,
                                                                    DamageSource source,
                                                                    boolean causedByPlayer,
                                                                    RegistryKey<LootTable> key,
                                                                    Consumer<ItemStack> consumer,
                                                                    Operation<Void> original) {
        if (self instanceof CreeperEntity creeper
                && ((ITamedCreeper) (Object) creeper).friendcreeper$isTamed()) {
            Consumer<ItemStack> wrapped = stack -> {
                if (stack.isOf(Items.GUNPOWDER)) {
                    if (FriendCreeperConfig.get().dropPoppy) {
                        consumer.accept(new ItemStack(Items.POPPY, Math.min(1, stack.getCount())));
                    }
                    // dropPoppy = false → swallow the gunpowder stack entirely
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
