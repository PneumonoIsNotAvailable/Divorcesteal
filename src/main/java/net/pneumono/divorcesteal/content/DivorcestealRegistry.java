package net.pneumono.divorcesteal.content;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.world.GameRules;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.hearts.Hearts;

import java.util.function.Function;

public class DivorcestealRegistry {
    public static final HeartItem HEART_ITEM = registerItem("heart", HeartItem::new,
            new Item.Settings().rarity(Rarity.UNCOMMON).component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
    );
    public static final ReviveBeaconItem REVIVE_BEACON_ITEM = registerItem("revive_beacon", ReviveBeaconItem::new,
            new Item.Settings().rarity(Rarity.RARE).component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
    );

    public static final SoundEvent USE_HEART_SOUND = registerSoundEvent("item.heart.use");
    public static final SoundEvent USE_REVIVE_BEACON_SOUND = registerSoundEvent("item.revive_beacon.use");
    public static final SoundEvent DEATHBAN_SOUND = registerSoundEvent("event.deathban");
    public static final SoundEvent REVIVE_SOUND = registerSoundEvent("event.revive");

    private static <T extends Item> T registerItem(String name, Function<Item.Settings, T> factory, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Divorcesteal.id(name));
        return Registry.register(Registries.ITEM, key, factory.apply(settings.registryKey(key)));
    }

    private static <T extends GameRules.Rule<T>> GameRules.Key<T> registerGameRule(String name, GameRules.Type<T> rule) {
        return GameRuleRegistry.register(name, GameRules.Category.PLAYER, rule);
    }

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Divorcesteal.id(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerDivorcestealContent() {
        PayloadTypeRegistry.playS2C().register(RevivablePlayersS2CPayload.PAYLOAD_ID, RevivablePlayersS2CPayload.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(RevivePlayerC2SPayload.PAYLOAD_ID, RevivePlayerC2SPayload.PACKET_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RevivePlayerC2SPayload.PAYLOAD_ID, DivorcestealRegistry::revivePlayer);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(entries -> {
            entries.add(HEART_ITEM);
            entries.add(REVIVE_BEACON_ITEM);
        });
    }

    private static void revivePlayer(RevivePlayerC2SPayload payload, ServerPlayNetworking.Context context) {
        ItemStack stack = context.player().getMainHandStack();
        if (!stack.isOf(DivorcestealRegistry.REVIVE_BEACON_ITEM) || stack.isEmpty()) {
            ItemStack otherStack = context.player().getOffHandStack();
            if (!otherStack.isOf(DivorcestealRegistry.REVIVE_BEACON_ITEM) || stack.isEmpty()) {
                Divorcesteal.LOGGER.warn("Player {} attempted to revive {} without holding a revive beacon!", context.player().getName().getString(), payload.player().getName());
                return;
            }
        }

        stack.decrement(1);
        Hearts.revive(context.player().getWorld(), payload.player());
    }
}
