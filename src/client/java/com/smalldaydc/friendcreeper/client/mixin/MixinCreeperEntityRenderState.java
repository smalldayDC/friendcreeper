package com.smalldaydc.friendcreeper.client.mixin;

import com.smalldaydc.friendcreeper.client.IFriendCreeperRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Environment(EnvType.CLIENT)
@Mixin(CreeperRenderState.class)
public class MixinCreeperEntityRenderState implements IFriendCreeperRenderState {

    @Unique private boolean friendcreeper$tamed = false;
    @Unique private boolean friendcreeper$sitting = false;
    @Unique private boolean friendcreeper$lowHealth = false;
    @Unique private boolean friendcreeper$hasTarget = false;
    @Unique private boolean friendcreeper$fleeing = false;
    @Unique private final ItemStackRenderState friendcreeper$poppyRenderState = new ItemStackRenderState();
    @Unique private final ItemStackRenderState friendcreeper$fishRenderState = new ItemStackRenderState();

    @Override
    public boolean friendcreeper$isTamed() {
        return friendcreeper$tamed;
    }

    @Override
    public void friendcreeper$setTamed(boolean tamed) {
        this.friendcreeper$tamed = tamed;
    }

    @Override
    public boolean friendcreeper$isSitting() {
        return friendcreeper$sitting;
    }

    @Override
    public void friendcreeper$setSitting(boolean sitting) {
        this.friendcreeper$sitting = sitting;
    }

    @Override
    public boolean friendcreeper$isLowHealth() {
        return friendcreeper$lowHealth;
    }

    @Override
    public void friendcreeper$setLowHealth(boolean lowHealth) {
        this.friendcreeper$lowHealth = lowHealth;
    }

    @Override
    public boolean friendcreeper$hasTarget() {
        return friendcreeper$hasTarget;
    }

    @Override
    public void friendcreeper$setHasTarget(boolean hasTarget) {
        this.friendcreeper$hasTarget = hasTarget;
    }

    @Override
    public boolean friendcreeper$isFleeing() {
        return friendcreeper$fleeing;
    }

    @Override
    public void friendcreeper$setFleeing(boolean fleeing) {
        this.friendcreeper$fleeing = fleeing;
    }

    @Override
    public ItemStackRenderState friendcreeper$getPoppyRenderState() {
        return friendcreeper$poppyRenderState;
    }

    @Override
    public ItemStackRenderState friendcreeper$getFishRenderState() {
        return friendcreeper$fishRenderState;
    }
}
