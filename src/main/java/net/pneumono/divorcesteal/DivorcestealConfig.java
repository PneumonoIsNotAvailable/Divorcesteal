package net.pneumono.divorcesteal;

import net.pneumono.pneumonocore.config.*;

public class DivorcestealConfig {
    public static final IntegerConfiguration MAX_HEARTS = new IntegerConfiguration(id(), "max_hearts", ConfigEnv.SERVER, 1, 100, 20);
    public static final IntegerConfiguration DEFAULT_HEARTS = new IntegerConfiguration(id(), "default_hearts", ConfigEnv.SERVER, 1, 100, 10);
    public static final IntegerConfiguration REVIVE_HEARTS = new IntegerConfiguration(id(), "revive_hearts", ConfigEnv.SERVER, 1, 100, 3);
    public static final IntegerConfiguration REVIVE_DAYS = new IntegerConfiguration(id(), "revive_days", ConfigEnv.SERVER, -1, 100, 30);
    public static final IntegerConfiguration CRAFTED_HEART_LIMIT = new IntegerConfiguration(id(), "crafted_heart_limit", ConfigEnv.SERVER, 1, 100, 7);
    public static final BooleanConfiguration DISABLE_ELYTRA = new BooleanConfiguration(id(), "disable_elytra", ConfigEnv.SERVER, false);
    public static final BooleanConfiguration DISABLE_TOTEMS = new BooleanConfiguration(id(), "disable_totems", ConfigEnv.SERVER, false);

    protected static void registerDivorcestealConfigs() {
        Configs.register(id(),
                MAX_HEARTS,
                DEFAULT_HEARTS,
                REVIVE_HEARTS,
                REVIVE_DAYS,
                CRAFTED_HEART_LIMIT,
                DISABLE_ELYTRA,
                DISABLE_TOTEMS
        );
        Configs.registerCategories(id(),
                new ConfigCategory(id(), "hearts",
                        MAX_HEARTS,
                        DEFAULT_HEARTS,
                        REVIVE_HEARTS,
                        REVIVE_DAYS,
                        CRAFTED_HEART_LIMIT
                ),
                new ConfigCategory(id(), "rebalances",
                        DISABLE_ELYTRA,
                        DISABLE_TOTEMS
                )
        );
    }

    private static String id() {
        return Divorcesteal.MOD_ID;
    }
}
