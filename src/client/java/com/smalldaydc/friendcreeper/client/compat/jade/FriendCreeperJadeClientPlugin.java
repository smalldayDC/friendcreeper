package com.smalldaydc.friendcreeper.client.compat.jade;

import net.minecraft.entity.mob.CreeperEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class FriendCreeperJadeClientPlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(CreeperOwnerProvider.INSTANCE, CreeperEntity.class);
    }
}
