package com.smalldaydc.friendlycreeper.mixin;

import com.smalldaydc.friendlycreeper.ITamedCreeper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public class MixinMobEntity {

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void friendlycreeper$onInteract(PlayerEntity player, Hand hand,
                                             CallbackInfoReturnable<ActionResult> cir) {
        // Only handle CreeperEntity
        if (!((Object) this instanceof CreeperEntity creeper)) return;
        // Only main hand to avoid double-firing
        if (hand != Hand.MAIN_HAND) return;
        if (player.getWorld().isClient()) return;

        ITamedCreeper tc = (ITamedCreeper)(Object) creeper;
        ItemStack stack = player.getMainHandStack();

        if (tc.friendlycreeper$isTamed()) {
            if (!player.getUuid().equals(tc.friendlycreeper$getOwnerUUID())) return;

            // Sneak+right-click → toggle sit
            if (player.isSneaking()) {
                tc.friendlycreeper$toggleSit();
                cir.setReturnValue(ActionResult.SUCCESS);
                return;
            }

            // Gunpowder when hurt → heal
            if (stack.isOf(Items.GUNPOWDER) && creeper.getHealth() < creeper.getMaxHealth()) {
                if (!player.getAbilities().creativeMode) stack.decrement(1);
                creeper.heal(4.0f);
                if (creeper.getWorld() instanceof ServerWorld sw) {
                    sw.spawnParticles(ParticleTypes.HEART,
                            creeper.getX(), creeper.getBodyY(0.5), creeper.getZ(),
                            5, 0.4, 0.4, 0.4, 0.05);
                }
                cir.setReturnValue(ActionResult.SUCCESS);
                return;
            }

            // Gunpowder at full HP or any other right-click → toggle sit
            tc.friendlycreeper$toggleSit();
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        // Untamed: gunpowder → tame attempt
        if (!stack.isOf(Items.GUNPOWDER)) return;
        if (!player.getAbilities().creativeMode) stack.decrement(1);

        int attempts = tc.friendlycreeper$getTameAttempts() + 1;
        boolean success = attempts >= 5 || creeper.getRandom().nextInt(3) == 0;

        if (success) {
            tc.friendlycreeper$setTamed(true);
            tc.friendlycreeper$setOwnerUUID(player.getUuid());
            tc.friendlycreeper$setTameAttempts(0);
            creeper.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.POPPY));
            creeper.setPersistent();
            if (creeper.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.HAPPY_VILLAGER,
                        creeper.getX(), creeper.getBodyY(0.5), creeper.getZ(),
                        20, 0.5, 0.5, 0.5, 0.1);
            }
        } else {
            tc.friendlycreeper$setTameAttempts(attempts);
            if (creeper.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.SMOKE,
                        creeper.getX(), creeper.getBodyY(0.5), creeper.getZ(),
                        10, 0.3, 0.3, 0.3, 0.05);
            }
        }
        cir.setReturnValue(ActionResult.SUCCESS);
    }
}
