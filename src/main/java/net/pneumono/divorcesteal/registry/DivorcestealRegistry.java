package net.pneumono.divorcesteal.registry;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.item.v1.ComponentTooltipAppenderRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.content.*;

import java.util.function.Function;

public class DivorcestealRegistry {
    public static final HeartItem HEART_ITEM = registerItem("heart", HeartItem::new,
            new Item.Settings().rarity(Rarity.UNCOMMON).component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
    );
    public static final ReviveBeaconItem REVIVE_BEACON_ITEM = registerItem("revive_beacon", ReviveBeaconItem::new,
            new Item.Settings().rarity(Rarity.RARE).component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
    );

    public static final ComponentType<KillerComponent> KILLER = registerDataComponentType(
            "killer", KillerComponent.CODEC, KillerComponent.PACKET_CODEC
    );
    public static final ComponentType<ProfileComponent> KILL_TARGET = registerDataComponentType(
            "kill_target", ProfileComponent.CODEC, ProfileComponent.PACKET_CODEC
    );

    public static final ScreenHandlerType<ReviveBeaconScreenHandler> REVIVE_BEACON_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER,
            Divorcesteal.id("revive_beacon"),
            new ScreenHandlerType<>(ReviveBeaconScreenHandler::new, FeatureSet.empty())
    );

    public static final SoundEvent USE_HEART_SOUND = registerSoundEvent("item.heart.use");
    public static final SoundEvent USE_REVIVE_BEACON_SOUND = registerSoundEvent("item.revive_beacon.use");
    public static final SoundEvent DEATHBAN_SOUND = registerSoundEvent("event.deathban");
    public static final SoundEvent REVIVE_SOUND = registerSoundEvent("event.revive");

    public static final Identifier STEAL_LIFE_STAT = registerStat("steal_life");
    public static final Identifier WITHDRAW_HEART_STAT = registerStat("withdraw_heart");
    public static final Identifier REVIVE_PLAYER_STAT = registerStat("revive_player");
    public static final Identifier DEATHBAN_PLAYER_STAT = registerStat("deathban_player");
    public static final Identifier DEATHBAN_SELF_STAT = registerStat("deathban_self");

    private static <T extends Item> T registerItem(String name, Function<Item.Settings, T> factory, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Divorcesteal.id(name));
        return Registry.register(Registries.ITEM, key, factory.apply(settings.registryKey(key)));
    }

    private static <T> ComponentType<T> registerDataComponentType(String name, Codec<T> codec, PacketCodec<? super RegistryByteBuf, T> packetCodec) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE,
                Divorcesteal.id(name),
                ComponentType.<T>builder().codec(codec).packetCodec(packetCodec).build()
        );
    }

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Divorcesteal.id(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    private static Identifier registerStat(String name) {
        Identifier id = Divorcesteal.id(name);
        Registry.register(Registries.CUSTOM_STAT, name, id);
        Stats.CUSTOM.getOrCreateStat(id, StatFormatter.DEFAULT);
        return id;
    }

    public static void registerDivorcestealContent() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(entries -> {
            entries.add(HEART_ITEM);
            entries.add(REVIVE_BEACON_ITEM);
        });

        ComponentTooltipAppenderRegistry.addFirst(KILLER);
    }
}
