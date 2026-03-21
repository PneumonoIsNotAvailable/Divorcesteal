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

public class KillTargetComponent extends AbstractNameAndIdComponent implements TooltipProvider {
    public static final Codec<KillTargetComponent> CODEC = createCodec(KillTargetComponent::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, KillTargetComponent> PACKET_CODEC = createStreamCodec(KillTargetComponent::new);

    public KillTargetComponent(NameAndId nameAndId) {
        super(nameAndId);
    }

    @Override
    public void addToTooltip(Item.@NonNull TooltipContext context, Consumer<Component> textConsumer, @NonNull TooltipFlag flag, @NonNull DataComponentGetter components) {
        textConsumer.accept(Component.translatable(
                "block.divorcesteal.revive_beacon.wanted",
                this.nameAndId().name()
        ).withStyle(ChatFormatting.GRAY));
    }
}
