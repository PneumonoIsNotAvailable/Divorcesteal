package net.pneumono.divorcesteal.content.component;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class CraftedComponent implements TooltipAppender {
    public static final CraftedComponent INSTANCE = new CraftedComponent();

    public static final Codec<CraftedComponent> CODEC = Codec.unit(INSTANCE);
    public static final PacketCodec<ByteBuf, CraftedComponent> PACKET_CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        textConsumer.accept(Text.translatable("item.divorcesteal.heart.crafted").formatted(Formatting.GRAY));
    }
}
