package net.pneumono.divorcesteal.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import net.pneumono.divorcesteal.content.DivorcestealRegistry;

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
        public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder builder) {
            builder.add(DivorcestealRegistry.HEART_ITEM, "Heart");
            builder.add("divorcesteal.deathban", "You ran out of hearts!");
            builder.add("divorcesteal.deathban_global", "%s ran out of hearts!");
            builder.add("commands.divorcesteal.get", "%1$s has %2$s hearts");
            builder.add("commands.divorcesteal.set.single", "Set %1$s to %2$s hearts");
            builder.add("commands.divorcesteal.set.multiple", "Set %1$s players to %2$s hearts");
            builder.add("commands.divorcesteal.add.single", "Gave %1$s hearts to %2$s");
            builder.add("commands.divorcesteal.add.multiple", "Gave %1$s hearts to %2$s players");
            builder.add("commands.divorcesteal.remove.single", "Took %1$s hearts from %2$s");
            builder.add("commands.divorcesteal.remove.multiple", "Took %1$s hearts from %2$s players");
            builder.add("commands.divorcesteal.revive.single", "Revived %s");
            builder.add("commands.divorcesteal.revive.multiple", "Revived %s players");
            builder.add("commands.divorcesteal.refresh.single", "Refreshed data for %s");
            builder.add("commands.divorcesteal.refresh.multiple", "Refreshed data for %s players");
            builder.add("commands.divorcesteal.withdraw.single", "Withdrew 1 heart");
            builder.add("commands.divorcesteal.withdraw.multiple", "Withdrew %s hearts");
            builder.add("commands.divorcesteal.withdraw.fail", "Could not withdraw any more hearts!");
            builder.add("arguments.divorcesteal.all_data", "All data");
            builder.add("arguments.divorcesteal.error.no_data", "No data exists for that player selection!");
            builder.add("arguments.divorcesteal.error.not_deathbanned", "Cannot revive a player that isn't deathbanned!");
        }
    }
}
