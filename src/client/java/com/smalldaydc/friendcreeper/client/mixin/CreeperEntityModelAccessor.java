package com.smalldaydc.friendcreeper.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(CreeperModel.class)
public interface CreeperEntityModelAccessor {
    @Accessor("head")          ModelPart friendcreeper$getHead();
    @Accessor("rightFrontLeg")  ModelPart friendcreeper$getLeftFrontLeg();
    @Accessor("leftFrontLeg") ModelPart friendcreeper$getRightFrontLeg();
    @Accessor("rightHindLeg")   ModelPart friendcreeper$getLeftHindLeg();
    @Accessor("leftHindLeg")  ModelPart friendcreeper$getRightHindLeg();
}
