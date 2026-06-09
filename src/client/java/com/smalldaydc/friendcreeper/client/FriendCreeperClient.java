package com.smalldaydc.friendcreeper.client;

import com.smalldaydc.friendcreeper.ITamedCreeper;
import com.smalldaydc.friendcreeper.client.render.CreeperFishFeature;
import com.smalldaydc.friendcreeper.client.render.CreeperPoppyFeature;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class FriendCreeperClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Prevent item use when right-clicking owned tamed Creeper
        UseItemCallback.EVENT.register((player, world, hand) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.hitResult == null) return InteractionResult.PASS;
            if (client.hitResult.getType() != HitResult.Type.ENTITY) return InteractionResult.PASS;

            EntityHitResult entityHit = (EntityHitResult) client.hitResult;
            if (!(entityHit.getEntity() instanceof Creeper creeper)) return InteractionResult.PASS;

            ITamedCreeper tc = (ITamedCreeper)(Object) creeper;
            if (!tc.friendcreeper$isTamed()) return InteractionResult.PASS;

            UUID ownerUUID = tc.friendcreeper$getOwnerUUID();
            if (ownerUUID == null || !ownerUUID.equals(player.getUUID())) return InteractionResult.PASS;

            return InteractionResult.FAIL;
        });

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
            (entityType, entityRenderer, registrationHelper, context) -> {
                if (entityType == EntityType.CREEPER && entityRenderer instanceof CreeperRenderer) {
                    registrationHelper.register(
                        new CreeperPoppyFeature(
                            (CreeperRenderer) entityRenderer
                        )
                    );
                    registrationHelper.register(
                        new CreeperFishFeature(
                            (CreeperRenderer) entityRenderer
                        )
                    );
                }
            }
        );
    }
}
