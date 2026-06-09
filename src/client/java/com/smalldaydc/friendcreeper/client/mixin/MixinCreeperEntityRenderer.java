package com.smalldaydc.friendcreeper.client.mixin;

import com.smalldaydc.friendcreeper.FriendCreeperConfig;
import com.smalldaydc.friendcreeper.FriendCreeperMod;
import com.smalldaydc.friendcreeper.ITamedCreeper;
import com.smalldaydc.friendcreeper.client.IFriendCreeperRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(CreeperRenderer.class)
public class MixinCreeperEntityRenderer {

    @Unique private static final ItemStack POPPY_STACK = new ItemStack(Items.POPPY);
    @Unique private static final ItemStack WITHER_ROSE_STACK = new ItemStack(Items.WITHER_ROSE);
    @Unique private static final Identifier HAPPY_TEXTURE = Identifier.fromNamespaceAndPath("friendcreeper", "textures/entity/creeper/happy.png");
    @Unique private static final Identifier SAD_TEXTURE = Identifier.fromNamespaceAndPath("friendcreeper", "textures/entity/creeper/sad.png");

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void friendcreeper$updateRenderState(Creeper entity,
                                                    CreeperRenderState state,
                                                    float tickDelta,
                                                    CallbackInfo ci) {
        ITamedCreeper tc = (ITamedCreeper) (Object) entity;
        IFriendCreeperRenderState fcState = (IFriendCreeperRenderState) state;
        fcState.friendcreeper$setTamed(tc.friendcreeper$isTamed());
        fcState.friendcreeper$setSitting(tc.friendcreeper$isSitting());

        boolean lowHealth = entity.getHealth() / entity.getMaxHealth() < FriendCreeperMod.LOW_HEALTH_THRESHOLD;
        fcState.friendcreeper$setLowHealth(lowHealth);
        fcState.friendcreeper$setHasTarget(tc.friendcreeper$hasTarget());
        fcState.friendcreeper$setFleeing(tc.friendcreeper$isFleeing());

        if (tc.friendcreeper$isTamed()) {
            FriendCreeperConfig config = FriendCreeperConfig.get();
            // Show wither rose when low health (if enabled), poppy otherwise
            ItemStack flowerStack = (config.witherRoseOnLowHealth && lowHealth)
                    ? WITHER_ROSE_STACK : POPPY_STACK;
            Minecraft.getInstance().getItemModelResolver()
                    .updateForNonLiving(fcState.friendcreeper$getPoppyRenderState(),
                            flowerStack, ItemDisplayContext.GROUND, entity);

            // Update fish render state
            ItemStack heldFish = tc.friendcreeper$getHeldFish();
            if (!heldFish.isEmpty()) {
                Minecraft.getInstance().getItemModelResolver()
                        .updateForNonLiving(fcState.friendcreeper$getFishRenderState(),
                                heldFish, ItemDisplayContext.GROUND, entity);
            } else {
                fcState.friendcreeper$getFishRenderState().clear();
            }
        } else {
            fcState.friendcreeper$getPoppyRenderState().clear();
            fcState.friendcreeper$getFishRenderState().clear();
        }
    }

    @Inject(method = "getTextureLocation", at = @At("RETURN"), cancellable = true)
    private void friendcreeper$getTexture(CreeperRenderState state,
                                           CallbackInfoReturnable<Identifier> cir) {
        IFriendCreeperRenderState fcState = (IFriendCreeperRenderState) state;
        if (!fcState.friendcreeper$isTamed()) return;
        if (!FriendCreeperConfig.get().tamedCreeperTexture) return;
        // Revert to vanilla face when targeting an enemy
        if (fcState.friendcreeper$hasTarget()) return;

        // Show sad face when fleeing from cats (if enabled)
        if (fcState.friendcreeper$isFleeing() && FriendCreeperConfig.get().scaredFace) {
            cir.setReturnValue(SAD_TEXTURE);
            return;
        }

        cir.setReturnValue(fcState.friendcreeper$isLowHealth() ? SAD_TEXTURE : HAPPY_TEXTURE);
    }
}
