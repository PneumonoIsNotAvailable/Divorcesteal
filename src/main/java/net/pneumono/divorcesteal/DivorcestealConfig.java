package net.pneumono.divorcesteal;

import net.pneumono.pneumonocore.config_api.ConfigApi;
import net.pneumono.pneumonocore.config_api.configurations.*;
import net.pneumono.pneumonocore.config_api.enums.LoadType;

public class DivorcestealConfig {
    public static final BoundedIntegerConfiguration MAX_HEARTS = register("max_hearts", new BoundedIntegerConfiguration(
            1, 100, 20, new ConfigSettings().category("hearts").loadType(LoadType.INSTANT)
    ));
    public static final BoundedIntegerConfiguration DEFAULT_HEARTS = register("default_hearts", new BoundedIntegerConfiguration(
            1, 100, 10, new ConfigSettings().category("hearts").loadType(LoadType.INSTANT)
    ));
    public static final BoundedIntegerConfiguration REVIVE_HEARTS = register("revive_hearts", new BoundedIntegerConfiguration(
            1, 100, 3, new ConfigSettings().category("hearts").loadType(LoadType.INSTANT)
    ));
    public static final BoundedIntegerConfiguration REVIVE_DAYS = register("revive_days", new BoundedIntegerConfiguration(
            -1, 100, 30, new ConfigSettings().category("hearts").loadType(LoadType.INSTANT)
    ));
    public static final BoundedIntegerConfiguration CRAFTED_HEART_LIMIT = register("crafted_heart_limit", new BoundedIntegerConfiguration(
            1, 100, 7, new ConfigSettings().category("hearts").loadType(LoadType.INSTANT)
    ));
    public static final BooleanConfiguration DISABLE_ELYTRA = register("disable_elytra", new BooleanConfiguration(
            false, new ConfigSettings().category("rebalances").loadType(LoadType.INSTANT)
    ));
    public static final BooleanConfiguration DISABLE_TOTEMS = register("disable_totems", new BooleanConfiguration(
            false, new ConfigSettings().category("rebalances").loadType(LoadType.INSTANT)
    ));

    public static <T extends AbstractConfiguration<?>> T register(String name, T config) {
        return ConfigApi.register(Divorcesteal.id(name), config);
    }

    protected static void registerDivorcestealConfigs() {
        ConfigApi.finishRegistry(Divorcesteal.MOD_ID);
    }
}
