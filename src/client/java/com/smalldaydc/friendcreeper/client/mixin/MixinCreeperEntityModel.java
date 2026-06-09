package com.smalldaydc.friendcreeper.client.mixin;

import com.smalldaydc.friendcreeper.client.IFriendCreeperRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(CreeperModel.class)
public class MixinCreeperEntityModel {

    @Unique private static final float DEF_HEAD_Y = 6f;
    @Unique private static final float DEF_BODY_Y = 6f;
    @Unique private static final float DEF_LEG_Y  = 18f;

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void friendcreeper$applySitPose(CreeperRenderState state, CallbackInfo ci) {

        CreeperEntityModelAccessor acc = (CreeperEntityModelAccessor)(Object) this;
        ModelPart head       = acc.friendcreeper$getHead();
        ModelPart body       = ((net.minecraft.client.model.Model<?>)(Object) this).root().getChild("body");
        ModelPart leftFront  = acc.friendcreeper$getLeftFrontLeg();
        ModelPart rightFront = acc.friendcreeper$getRightFrontLeg();
        ModelPart leftHind   = acc.friendcreeper$getLeftHindLeg();
        ModelPart rightHind  = acc.friendcreeper$getRightHindLeg();

        // Always reset originY to defaults (model instances are shared)
        head.y       = DEF_HEAD_Y;
        body.y       = DEF_BODY_Y;
        leftFront.y  = DEF_LEG_Y;
        rightFront.y = DEF_LEG_Y;
        leftHind.y   = DEF_LEG_Y;
        rightHind.y  = DEF_LEG_Y;
        // NOTE: do NOT reset pitch here — vanilla setAngles already set walking animation

        IFriendCreeperRenderState fcState = (IFriendCreeperRenderState) state;
        if (!fcState.friendcreeper$isSitting()) return;

        // Sitting pose: head+body sink together, legs fold UP
        head.y       = DEF_HEAD_Y + 1f;
        body.y       = DEF_BODY_Y + 1f;
        leftFront.y  = DEF_LEG_Y  - 4f;
        rightFront.y = DEF_LEG_Y  - 4f;
        leftHind.y   = DEF_LEG_Y  - 4f;
        rightHind.y  = DEF_LEG_Y  - 4f;
        leftFront.xRot    = -0.8f;
        rightFront.xRot   = -0.8f;
        leftHind.xRot     =  0.8f;
        rightHind.xRot    =  0.8f;
    }
}
