package net.pneumono.divorcesteal.content;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public record KillerComponent(Text name) implements TooltipAppender {
    public static final Codec<KillerComponent> CODEC = TextCodecs.CODEC.xmap(KillerComponent::new, KillerComponent::name);
    public static final PacketCodec<ByteBuf, KillerComponent> PACKET_CODEC = PacketCodec.tuple(
            TextCodecs.PACKET_CODEC,
            KillerComponent::name,
            KillerComponent::new
    );

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        textConsumer.accept(Text.translatable("divorcesteal.item.player_head.killer", this.name()).formatted(Formatting.GRAY));
    }
}
