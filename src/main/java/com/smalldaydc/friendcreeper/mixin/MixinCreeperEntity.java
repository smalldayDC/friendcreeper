package com.smalldaydc.friendcreeper.mixin;

import com.smalldaydc.friendcreeper.FriendCreeperConfig;
import com.smalldaydc.friendcreeper.FriendCreeperMod;
import com.smalldaydc.friendcreeper.ITamedCreeper;
import com.smalldaydc.friendcreeper.goal.CreeperFeedCatGoal;
import com.smalldaydc.friendcreeper.goal.CreeperFollowOwnerGoal;
import com.smalldaydc.friendcreeper.goal.CreeperPickupFishGoal;
import com.smalldaydc.friendcreeper.goal.CreeperSitGoal;
import com.smalldaydc.friendcreeper.goal.CreeperSuppressTargetGoal;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

@Mixin(Creeper.class)
public abstract class MixinCreeperEntity extends Monster implements ITamedCreeper {

    @Shadow public abstract void setSwellDir(int fuseSpeed);
    @Shadow public abstract int getSwellDir();

    @Unique
    private static final EntityDataAccessor<Boolean> FRIENDCREEPER_TAMED =
            SynchedEntityData.defineId(Creeper.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private static final EntityDataAccessor<Boolean> FRIENDCREEPER_SITTING =
            SynchedEntityData.defineId(Creeper.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private static final EntityDataAccessor<String> FRIENDCREEPER_OWNER =
            SynchedEntityData.defineId(Creeper.class, EntityDataSerializers.STRING);

    @Unique
    private static final EntityDataAccessor<Boolean> FRIENDCREEPER_HAS_TARGET =
            SynchedEntityData.defineId(Creeper.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private static final EntityDataAccessor<Boolean> FRIENDCREEPER_IS_FLEEING =
            SynchedEntityData.defineId(Creeper.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private static final EntityDataAccessor<ItemStack> FRIENDCREEPER_HELD_FISH =
            SynchedEntityData.defineId(Creeper.class, EntityDataSerializers.ITEM_STACK);

    @Unique private static final double CHASE_RANGE_SQ = 16.0 * 16.0;
    @Unique private @Nullable UUID friendcreeper$avengeTargetUUID = null;
    @Unique private int friendcreeper$tameAttempts = 0;
    @Unique private int friendcreeper$hurtSoundCooldown = 0;

    protected MixinCreeperEntity(EntityType<? extends Monster> entityType, Level world) {
        super(entityType, world);
    }

    // ── ITamedCreeper ─────────────────────────────────────────────────────────

    @Override public boolean friendcreeper$isTamed() {
        return this.entityData.get(FRIENDCREEPER_TAMED);
    }

    @Override public void friendcreeper$setTamed(boolean tamed) {
        this.entityData.set(FRIENDCREEPER_TAMED, tamed);
    }

    @Override public boolean friendcreeper$isSitting() {
        return this.entityData.get(FRIENDCREEPER_SITTING);
    }

    @Override public void friendcreeper$toggleSit() {
        boolean nowSitting = !friendcreeper$isSitting();
        this.entityData.set(FRIENDCREEPER_SITTING, nowSitting);
        this.setPose(nowSitting ? Pose.CROUCHING : Pose.STANDING);
        this.getNavigation().stop();
        setSwellDir(-1);
        if (nowSitting) {
            FriendCreeperMod.dropHeldFish((Creeper) (Object) this);
        }
    }

    @Override public @Nullable UUID friendcreeper$getOwnerUUID() {
        String s = this.entityData.get(FRIENDCREEPER_OWNER);
        if (s.isEmpty()) return null;
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override public void friendcreeper$setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(FRIENDCREEPER_OWNER, uuid == null ? "" : uuid.toString());
    }

    @Override public @Nullable UUID friendcreeper$getAvengeTargetUUID() {
        return friendcreeper$avengeTargetUUID;
    }

    @Override public void friendcreeper$setAvengeTargetUUID(@Nullable UUID uuid) {
        this.friendcreeper$avengeTargetUUID = uuid;
    }

    @Override public int friendcreeper$getTameAttempts() {
        return friendcreeper$tameAttempts;
    }

    @Override public void friendcreeper$setTameAttempts(int attempts) {
        this.friendcreeper$tameAttempts = attempts;
    }

    @Override public boolean friendcreeper$hasTarget() {
        return this.entityData.get(FRIENDCREEPER_HAS_TARGET);
    }

    @Override public boolean friendcreeper$isFleeing() {
        return this.entityData.get(FRIENDCREEPER_IS_FLEEING);
    }

    @Override public void friendcreeper$setFleeing(boolean fleeing) {
        this.entityData.set(FRIENDCREEPER_IS_FLEEING, fleeing);
    }

    @Override public ItemStack friendcreeper$getHeldFish() {
        return this.entityData.get(FRIENDCREEPER_HELD_FISH);
    }

    @Override public void friendcreeper$setHeldFish(ItemStack stack) {
        this.entityData.set(FRIENDCREEPER_HELD_FISH, stack);
    }

    // ── DataTracker ───────────────────────────────────────────────────────────

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void friendcreeper$initDataTracker(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(FRIENDCREEPER_TAMED, false);
        builder.define(FRIENDCREEPER_SITTING, false);
        builder.define(FRIENDCREEPER_OWNER, "");
        builder.define(FRIENDCREEPER_HAS_TARGET, false);
        builder.define(FRIENDCREEPER_IS_FLEEING, false);
        builder.define(FRIENDCREEPER_HELD_FISH, ItemStack.EMPTY);
    }

    // ── Goals ─────────────────────────────────────────────────────────────────

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void friendcreeper$initGoals(CallbackInfo ci) {
        Creeper self = (Creeper) (Object) this;
        this.goalSelector.addGoal(1, new CreeperSitGoal(self));
        this.goalSelector.addGoal(2, new CreeperFeedCatGoal(self));
        this.goalSelector.addGoal(3, new CreeperPickupFishGoal(self));
        this.goalSelector.addGoal(4, new CreeperFollowOwnerGoal(self));
        this.targetSelector.addGoal(0, new CreeperSuppressTargetGoal(self));

        // Replace vanilla ActiveTargetGoal with one that filters out gunpowder-holding players
        this.targetSelector.removeAllGoals(goal -> goal instanceof NearestAttackableTargetGoal);
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(self, Player.class, true,
                (target, world) -> !(target instanceof Player p
                        && (p.getMainHandItem().is(Items.GUNPOWDER)
                            || p.getOffhandItem().is(Items.GUNPOWDER)))));

        // Replace vanilla flee goals with conditional ones (respects afraidOfCats config)
        this.goalSelector.removeAllGoals(goal -> goal instanceof AvoidEntityGoal);
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(self, Ocelot.class, 6.0F, 1.0, 1.2) {
            @Override
            public boolean canUse() {
                if (friendcreeper$isTamed() && !FriendCreeperConfig.get().afraidOfCats) return false;
                return super.canUse();
            }
            @Override
            public void start() {
                super.start();
                friendcreeper$setFleeing(true);
            }
            @Override
            public void stop() {
                super.stop();
                // Don't reset fleeing when sitting — tick check handles it
                if (!friendcreeper$isSitting()) friendcreeper$setFleeing(false);
            }
        });
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(self, Cat.class, 6.0F, 1.0, 1.2) {
            @Override
            public boolean canUse() {
                if (friendcreeper$isTamed() && !FriendCreeperConfig.get().afraidOfCats) return false;
                return super.canUse();
            }
            @Override
            public void start() {
                super.start();
                friendcreeper$setFleeing(true);
            }
            @Override
            public void stop() {
                super.stop();
                // Don't reset fleeing when sitting — tick check handles it
                if (!friendcreeper$isSitting()) friendcreeper$setFleeing(false);
            }
        });
    }

    // ── Tick ─────────────────────────────────────────────────────────────────

    @Inject(method = "tick", at = @At("HEAD"))
    private void friendcreeper$preTick(CallbackInfo ci) {
        // Force-stop fuse before CreeperEntity.tick() processes it
        if (friendcreeper$isTamed() && friendcreeper$isSitting() && getSwellDir() > 0) {
            setSwellDir(-1);
        }
        // Force-stop fuse when untamed target picks up gunpowder
        if (!friendcreeper$isTamed()
                && this.getTarget() instanceof Player player
                && (player.getMainHandItem().is(Items.GUNPOWDER)
                    || player.getOffhandItem().is(Items.GUNPOWDER))
                && getSwellDir() > 0) {
            this.setTarget(null);
            setSwellDir(-1);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void friendcreeper$onTick(CallbackInfo ci) {
        // Client-side: handle hurt sound
        if (this.level().isClientSide()) {
            if (!friendcreeper$isTamed()) return;
            if (friendcreeper$hurtSoundCooldown > 0) {
                friendcreeper$hurtSoundCooldown--;
            } else if (FriendCreeperConfig.get().hurtSound
                    && this.getHealth() / this.getMaxHealth() < FriendCreeperMod.LOW_HEALTH_THRESHOLD) {
                if (this.getRandom().nextInt(300) == 0) {
                    this.level().playLocalSound(
                            this.getX(), this.getY(), this.getZ(),
                            SoundEvents.CREEPER_HURT, this.getSoundSource(),
                            0.8f, 0.9f + this.getRandom().nextFloat() * 0.2f, false);
                    friendcreeper$hurtSoundCooldown = 160;
                }
            }
            return;
        }

        if (!friendcreeper$isTamed()) {
            // Clear target if current target picked up gunpowder
            if (this.getTarget() instanceof Player player
                    && (player.getMainHandItem().is(Items.GUNPOWDER)
                        || player.getOffhandItem().is(Items.GUNPOWDER))) {
                this.setTarget(null);
            }

            // Fallback: stop fuse if target is gone
            if (getSwellDir() > 0 && (this.getTarget() == null || !this.getTarget().isAlive())) {
                setSwellDir(-1);
            }
            return;
        }

        Creeper self = (Creeper) (Object) this;
        LivingEntity target = this.getTarget();

        // Force-reset fleeing state when afraidOfCats is disabled
        if (friendcreeper$isFleeing() && !FriendCreeperConfig.get().afraidOfCats) {
            friendcreeper$setFleeing(false);
            this.getNavigation().stop();
        }

        if (target != null && target.isAlive() && this.distanceToSqr(target) > CHASE_RANGE_SQ) {
            this.setTarget(null);
        }

        if ((target == null || !target.isAlive()) && getSwellDir() > 0) {
            setSwellDir(-1);
        }

        // Natural regeneration: heal 1 HP every 200 ticks (~190 seconds from 1 HP to full)
        if (FriendCreeperConfig.get().naturalRegeneration && this.tickCount % 200 == 0) {
            this.heal(1.0f);
        }

        // Drop held fish: low health / afraidOfCats / no reachable hurt owner cat nearby
        if (!friendcreeper$getHeldFish().isEmpty()) {
            boolean lowHealth = this.getHealth() / this.getMaxHealth() < FriendCreeperMod.LOW_HEALTH_THRESHOLD;
            boolean shouldDrop = lowHealth || FriendCreeperConfig.get().afraidOfCats;

            // Check for nearby reachable hurt owner cat every 20 ticks (1 second) to reduce overhead
            if (!shouldDrop && this.tickCount % 20 == 0) {
                if (FriendCreeperMod.findNearestReachableHurtOwnerCat(self) == null) {
                    shouldDrop = true;
                }
            }

            if (shouldDrop) {
                FriendCreeperMod.dropHeldFish(self);
            }
        }

        // Sync hasTarget to client for texture switching
        boolean hasTarget = this.getTarget() != null && this.getTarget().isAlive();
        if (this.entityData.get(FRIENDCREEPER_HAS_TARGET) != hasTarget) {
            this.entityData.set(FRIENDCREEPER_HAS_TARGET, hasTarget);
        }

        // Reset fleeing state when cat leaves (flee goal can't run while sitting)
        if (friendcreeper$isSitting() && friendcreeper$isFleeing() && this.tickCount % 20 == 0) {
            boolean catNearby = !self.level().getEntitiesOfClass(
                    Cat.class, self.getBoundingBox().inflate(6.0), cat -> cat.isAlive()).isEmpty()
                || !self.level().getEntitiesOfClass(
                    Ocelot.class, self.getBoundingBox().inflate(6.0), ocelot -> ocelot.isAlive()).isEmpty();
            if (!catNearby) {
                this.entityData.set(FRIENDCREEPER_IS_FLEEING, false);
            }
        }
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void friendcreeper$writeNbt(ValueOutput view, CallbackInfo ci) {
        view.putBoolean(FriendCreeperMod.NBT_TAMED,    friendcreeper$isTamed());
        view.putBoolean(FriendCreeperMod.NBT_SITTING,  friendcreeper$isSitting());
        view.putInt(    FriendCreeperMod.NBT_ATTEMPTS, friendcreeper$tameAttempts);
        UUID ownerUUID = friendcreeper$getOwnerUUID();
        if (ownerUUID != null) {
            view.store(FriendCreeperMod.NBT_OWNER, UUIDUtil.CODEC, ownerUUID);
        }
        // Save held fish: 0=none, 1=cod, 2=salmon
        ItemStack fish = friendcreeper$getHeldFish();
        if (!fish.isEmpty()) {
            view.putInt(FriendCreeperMod.NBT_HELD_FISH, fish.is(Items.COD) ? 1 : 2);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void friendcreeper$readNbt(ValueInput view, CallbackInfo ci) {
        this.entityData.set(FRIENDCREEPER_TAMED, view.getBooleanOr(FriendCreeperMod.NBT_TAMED, false));
        if (view.getBooleanOr(FriendCreeperMod.NBT_SITTING, false)) {
            this.entityData.set(FRIENDCREEPER_SITTING, true);
            this.setPose(Pose.CROUCHING);
        }
        friendcreeper$tameAttempts = view.getIntOr(FriendCreeperMod.NBT_ATTEMPTS, 0);
        Optional<UUID> ownerOpt = view.read(FriendCreeperMod.NBT_OWNER, UUIDUtil.CODEC);
        ownerOpt.ifPresent(this::friendcreeper$setOwnerUUID);
        // Load held fish
        int fishType = view.getIntOr(FriendCreeperMod.NBT_HELD_FISH, 0);
        if (fishType == 1) {
            this.entityData.set(FRIENDCREEPER_HELD_FISH, new ItemStack(Items.COD));
        } else if (fishType == 2) {
            this.entityData.set(FRIENDCREEPER_HELD_FISH, new ItemStack(Items.SALMON));
        }
    }
}