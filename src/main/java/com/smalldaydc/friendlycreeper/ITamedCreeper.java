package com.smalldaydc.friendlycreeper;

import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public interface ITamedCreeper {
    boolean friendlycreeper$isTamed();
    void friendlycreeper$setTamed(boolean tamed);

    boolean friendlycreeper$isSitting();
    void friendlycreeper$toggleSit();

    @Nullable UUID friendlycreeper$getOwnerUUID();
    void friendlycreeper$setOwnerUUID(@Nullable UUID uuid);

    @Nullable UUID friendlycreeper$getAvengeTargetUUID();
    void friendlycreeper$setAvengeTargetUUID(@Nullable UUID uuid);

    int friendlycreeper$getTameAttempts();
    void friendlycreeper$setTameAttempts(int attempts);
}
