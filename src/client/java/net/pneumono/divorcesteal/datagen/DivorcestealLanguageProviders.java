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
        }
    }
}
