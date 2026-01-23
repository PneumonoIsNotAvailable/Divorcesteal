package net.pneumono.divorcesteal.content.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jspecify.annotations.NonNull;

public class CraftedComponent implements TooltipProvider {
    public static final CraftedComponent INSTANCE = new CraftedComponent();

    public static final Codec<CraftedComponent> CODEC = MapCodec.unitCodec(INSTANCE);
    public static final StreamCodec<ByteBuf, CraftedComponent> PACKET_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public void addToTooltip(Item.@NonNull TooltipContext context, Consumer<Component> textConsumer, @NonNull TooltipFlag flag, @NonNull DataComponentGetter components) {
        textConsumer.accept(Component.translatable("item.divorcesteal.heart.crafted").withStyle(ChatFormatting.GRAY));
    }
}
