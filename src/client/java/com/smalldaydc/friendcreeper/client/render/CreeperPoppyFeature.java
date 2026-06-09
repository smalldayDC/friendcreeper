package com.smalldaydc.friendcreeper.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.smalldaydc.friendcreeper.FriendCreeperConfig;
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
public class CreeperPoppyFeature extends RenderLayer<CreeperRenderState, CreeperModel> {

    // Head cube is 8px tall; head top = -8/16 relative to head pivot in render space
    private static final float HEAD_TOP_OFFSET = -8.0f / 16.0f;

    public CreeperPoppyFeature(
            RenderLayerParent<CreeperRenderState, CreeperModel> context) {
        super(context);
    }

    @Override
    public void submit(PoseStack matrices, SubmitNodeCollector queue,
                       int light, CreeperRenderState state,
                       float limbAngle, float limbDistance) {

        IFriendCreeperRenderState fcState = (IFriendCreeperRenderState) state;
        if (!fcState.friendcreeper$isTamed()) return;
        if (!FriendCreeperConfig.get().renderPoppy) return;

        ItemStackRenderState poppyState = fcState.friendcreeper$getPoppyRenderState();
        if (poppyState.isEmpty()) return;

        // Get the head model part to render in its local coordinate space
        CreeperEntityModelAccessor modelAcc = (CreeperEntityModelAccessor)(Object) getParentModel();
        ModelPart head = modelAcc.friendcreeper$getHead();

        matrices.pushPose();
        // Follow the head's pivot position and all rotations (yaw + pitch)
        head.translateAndRotate(matrices);
        // Translate to the top of the head in head's local space, slightly above surface
        matrices.translate(0.0, HEAD_TOP_OFFSET - 0.08, 0.0);
        matrices.mulPose(Axis.ZP.rotationDegrees(180f));
        matrices.scale(0.5f, 0.5f, 0.5f);

        poppyState.submit(matrices, queue, light, OverlayTexture.NO_OVERLAY, state.outlineColor);

        matrices.popPose();
    }
}
