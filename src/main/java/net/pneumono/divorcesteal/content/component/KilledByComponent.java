package net.pneumono.divorcesteal.content.component;

import com.mojang.serialization.Codec;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jspecify.annotations.NonNull;

public class KilledByComponent extends AbstractNameAndIdComponent implements TooltipProvider {
    public static final Codec<KilledByComponent> CODEC = createCodec(KilledByComponent::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, KilledByComponent> STREAM_CODEC = createStreamCodec(KilledByComponent::new);

    public KilledByComponent(NameAndId nameAndId) {
        super(nameAndId);
    }

    @Override
    public void addToTooltip(Item.@NonNull TooltipContext context, Consumer<Component> textConsumer, @NonNull TooltipFlag flag, @NonNull DataComponentGetter components) {
        textConsumer.accept(Component.translatable(
                "item.divorcesteal.player_head.killer",
                this.nameAndId().name()
        ).withStyle(ChatFormatting.GRAY));
    }
}
