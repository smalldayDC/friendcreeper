package com.smalldaydc.friendcreeper.compat.jade;

import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.Identifier;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class FriendCreeperJadePlugin implements IWailaPlugin {

    public static final Identifier CREEPER_OWNER = Identifier.of("friendcreeper", "creeper_owner");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(CreeperOwnerDataProvider.INSTANCE, CreeperEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // Resolve the client-only provider reflectively so this class stays loadable on dedicated servers.
        try {
            Class<?> providerClass = Class.forName("com.smalldaydc.friendcreeper.client.compat.jade.CreeperOwnerProvider");
            IEntityComponentProvider provider = (IEntityComponentProvider) providerClass.getEnumConstants()[0];
            registration.registerEntityComponent(provider, CreeperEntity.class);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to load Friend Creeper Jade client provider", e);
        }
    }
}
