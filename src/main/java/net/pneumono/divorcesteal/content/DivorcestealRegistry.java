package net.pneumono.divorcesteal.content;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.pneumono.divorcesteal.Divorcesteal;

import java.util.function.Function;

public class DivorcestealRegistry {

    protected static <T extends Item> T registerItem(String name, Function<Item.Settings, T> factory, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Divorcesteal.id(name));
        return Registry.register(Registries.ITEM, key, factory.apply(settings.registryKey(key)));
    }

    public static void registerDivorcestealContent() {

    }
}
