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
            builder.add("commands.hearts.get", "%1$s has %2$s hearts");
            builder.add("commands.hearts.set.single", "Set %1$s to %2$s hearts");
            builder.add("commands.hearts.set.multiple", "Set %1$s players to %2$s hearts");
            builder.add("commands.hearts.add.single", "Gave %1$s hearts to %2$s");
            builder.add("commands.hearts.add.multiple", "Gave %1$s hearts to %2$s players");
            builder.add("commands.hearts.remove.single", "Took %1$s hearts from %2$s");
            builder.add("commands.hearts.remove.multiple", "Took %1$s hearts from %2$s players");
            builder.add("commands.hearts.withdraw.single", "Withdrew 1 heart");
            builder.add("commands.hearts.withdraw.multiple", "Withdrew %s hearts");
            builder.add("commands.hearts.withdraw.fail", "Could not withdraw any more hearts!");
            builder.add("commands.hearts.refresh", "Hearts refreshed");
        }
    }
}
