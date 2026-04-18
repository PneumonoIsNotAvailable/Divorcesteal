package net.pneumono.aprilfools.gacha.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.pneumono.divorcesteal.content.component.AbstractNameAndIdComponent;
import net.pneumono.divorcesteal.registry.DivorcestealNetworking;
import net.pneumono.aprilfools.gacha.GachaRarity;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class GachaResult extends AbstractNameAndIdComponent implements TooltipProvider {
    public static final Codec<GachaResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GachaRarity.CODEC.fieldOf("rarity").forGetter(GachaResult::rarity),
            NameAndId.CODEC.fieldOf("player").forGetter(GachaResult::nameAndId)
    ).apply(instance, GachaResult::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GachaResult> STREAM_CODEC = StreamCodec.composite(
            GachaRarity.STREAM_CODEC, GachaResult::rarity,
            DivorcestealNetworking.NAME_AND_ID_CODEC, GachaResult::nameAndId,
            GachaResult::new
    );

    private final GachaRarity rarity;

    public GachaResult(GachaRarity rarity, NameAndId nameAndId) {
        super(nameAndId);
        this.rarity = rarity;
    }

    public GachaRarity rarity() {
        return this.rarity;
    }

    @Override
    public void addToTooltip(Item.@NonNull TooltipContext context, Consumer<Component> textConsumer, @NonNull TooltipFlag flag, @NonNull DataComponentGetter components) {
        textConsumer.accept(Component.translatable(
                "block.divorcesteal.gacha_beacon.result",
                Component.translatable(this.rarity.getTranslationKey()),
                this.nameAndId().name()
        ).withStyle(ChatFormatting.GRAY));
    }
}
