package com.smalldaydc.friendlycreeper.client;

import com.smalldaydc.friendlycreeper.ITamedCreeper;
import com.smalldaydc.friendlycreeper.client.render.CreeperPoppyFeature;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.CreeperEntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class FriendlyCreeperClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Prevent item use when right-clicking owned tamed Creeper
        UseItemCallback.EVENT.register((player, world, hand) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.crosshairTarget == null) return TypedActionResult.pass(ItemStack.EMPTY);
            if (client.crosshairTarget.getType() != HitResult.Type.ENTITY) return TypedActionResult.pass(ItemStack.EMPTY);

            EntityHitResult entityHit = (EntityHitResult) client.crosshairTarget;
            if (!(entityHit.getEntity() instanceof CreeperEntity creeper)) return TypedActionResult.pass(ItemStack.EMPTY);

            ITamedCreeper tc = (ITamedCreeper)(Object) creeper;
            if (!tc.friendlycreeper$isTamed()) return TypedActionResult.pass(ItemStack.EMPTY);

            UUID ownerUUID = tc.friendlycreeper$getOwnerUUID();
            if (ownerUUID == null || !ownerUUID.equals(player.getUuid())) return TypedActionResult.pass(ItemStack.EMPTY);

            return TypedActionResult.fail(player.getStackInHand(hand));
        });

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
            (entityType, entityRenderer, registrationHelper, context) -> {
                if (entityType == EntityType.CREEPER && entityRenderer instanceof CreeperEntityRenderer) {
                    registrationHelper.register(
                        new CreeperPoppyFeature(
                            (CreeperEntityRenderer) entityRenderer,
                            context.getItemRenderer()
                        )
                    );
                }
            }
        );
    }
}