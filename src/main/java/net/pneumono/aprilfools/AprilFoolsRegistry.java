package net.pneumono.aprilfools;

import net.pneumono.aprilfools.gacha.GachaRegistry;

public class AprilFoolsRegistry {
    public static void register() {
        GachaRegistry.register();
        AprilFoolsCommands.register();
    }
}
