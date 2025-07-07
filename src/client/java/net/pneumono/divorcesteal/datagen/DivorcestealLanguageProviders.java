package net.pneumono.divorcesteal.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.DivorcestealConfig;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import net.pneumono.pneumonocore.datagen.PneumonoCoreTranslationBuilder;

import java.util.concurrent.CompletableFuture;

public class DivorcestealLanguageProviders {
    public static void addProviders(FabricDataGenerator.Pack pack) {
        pack.addProvider(English::new);
    }

    public static class English extends FabricLanguageProvider {
        public English(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
            super(dataOutput, "en_us", registryLookup);
        }

        @Override
        public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
            PneumonoCoreTranslationBuilder builder = new PneumonoCoreTranslationBuilder(translationBuilder);

            builder.add(DivorcestealRegistry.HEART_ITEM, "Heart");
            builder.add(DivorcestealRegistry.REVIVE_BEACON_ITEM, "Revive Beacon");
            builder.add("divorcesteal.item.player_head.killer", "Killed by %s");

            builder.add("divorcesteal.gui.deathban.title", "You ran out of hearts!");
            builder.add("divorcesteal.gui.revive.title", "Revive Player");

            builder.addSubtitle(DivorcestealRegistry.USE_HEART_SOUND, "Heart applies");
            builder.addSubtitle(DivorcestealRegistry.USE_REVIVE_BEACON_SOUND, "Revive Beacon activates");
            builder.addSubtitle(DivorcestealRegistry.DEATHBAN_SOUND, "Deathban echoes");
            builder.addSubtitle(DivorcestealRegistry.REVIVE_SOUND, "Revival echoes");

            builder.add("divorcesteal.deathban", "You ran out of hearts!");
            builder.add("divorcesteal.deathban_global", "%s ran out of hearts!");

            builder.add(DivorcestealRegistry.STEAL_LIFE_STAT, "Lives Stolen", "stat");
            builder.add(DivorcestealRegistry.WITHDRAW_HEART_STAT, "Hearts Withdrawn", "stat");
            builder.add(DivorcestealRegistry.REVIVE_PLAYER_STAT, "Players Revived", "stat");
            builder.add(DivorcestealRegistry.DEATHBAN_PLAYER_STAT, "Players Deathbanned", "stat");
            builder.add(DivorcestealRegistry.DEATHBAN_SELF_STAT, "Times Deathbanned", "stat");

            builder.add("commands.divorcesteal.get", "%1$s has %2$s hearts");
            builder.add("commands.divorcesteal.set.single", "Set %1$s to %2$s hearts");
            builder.add("commands.divorcesteal.set.multiple", "Set %1$s players to %2$s hearts");
            builder.add("commands.divorcesteal.add.single", "Gave %1$s hearts to %2$s");
            builder.add("commands.divorcesteal.add.multiple", "Gave %1$s hearts to %2$s players");
            builder.add("commands.divorcesteal.remove.single", "Took %1$s hearts from %2$s");
            builder.add("commands.divorcesteal.remove.multiple", "Took %1$s hearts from %2$s players");
            builder.add("commands.divorcesteal.revive.single", "Revived %s");
            builder.add("commands.divorcesteal.revive.multiple", "Revived %s players");
            builder.add("commands.divorcesteal.delete.single", "Deleted data for %s");
            builder.add("commands.divorcesteal.delete.multiple", "Deleted data for %s players");
            builder.add("commands.divorcesteal.withdraw.single", "Withdrew 1 heart");
            builder.add("commands.divorcesteal.withdraw.multiple", "Withdrew %s hearts");
            builder.add("commands.divorcesteal.withdraw.fail", "Could not withdraw any more hearts!");

            builder.add("arguments.divorcesteal.all_data", "All data");
            builder.add("arguments.divorcesteal.error.no_data", "No data exists for that player selection!");
            builder.add("arguments.divorcesteal.error.not_deathbanned", "Cannot revive a player that isn't deathbanned!");

            builder.addConfigScreenTitle(Divorcesteal.MOD_ID, "Divorcesteal Configs");
            builder.addConfig(DivorcestealConfig.MAX_HEARTS,
                    "Max Hearts",
                    "The maximum number of hearts a player can have"
            );
            builder.addConfig(DivorcestealConfig.DEFAULT_HEARTS,
                    "Default Hearts",
                    "The number of hearts players start with when they first join"
            );
            builder.addConfig(DivorcestealConfig.REVIVE_HEARTS,
                    "Revive Hearts",
                    "The number of hearts players start with after being revived"
            );
            builder.addConfig(DivorcestealConfig.REVIVE_DAYS,
                    "Days to Revive",
                    "How many real-life days it takes to be automatically revived. -1 represents auto-revival being disabled"
            );
            builder.addConfig(DivorcestealConfig.DISABLE_ELYTRA,
                    "Disable Elytra",
                    "Whether Elytra are disabled"
            );
            builder.addConfig(DivorcestealConfig.DISABLE_TOTEMS,
                    "Disable Totems",
                    "Whether Totems of Undying are disabled"
            );
            builder.addEnumConfig(DivorcestealConfig.DATE_FORMAT,
                    "Date Format",
                    "The format of dates in GUI",
                    "DD/MM/YYYY",
                    "MM/DD/YYYY",
                    "YYYY/MM/DD"
            );
            builder.add("configs.category.divorcesteal.hearts", "Hearts");
            builder.add("configs.category.divorcesteal.rebalances", "Rebalances");

            builder.add("modmenu.nameTranslation.divorcesteal", "Divorcesteal");
            builder.add("modmenu.summaryTranslation.divorcesteal", "The official mod for Divorcesteal");
            builder.add("modmenu.descriptionTranslation.divorcesteal", "The official mod for Divorcesteal, not affiliated with the Lifesteal SMP");
        }
    }
}
