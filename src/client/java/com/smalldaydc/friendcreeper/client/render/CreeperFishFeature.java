package com.smalldaydc.friendcreeper.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.smalldaydc.friendcreeper.client.IFriendCreeperRenderState;
import com.smalldaydc.friendcreeper.client.mixin.CreeperEntityModelAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

@Environment(EnvType.CLIENT)
public class CreeperFishFeature extends RenderLayer<CreeperRenderState, CreeperModel> {

    // Creeper head cube: 8×8×8 pixels, from (-4,-8,-4) to (4,0,4) relative to pivot.
    // Front face is at z = -4/16 = -0.25 in head-local space.
    // Position the fish so it sticks out naturally from the mouth.
    private static final float MOUTH_Y = -0.0625f;  // near the bottom of the face
    private static final float MOUTH_Z = -0.375f;   // 2px in front of the face surface

    public CreeperFishFeature(
            RenderLayerParent<CreeperRenderState, CreeperModel> context) {
        super(context);
    }

    @Override
    public void submit(PoseStack matrices, SubmitNodeCollector queue,
                       int light, CreeperRenderState state,
                       float limbAngle, float limbDistance) {

        IFriendCreeperRenderState fcState = (IFriendCreeperRenderState) state;
        if (!fcState.friendcreeper$isTamed()) return;

        ItemStackRenderState fishState = fcState.friendcreeper$getFishRenderState();
        if (fishState.isEmpty()) return;

        // Get the head model part to render in its local coordinate space
        CreeperEntityModelAccessor modelAcc = (CreeperEntityModelAccessor)(Object) getParentModel();
        ModelPart head = modelAcc.friendcreeper$getHead();

        matrices.pushPose();
        // Follow the head's pivot position and all rotations (yaw + pitch)
        head.translateAndRotate(matrices);
        // Translate to the mouth area on the front of the head
        matrices.translate(0.0, MOUTH_Y, MOUTH_Z);
        // Rotate the flat item sprite from lying flat (facing Y+) to vertical (facing Z-)
        // so the fish texture faces outward from the mouth
        matrices.mulPose(Axis.XP.rotationDegrees(-90f));
        // Flip vertically so the fish isn't upside down
        matrices.mulPose(Axis.ZP.rotationDegrees(180f));
        matrices.scale(0.4f, 0.4f, 0.4f);

        fishState.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, state.outlineColor);

        matrices.popPose();
    }
}
