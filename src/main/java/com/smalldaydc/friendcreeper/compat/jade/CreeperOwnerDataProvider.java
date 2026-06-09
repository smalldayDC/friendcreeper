package com.smalldaydc.friendcreeper.compat.jade;

import com.smalldaydc.friendcreeper.ITamedCreeper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.monster.Creeper;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

import java.util.Optional;
import java.util.UUID;

public enum CreeperOwnerDataProvider implements IServerDataProvider<EntityAccessor> {
    INSTANCE;

    public static final String TAG_OWNER_NAME = "FriendCreeperOwnerName";

    @Override
    public boolean shouldRequestData(EntityAccessor accessor) {
        return accessor.getEntity() instanceof Creeper creeper
                && ((ITamedCreeper) creeper).friendcreeper$isTamed();
    }

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        Creeper creeper = (Creeper) accessor.getEntity();
        UUID ownerUUID = ((ITamedCreeper) creeper).friendcreeper$getOwnerUUID();
        if (ownerUUID == null) return;

        MinecraftServer server = creeper.level().getServer();
        if (server == null) return;

        // Try online player first
        ServerPlayer player = server.getPlayerList().getPlayer(ownerUUID);
        if (player != null) {
            data.putString(TAG_OWNER_NAME, player.getName().getString());
            return;
        }

        // Fall back to NameToIdCache for offline players
        Optional<NameAndId> entry = server.services().nameToIdCache().get(ownerUUID);
        entry.ifPresent(e -> data.putString(TAG_OWNER_NAME, e.name()));
    }

    @Override
    public Identifier getUid() {
        return FriendCreeperJadePlugin.CREEPER_OWNER;
    }
}
