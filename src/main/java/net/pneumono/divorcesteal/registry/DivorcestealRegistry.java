package net.pneumono.divorcesteal.registry;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.item.v1.ComponentTooltipAppenderRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.content.*;
import net.pneumono.divorcesteal.content.component.CraftedComponent;
import net.pneumono.divorcesteal.content.component.KillTargetComponent;
import net.pneumono.divorcesteal.content.component.KilledByComponent;

import java.util.function.Function;

public class DivorcestealRegistry {
    public static final ReviveBeaconBlock REVIVE_BEACON_BLOCK = registerReviveBeaconBlock();

    public static final HeartItem HEART_ITEM = registerItem("heart", HeartItem::new,
            new Item.Properties().rarity(Rarity.UNCOMMON).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
    );
    public static final BlockItem REVIVE_BEACON_ITEM = registerItem("revive_beacon",
            settings -> new BlockItem(REVIVE_BEACON_BLOCK, settings),
            new Item.Properties().rarity(Rarity.RARE).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true).useBlockDescriptionPrefix()
    );

    public static final BlockEntityType<ReviveBeaconBlockEntity> REVIVE_BEACON_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Divorcesteal.id("revive_beacon"),
            FabricBlockEntityTypeBuilder.create(ReviveBeaconBlockEntity::new, REVIVE_BEACON_BLOCK).build()
    );

    public static final DataComponentType<KilledByComponent> KILLED_BY_COMPONENT = registerDataComponentType(
            "killer", KilledByComponent.CODEC, KilledByComponent.PACKET_CODEC
    );
    public static final DataComponentType<KillTargetComponent> KILL_TARGET_COMPONENT = registerDataComponentType(
            "kill_target", KillTargetComponent.CODEC, KillTargetComponent.PACKET_CODEC
    );
    public static final DataComponentType<CraftedComponent> CRAFTED_COMPONENT = registerDataComponentType(
            "crafted", CraftedComponent.CODEC, CraftedComponent.PACKET_CODEC
    );

    public static final MenuType<ReviveBeaconMenu> REVIVE_BEACON_SCREEN_HANDLER = Registry.register(BuiltInRegistries.MENU,
            Divorcesteal.id("revive_beacon"),
            new MenuType<>(ReviveBeaconMenu::new, FeatureFlagSet.of())
    );

    public static final SoundEvent USE_HEART_SOUND = registerSoundEvent("item.heart.use");
    public static final SoundEvent USE_REVIVE_BEACON_SOUND = registerSoundEvent("item.revive_beacon.use");
    public static final SoundEvent REVIVE_BEACON_SELECT_SOUND = registerSoundEvent("ui.revive_beacon.select");
    public static final SoundEvent DEATHBAN_SOUND = registerSoundEvent("event.deathban");
    public static final SoundEvent REVIVE_SOUND = registerSoundEvent("event.revive");

    public static final Identifier STEAL_LIFE_STAT = registerStat("steal_life");
    public static final Identifier WITHDRAW_HEART_STAT = registerStat("withdraw_heart");
    public static final Identifier REVIVE_PLAYER_STAT = registerStat("revive_player");
    public static final Identifier DEATHBAN_PLAYER_STAT = registerStat("deathban_player");
    public static final Identifier DEATHBAN_SELF_STAT = registerStat("deathban_self");
    public static final Identifier INTERACT_WITH_REVIVE_BEACON_STAT = registerStat("interact_with_revive_beacon");

    private static ReviveBeaconBlock registerReviveBeaconBlock() {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Divorcesteal.id("revive_beacon"));
        BlockBehaviour.Properties settings = BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PINK)
                .instrument(NoteBlockInstrument.HAT)
                .strength(3.0F)
                .lightLevel(state -> 15)
                .noOcclusion()
                .isRedstoneConductor(Blocks::never)
                .setId(key);
        return Registry.register(BuiltInRegistries.BLOCK, key, new ReviveBeaconBlock(settings));
    }

    private static <T extends Item> T registerItem(String name, Function<Item.Properties, T> factory, Item.Properties settings) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Divorcesteal.id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(settings.setId(key)));
    }

    private static <T> DataComponentType<T> registerDataComponentType(String name, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> packetCodec) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                Divorcesteal.id(name),
                DataComponentType.<T>builder().persistent(codec).networkSynchronized(packetCodec).build()
        );
    }

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Divorcesteal.id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    private static Identifier registerStat(String name) {
        Identifier id = Divorcesteal.id(name);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, id, id);
        BuiltInRegistries.CUSTOM_STAT.addAlias(Identifier.withDefaultNamespace(name), id);
        Stats.CUSTOM.get(id, StatFormatter.DEFAULT);
        return id;
    }

    public static void registerDivorcestealContent() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.OP_BLOCKS).register(entries -> {
            entries.accept(HEART_ITEM);
            entries.accept(REVIVE_BEACON_ITEM);
        });

        ComponentTooltipAppenderRegistry.addFirst(KILLED_BY_COMPONENT);
        ComponentTooltipAppenderRegistry.addFirst(KILL_TARGET_COMPONENT);
        ComponentTooltipAppenderRegistry.addFirst(CRAFTED_COMPONENT);
    }
}
