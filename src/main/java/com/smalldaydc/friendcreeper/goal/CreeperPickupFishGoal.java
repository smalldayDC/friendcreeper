package com.smalldaydc.friendcreeper.goal;

import com.smalldaydc.friendcreeper.FriendCreeperConfig;
import com.smalldaydc.friendcreeper.FriendCreeperMod;
import com.smalldaydc.friendcreeper.ITamedCreeper;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;

public class CreeperPickupFishGoal extends Goal {

    private static final double FISH_SEARCH_RANGE = 10.0;

    private final Creeper creeper;
    private ItemEntity targetFish;
    private int updateCountdownTicks;

    public CreeperPickupFishGoal(Creeper creeper) {
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
        if (!tc.friendcreeper$getHeldFish().isEmpty()) return false;
        if (!FriendCreeperConfig.get().feedOwnerCat) return false;
        if (FriendCreeperConfig.get().afraidOfCats) return false;
        if (creeper.getHealth() / creeper.getMaxHealth() < FriendCreeperMod.LOW_HEALTH_THRESHOLD) return false;
        if (creeper.getTarget() != null && creeper.getTarget().isAlive()) return false;

        // Only pick up fish when there is a nearby reachable hurt cat belonging to the same owner
        if (FriendCreeperMod.findNearestReachableHurtOwnerCat(creeper) == null) return false;

        targetFish = findNearestReachableFish();
        return targetFish != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (targetFish == null || !targetFish.isAlive()) return false;
        if (!asTamed().friendcreeper$isTamed()) return false;
        if (asTamed().friendcreeper$isSitting()) return false;
        if (!asTamed().friendcreeper$getHeldFish().isEmpty()) return false;
        if (creeper.getTarget() != null && creeper.getTarget().isAlive()) return false;
        return true;
    }

    @Override
    public void start() {
        this.updateCountdownTicks = 0;
    }

    @Override
    public void tick() {
        if (targetFish == null || !targetFish.isAlive()) return;

        creeper.getLookControl().setLookAt(targetFish, 10.0f, creeper.getMaxHeadXRot());

        if (--this.updateCountdownTicks <= 0 || creeper.getNavigation().isDone()) {
            this.updateCountdownTicks = this.adjustedTickDelay(10);
            boolean pathFound = creeper.getNavigation().moveTo(targetFish, FriendCreeperMod.INTERACTION_MOVE_SPEED);
            if (!pathFound) {
                // Path became invalid mid-travel, give up immediately
                targetFish = null;
                return;
            }
        }

        // Vanilla-style pickup: bounding box overlap with pickup reach expansion
        AABB pickupBox = creeper.getBoundingBox().inflate(
                FriendCreeperMod.INTERACTION_REACH_XZ, FriendCreeperMod.INTERACTION_REACH_Y, FriendCreeperMod.INTERACTION_REACH_XZ);
        if (pickupBox.intersects(targetFish.getBoundingBox())) {
            asTamed().friendcreeper$setHeldFish(targetFish.getItem().copyWithCount(1));
            if (targetFish.getItem().getCount() <= 1) {
                targetFish.discard();
            } else {
                // Must call setStack() to trigger DataTracker sync to client
                targetFish.setItem(targetFish.getItem().copyWithCount(targetFish.getItem().getCount() - 1));
            }
            targetFish = null;
        }
    }

    @Override
    public void stop() {
        targetFish = null;
        creeper.getNavigation().stop();
    }

    private ItemEntity findNearestReachableFish() {
        AABB searchBox = creeper.getBoundingBox().inflate(FISH_SEARCH_RANGE);
        List<ItemEntity> items = creeper.level().getEntitiesOfClass(
                ItemEntity.class, searchBox,
                item -> item.isAlive()
                        && (item.getItem().is(Items.COD) || item.getItem().is(Items.SALMON)));

        // Sort by distance so we try the closest fish first
        items.sort((a, b) -> Double.compare(
                creeper.distanceToSqr(a), creeper.distanceToSqr(b)));

        for (ItemEntity item : items) {
            // Pre-check path reachability — findPathTo can return partial paths
            Path path = creeper.getNavigation().createPath(item, 1);
            if (path != null && path.canReach()) {
                return item;
            }
        }
        return null;
    }
}
