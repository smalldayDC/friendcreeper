package com.smalldaydc.friendlycreeper.client.render;

import com.smalldaydc.friendlycreeper.ITamedCreeper;
import com.smalldaydc.friendlycreeper.client.mixin.CreeperEntityModelAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class CreeperPoppyFeature extends FeatureRenderer<CreeperEntity, CreeperEntityModel<CreeperEntity>> {

    private static final ItemStack POPPY_STACK = new ItemStack(Items.POPPY);

    // Head cube is 8px tall; head top = -8/16 relative to head pivot in render space
    private static final float HEAD_TOP_OFFSET = -8.0f / 16.0f;

    private final ItemRenderer itemRenderer;

    public CreeperPoppyFeature(
            FeatureRendererContext<CreeperEntity, CreeperEntityModel<CreeperEntity>> context,
            ItemRenderer itemRenderer) {
        super(context);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                       int light, CreeperEntity entity,
                       float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress,
                       float headYaw, float headPitch) {

        if (!((ITamedCreeper)(Object) entity).friendlycreeper$isTamed()) return;

        // Get the head model part to render in its local coordinate space
        CreeperEntityModelAccessor modelAcc = (CreeperEntityModelAccessor)(Object) getContextModel();
        ModelPart head = modelAcc.friendlycreeper$getHead();

        matrices.push();
        // Follow the head's pivot position and all rotations (yaw + pitch)
        head.rotate(matrices);
        // Translate to the top of the head in head's local space, slightly above surface
        matrices.translate(0.0, HEAD_TOP_OFFSET - 0.08, 0.0);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180f));
        matrices.scale(0.5f, 0.5f, 0.5f);

        itemRenderer.renderItem(POPPY_STACK, ModelTransformationMode.GROUND, false,
                matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV,
                itemRenderer.getModel(POPPY_STACK, entity.getWorld(), null, entity.getId()));

        matrices.pop();
    }
}
