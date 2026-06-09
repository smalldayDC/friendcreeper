package com.smalldaydc.friendcreeper.mixin;

import com.smalldaydc.friendcreeper.FriendCreeperConfig;
import com.smalldaydc.friendcreeper.ITamedCreeper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MixinMobEntity {

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void friendcreeper$preventTargeting(LivingEntity target, CallbackInfo ci) {
        // Prevent snow golems from targeting tamed creepers
        if ((Object) this instanceof SnowGolem && !FriendCreeperConfig.get().snowGolemAttack) {
            if (target instanceof Creeper creeper
                    && ((ITamedCreeper)(Object) creeper).friendcreeper$isTamed()) {
                ci.cancel();
                return;
            }
        }

        // Prevent untamed creepers from targeting gunpowder-holding players
        if ((Object) this instanceof Creeper creeper) {
            ITamedCreeper tc = (ITamedCreeper)(Object) creeper;
            if (!tc.friendcreeper$isTamed() && target instanceof Player player
                    && (player.getMainHandItem().is(Items.GUNPOWDER)
                        || player.getOffhandItem().is(Items.GUNPOWDER))) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void friendcreeper$onInteract(Player player, InteractionHand hand,
                                             CallbackInfoReturnable<InteractionResult> cir) {
        if (!((Object) this instanceof Creeper creeper)) return;

        ITamedCreeper tc = (ITamedCreeper)(Object) creeper;
        ItemStack stack = player.getItemInHand(hand);

        if (tc.friendcreeper$isTamed()) {
            // Only handle main hand for tamed interactions to avoid double-firing
            if (hand != InteractionHand.MAIN_HAND) return;

            // Gunpowder when hurt → heal (any player, like vanilla wolves)
            if (stack.is(Items.GUNPOWDER) && creeper.getHealth() < creeper.getMaxHealth()) {
                if (!player.level().isClientSide()) {
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                    creeper.heal(4.0f);
                    if (creeper.level() instanceof ServerLevel sw) {
                        sw.sendParticles(ParticleTypes.HEART,
                                creeper.getX(), creeper.getY(0.5), creeper.getZ(),
                                5, 0.4, 0.4, 0.4, 0.05);
                    }
                }
                cir.setReturnValue(InteractionResult.SUCCESS);
                return;
            }

            // Non-owner cannot do other interactions (sit/toggle)
            if (!player.getUUID().equals(tc.friendcreeper$getOwnerUUID())) return;

            // Sneak+right-click → toggle sit (not while in vehicle)
            if (player.isShiftKeyDown()) {
                if (!creeper.isPassenger() && !player.level().isClientSide()) tc.friendcreeper$toggleSit();
                cir.setReturnValue(InteractionResult.SUCCESS);
                return;
            }

            // Any other right-click → toggle sit (also prevents usable items from firing, not while in vehicle)
            if (!creeper.isPassenger() && !player.level().isClientSide()) tc.friendcreeper$toggleSit();
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }

        // Untamed: gunpowder → tame attempt (both hands allowed)
        if (!stack.is(Items.GUNPOWDER)) return;
        // If off-hand triggers but main hand also has gunpowder, skip to avoid double-firing
        if (hand == InteractionHand.OFF_HAND && player.getMainHandItem().is(Items.GUNPOWDER)) return;

        if (!player.level().isClientSide()) {
            if (!player.getAbilities().instabuild) stack.shrink(1);

            int attempts = tc.friendcreeper$getTameAttempts() + 1;
            boolean success = attempts >= 5 || creeper.getRandom().nextInt(3) == 0;

            if (success) {
                tc.friendcreeper$setTamed(true);
                tc.friendcreeper$setOwnerUUID(player.getUUID());
                tc.friendcreeper$setTameAttempts(0);
                creeper.setPersistenceRequired();
                if (creeper.level() instanceof ServerLevel sw) {
                    sw.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            creeper.getX(), creeper.getY(0.5), creeper.getZ(),
                            20, 0.5, 0.5, 0.5, 0.1);
                }
            } else {
                tc.friendcreeper$setTameAttempts(attempts);
                if (creeper.level() instanceof ServerLevel sw) {
                    sw.sendParticles(ParticleTypes.SMOKE,
                            creeper.getX(), creeper.getY(0.5), creeper.getZ(),
                            10, 0.3, 0.3, 0.3, 0.05);
                }
            }
        }
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}