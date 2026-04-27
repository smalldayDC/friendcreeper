package com.smalldaydc.friendlycreeper.client.mixin;

import com.smalldaydc.friendlycreeper.FriendlyCreeperConfig;
import com.smalldaydc.friendlycreeper.ITamedCreeper;
import com.smalldaydc.friendlycreeper.client.IFriendlyCreeperRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.CreeperEntityRenderer;
import net.minecraft.client.render.entity.state.CreeperEntityRenderState;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(CreeperEntityRenderer.class)
public class MixinCreeperEntityRenderer {

    @Unique private static final ItemStack POPPY_STACK = new ItemStack(Items.POPPY);
    @Unique private static final ItemStack WITHER_ROSE_STACK = new ItemStack(Items.WITHER_ROSE);
    @Unique private static final float LOW_HEALTH_THRESHOLD = 0.25f;

    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void friendlycreeper$updateRenderState(CreeperEntity entity,
                                                    CreeperEntityRenderState state,
                                                    float tickDelta,
                                                    CallbackInfo ci) {
        ITamedCreeper tc = (ITamedCreeper) (Object) entity;
        IFriendlyCreeperRenderState fcState = (IFriendlyCreeperRenderState) state;
        fcState.friendlycreeper$setTamed(tc.friendlycreeper$isTamed());
        fcState.friendlycreeper$setSitting(tc.friendlycreeper$isSitting());

        if (tc.friendlycreeper$isTamed()) {
            FriendlyCreeperConfig config = FriendlyCreeperConfig.get();
            // Show wither rose when low health (if enabled), poppy otherwise
            ItemStack flowerStack = (config.witherRoseOnLowHealth
                    && entity.getHealth() / entity.getMaxHealth() < LOW_HEALTH_THRESHOLD)
                    ? WITHER_ROSE_STACK : POPPY_STACK;
            MinecraftClient.getInstance().getItemModelManager()
                    .updateForNonLivingEntity(fcState.friendlycreeper$getPoppyRenderState(),
                            flowerStack, ItemDisplayContext.GROUND, entity);
        } else {
            fcState.friendlycreeper$getPoppyRenderState().clear();
        }
    }
}
