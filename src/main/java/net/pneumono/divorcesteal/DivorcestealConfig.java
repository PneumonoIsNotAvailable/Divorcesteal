package net.pneumono.divorcesteal;

import net.pneumono.pneumonocore.config.*;

public class DivorcestealConfig {
    public static final IntegerConfiguration MAX_HEARTS = new IntegerConfiguration(id(), "max_hearts", ConfigEnv.SERVER, 1, 100, 20);
    public static final IntegerConfiguration DEFAULT_HEARTS = new IntegerConfiguration(id(), "default_hearts", ConfigEnv.SERVER, 1, 100, 10);
    public static final IntegerConfiguration REVIVE_HEARTS = new IntegerConfiguration(id(), "revive_hearts", ConfigEnv.SERVER, 1, 100, 3);
    public static final IntegerConfiguration REVIVE_DAYS = new IntegerConfiguration(id(), "revive_days", ConfigEnv.SERVER, -1, 100, 30);
    public static final BooleanConfiguration DISABLE_ELYTRA = new BooleanConfiguration(id(), "disable_elytra", ConfigEnv.SERVER, false);
    public static final BooleanConfiguration DISABLE_TOTEMS = new BooleanConfiguration(id(), "disable_totems", ConfigEnv.SERVER, false);
    public static final EnumConfiguration<DateFormat> DATE_FORMAT = new EnumConfiguration<>(id(), "date_format", ConfigEnv.CLIENT, DateFormat.MMDDYYYY);

    protected static void registerDivorcestealConfigs() {
        Configs.register(id(),
                MAX_HEARTS,
                DEFAULT_HEARTS,
                REVIVE_HEARTS,
                REVIVE_DAYS,
                DISABLE_ELYTRA,
                DISABLE_TOTEMS,
                DATE_FORMAT
        );
        Configs.registerCategories(id(),
                new ConfigCategory(id(), "hearts",
                        MAX_HEARTS,
                        DEFAULT_HEARTS,
                        REVIVE_HEARTS,
                        REVIVE_DAYS
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

    @SuppressWarnings("unused")
    public enum DateFormat {
        DDMMYYYY("dd/MM/yyyy", "HH:mm:ss"),
        MMDDYYYY("MM/dd/yyyy", "HH:mm:ss"),
        YYYYMMDD("yyyy/MM/dd", "HH:mm:ss");

        private final String dateFormat;
        private final String timeFormat;

        DateFormat(String dateFormat, String timeFormat) {
            this.dateFormat = dateFormat;
            this.timeFormat = timeFormat;
        }

        public String getDateFormat() {
            return dateFormat;
        }

        public String getTimeFormat() {
            return timeFormat;
        }
    }
}
