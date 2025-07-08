package net.pneumono.divorcesteal.content.component;

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

public record KilledByComponent(Text name) implements TooltipAppender {
    public static final Codec<KilledByComponent> CODEC = TextCodecs.CODEC.xmap(KilledByComponent::new, KilledByComponent::name);
    public static final PacketCodec<ByteBuf, KilledByComponent> PACKET_CODEC = PacketCodec.tuple(
            TextCodecs.PACKET_CODEC,
            KilledByComponent::name,
            KilledByComponent::new
    );

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        textConsumer.accept(Text.translatable("item.divorcesteal.player_head.killer", this.name()).formatted(Formatting.GRAY));
    }
}
