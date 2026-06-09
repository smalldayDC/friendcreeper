package com.smalldaydc.friendcreeper.goal;

import com.smalldaydc.friendcreeper.FriendCreeperConfig;
import com.smalldaydc.friendcreeper.FriendCreeperMod;
import com.smalldaydc.friendcreeper.ITamedCreeper;
import java.util.EnumSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class CreeperFeedCatGoal extends Goal {

    private final Creeper creeper;
    private Cat targetCat;
    private int updateCountdownTicks;

    public CreeperFeedCatGoal(Creeper creeper) {
        this.creeper = creeper;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private ITamedCreeper asTamed() {
        return (ITamedCreeper) (Object) creeper;
    }

    @Override
    public boolean canUse() {
        ITamedCreeper tc = asTamed();
        if (!tc.friendcreeper$isTamed()) return false;
        if (tc.friendcreeper$isSitting()) return false;
        if (tc.friendcreeper$getHeldFish().isEmpty()) return false;
        if (!FriendCreeperConfig.get().feedOwnerCat) return false;
        if (FriendCreeperConfig.get().afraidOfCats) return false;
        if (creeper.getHealth() / creeper.getMaxHealth() < FriendCreeperMod.LOW_HEALTH_THRESHOLD) return false;
        if (creeper.getTarget() != null && creeper.getTarget().isAlive()) return false;

        targetCat = FriendCreeperMod.findNearestReachableHurtOwnerCat(creeper);
        return targetCat != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (targetCat == null || !targetCat.isAlive()) return false;
        if (asTamed().friendcreeper$getHeldFish().isEmpty()) return false;
        if (!asTamed().friendcreeper$isTamed()) return false;
        if (asTamed().friendcreeper$isSitting()) return false;
        if (creeper.getTarget() != null && creeper.getTarget().isAlive()) return false;
        // Stop if cat is fully healed
        if (targetCat.getHealth() >= targetCat.getMaxHealth()) return false;
        return true;
    }

    @Override
    public void start() {
        this.updateCountdownTicks = 0;
    }

    @Override
    public void tick() {
        if (targetCat == null || !targetCat.isAlive()) return;

        creeper.getLookControl().setLookAt(targetCat, 10.0f, creeper.getMaxHeadXRot());

        if (--this.updateCountdownTicks <= 0 || creeper.getNavigation().isDone()) {
            this.updateCountdownTicks = this.adjustedTickDelay(10);
            boolean pathFound = creeper.getNavigation().moveTo(targetCat, FriendCreeperMod.INTERACTION_MOVE_SPEED);
            if (!pathFound) {
                // Cat became unreachable, give up immediately
                targetCat = null;
                return;
            }
        }

        // Bounding box overlap + line of sight check for feeding (prevent feeding through walls)
        AABB feedBox = creeper.getBoundingBox().inflate(
                FriendCreeperMod.INTERACTION_REACH_XZ, FriendCreeperMod.INTERACTION_REACH_Y, FriendCreeperMod.INTERACTION_REACH_XZ);
        if (feedBox.intersects(targetCat.getBoundingBox()) && creeper.hasLineOfSight(targetCat)) {
            feedCat();
        }
    }

    @Override
    public void stop() {
        targetCat = null;
        creeper.getNavigation().stop();
    }

    private void feedCat() {
        if (targetCat == null) return;

        ItemStack fish = asTamed().friendcreeper$getHeldFish();
        // Heal using the food's nutrition value, matching vanilla Cat.mobInteract logic
        FoodProperties food = fish.get(DataComponents.FOOD);
        targetCat.heal(food != null ? (float) food.nutrition() : 1.0f);
        asTamed().friendcreeper$setHeldFish(ItemStack.EMPTY);

        targetCat.playSound(SoundEvents.CAT_EAT, 1.0f, 1.0f);
        if (creeper.level() instanceof ServerLevel sw) {
            sw.sendParticles(ParticleTypes.HEART,
                    targetCat.getX(), targetCat.getY(0.5), targetCat.getZ(),
                    3, 0.3, 0.3, 0.3, 0.05);
        }

        targetCat = null;
    }
}
