package com.smalldaydc.friendcreeper.client.mixin;

import com.smalldaydc.friendcreeper.client.IFriendCreeperRenderState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
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

    // Confirmed from the decompiled vanilla model: body spans Y 6..18 / Z -2..2, legs pivot at
    // Y=18 with their near edge exactly at the body's Z boundary (front Z=-4, hind Z=4).
    @Unique private static final float DEF_HEAD_Y = 6f;
    @Unique private static final float DEF_BODY_Y = 6f;
    @Unique private static final float DEF_LEG_Y  = 18f;
    @Unique private static final float DEF_FRONT_LEG_Z = -4f;
    @Unique private static final float DEF_HIND_LEG_Z  =  4f;

    @Unique private static final float SIT_ANGLE = 0.8f;  // leg fold, radians from vertical
    @Unique private static final float SINK      = 2.5f;  // head/body drop for a settled look

    // Rotating a leg SIT_ANGLE around its hip pivot swings its near (body-side) edge away from
    // the body's flat face by 2*cos(SIT_ANGLE) =~ 1.39px, opening a wedge-shaped gap right at
    // the seam. Pulling the pivot Z_EMBED toward the body's center puts that edge back inside
    // the body volume instead. The minimum needed is 2 - 2*cos(SIT_ANGLE) =~ 0.61; 0.7 leaves a
    // small safety margin without approaching ~1.5, which is wide enough overlap to z-fight
    // against the body's own face (tested and reverted).
    @Unique private static final float Z_EMBED = 0.7f;

    // CreeperModel doesn't keep "body" as a field (unlike head/legs), so it can't be reached via
    // @Accessor - only via a string lookup on the shared root part. Cache it once instead of
    // paying that lookup every frame for every rendered creeper.
    @Unique private static ModelPart bodyPart;

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void friendcreeper$applySitPose(CreeperRenderState state, CallbackInfo ci) {

        if (bodyPart == null) {
            bodyPart = ((Model<?>) (Object) this).root().getChild("body");
        }

        CreeperEntityModelAccessor acc = (CreeperEntityModelAccessor)(Object) this;
        ModelPart head       = acc.friendcreeper$getHead();
        ModelPart body       = bodyPart;
        ModelPart leftFront  = acc.friendcreeper$getLeftFrontLeg();
        ModelPart rightFront = acc.friendcreeper$getRightFrontLeg();
        ModelPart leftHind   = acc.friendcreeper$getLeftHindLeg();
        ModelPart rightHind  = acc.friendcreeper$getRightHindLeg();

        // Always reset to defaults (model instances are shared)
        head.y       = DEF_HEAD_Y;
        body.y       = DEF_BODY_Y;
        leftFront.y  = DEF_LEG_Y;
        rightFront.y = DEF_LEG_Y;
        leftHind.y   = DEF_LEG_Y;
        rightHind.y  = DEF_LEG_Y;
        leftFront.z  = DEF_FRONT_LEG_Z;
        rightFront.z = DEF_FRONT_LEG_Z;
        leftHind.z   = DEF_HIND_LEG_Z;
        rightHind.z  = DEF_HIND_LEG_Z;
        // NOTE: do NOT reset pitch here — vanilla setAngles already set walking animation

        IFriendCreeperRenderState fcState = (IFriendCreeperRenderState) state;
        if (!fcState.friendcreeper$isSitting()) return;

        // Sitting pose: head+body sink together; legs fold toward the body and pull in by
        // Z_EMBED so the seam stays buried inside the body from every angle.
        head.y = DEF_HEAD_Y + SINK;
        body.y = DEF_BODY_Y + SINK;

        leftFront.z  = DEF_FRONT_LEG_Z + Z_EMBED;
        rightFront.z = DEF_FRONT_LEG_Z + Z_EMBED;
        leftHind.z   = DEF_HIND_LEG_Z  - Z_EMBED;
        rightHind.z  = DEF_HIND_LEG_Z  - Z_EMBED;
        leftFront.xRot  = -SIT_ANGLE;
        rightFront.xRot = -SIT_ANGLE;
        leftHind.xRot   =  SIT_ANGLE;
        rightHind.xRot  =  SIT_ANGLE;
    }
}
