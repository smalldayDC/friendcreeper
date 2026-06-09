package com.smalldaydc.friendcreeper.client.compat.jade;

import com.smalldaydc.friendcreeper.compat.jade.CreeperOwnerDataProvider;
import com.smalldaydc.friendcreeper.compat.jade.FriendCreeperJadePlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum CreeperOwnerProvider implements IEntityComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        String ownerName = accessor.getServerData().getStringOr(CreeperOwnerDataProvider.TAG_OWNER_NAME, "");
        if (!ownerName.isEmpty()) {
            tooltip.add(Component.translatable("jade.friendcreeper.owner", ownerName));
        }
    }

    @Override
    public Identifier getUid() {
        return FriendCreeperJadePlugin.CREEPER_OWNER;
    }
}
