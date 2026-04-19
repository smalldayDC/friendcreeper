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
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class CreeperPoppyFeature extends FeatureRenderer<CreeperEntity, CreeperEntityModel<CreeperEntity>> {

    private final ItemRenderer itemRenderer;
    private final FeatureRendererContext<CreeperEntity, CreeperEntityModel<CreeperEntity>> ctx;

    public CreeperPoppyFeature(
            FeatureRendererContext<CreeperEntity, CreeperEntityModel<CreeperEntity>> context,
            ItemRenderer itemRenderer) {
        super(context);
        this.itemRenderer = itemRenderer;
        this.ctx = context;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                       int light, CreeperEntity entity,
                       float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress,
                       float headYaw, float headPitch) {

        if (!((ITamedCreeper)(Object) entity).friendlycreeper$isTamed()) return;

        // Get head ModelPart for its current pivotY (changes when sitting)
        CreeperEntityModelAccessor acc = (CreeperEntityModelAccessor) ctx.getModel();
        ModelPart head = acc.friendlycreeper$getHead();

        ItemStack poppy = new ItemStack(Items.POPPY);
        BakedModel model = itemRenderer.getModel(poppy, entity.getWorld(), null, entity.getId());

        matrices.push();

        // In feature renderer space: Y=0 is entity top (after -1 scale), positive Y goes toward feet
        // head.pivotY is in model pixels; divide by 16 to get blocks
        // Head cube is 8px tall; pivot at bottom of head.
        // headTopY is negative (above entity center in model space)
        float headTopY = (head.pivotY - 8f) / 16.0f;

        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(
                (float) Math.toRadians(-headYaw)));

        // Negative Y = upward in this coordinate system
        matrices.translate(0.0, headTopY - 0.08, 0.0);
        // Flip 180° so flower faces up instead of down
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180f));
        matrices.scale(0.5f, 0.5f, 0.5f);

        itemRenderer.renderItem(poppy, ModelTransformationMode.GROUND, false,
                matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV, model);

        matrices.pop();
    }
}
