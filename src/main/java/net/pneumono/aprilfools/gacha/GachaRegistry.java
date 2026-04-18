package net.pneumono.aprilfools.gacha;

import net.fabricmc.fabric.api.item.v1.ComponentTooltipAppenderRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
import net.pneumono.aprilfools.gacha.content.*;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.aprilfools.gacha.networking.GachaBeaconResultS2CPayload;
import net.pneumono.aprilfools.gacha.networking.GachaBeaconSpinDataS2CPayload;

public class GachaRegistry {
    public static final GachaBeaconBlock GACHA_BEACON_BLOCK = registerGachaBeaconBlock();

    public static final BlockItem GACHA_BEACON_ITEM = registerGachaBeaconItem();

    public static final BlockEntityType<GachaBeaconBlockEntity> GACHA_BEACON_ENTITY = registerGachaBeaconEntity();

    public static final MenuType<GachaBeaconMenu> GACHA_BEACON_MENU = Registry.register(BuiltInRegistries.MENU,
            Divorcesteal.id("gacha_beacon"),
            new MenuType<>(GachaBeaconMenu::new, FeatureFlagSet.of())
    );

    public static final DataComponentType<GachaResult> GACHA_DATA_COMPONENT = registerGachaDataComponentType();
    public static final DataComponentType<GachaRoll> GACHA_ROLL_COMPONENT = registerGachaRollComponentType();

    public static final ParticleType<GachaRarityParticleOption> GACHA_FINISH_PARTICLE = registerGachaFinishParticle();

    public static final Identifier INTERACT_WITH_GACHA_BEACON_STAT = registerStat("interact_with_gacha_beacon");
    public static final Identifier SPIN_GACHA_BEACON_STAT = registerStat("spin_gacha_beacon");

    private static GachaBeaconBlock registerGachaBeaconBlock() {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Divorcesteal.id("gacha_beacon"));
        BlockBehaviour.Properties settings = BlockBehaviour.Properties.of()
                .mapColor(MapColor.SNOW)
                .instrument(NoteBlockInstrument.HAT)
                .strength(3.0F)
                .lightLevel(state -> 15)
                .noOcclusion()
                .isRedstoneConductor(Blocks::never)
                .setId(key);
        return Registry.register(BuiltInRegistries.BLOCK, key, new GachaBeaconBlock(settings));
    }

    private static BlockItem registerGachaBeaconItem() {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Divorcesteal.id("gacha_beacon"));
        BlockItem item = new BlockItem(GACHA_BEACON_BLOCK, new Item.Properties()
                .rarity(Rarity.RARE)
                .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                .useBlockDescriptionPrefix()
                .setId(key)
        );
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    private static BlockEntityType<GachaBeaconBlockEntity> registerGachaBeaconEntity() {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Divorcesteal.id("gacha_beacon"),
                FabricBlockEntityTypeBuilder.create(GachaBeaconBlockEntity::new, GACHA_BEACON_BLOCK).build()
        );
    }

    private static DataComponentType<GachaResult> registerGachaDataComponentType() {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                Divorcesteal.id("gacha_data"),
                DataComponentType.<GachaResult>builder().persistent(GachaResult.CODEC).networkSynchronized(GachaResult.STREAM_CODEC).build()
        );
    }

    private static DataComponentType<GachaRoll> registerGachaRollComponentType() {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                Divorcesteal.id("gacha_roll"),
                DataComponentType.<GachaRoll>builder().persistent(GachaRoll.CODEC).build()
        );
    }

    private static ParticleType<GachaRarityParticleOption> registerGachaFinishParticle() {
        return Registry.register(
                BuiltInRegistries.PARTICLE_TYPE, Divorcesteal.id("gacha_finish"),
                FabricParticleTypes.complex(GachaRarityParticleOption::codec, GachaRarityParticleOption::streamCodec)
        );
    }

    private static Identifier registerStat(String name) {
        Identifier id = Divorcesteal.id(name);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, id, id);
        Stats.CUSTOM.get(id, StatFormatter.DEFAULT);
        return id;
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(GachaBeaconSpinDataS2CPayload.ID, GachaBeaconSpinDataS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GachaBeaconResultS2CPayload.ID, GachaBeaconResultS2CPayload.CODEC);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.OP_BLOCKS)
                .register(entries -> entries.accept(GACHA_BEACON_ITEM));

        ComponentTooltipAppenderRegistry.addFirst(GACHA_DATA_COMPONENT);

        GachaDataSaving.register();
    }
}
