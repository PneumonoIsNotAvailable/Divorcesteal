package net.pneumono.divorcesteal.content.component;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.TooltipProvider;

public record KilledByComponent(ResolvableProfile profile) implements TooltipProvider {
    public static final Codec<KilledByComponent> CODEC = ResolvableProfile.CODEC.xmap(
            KilledByComponent::new, KilledByComponent::profile
    );
    public static final StreamCodec<ByteBuf, KilledByComponent> PACKET_CODEC = ResolvableProfile.STREAM_CODEC.map(
            KilledByComponent::new, KilledByComponent::profile
    );

    public KilledByComponent(GameProfile profile) {
        this(new ResolvableProfile(profile));
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> textConsumer, TooltipFlag flag, DataComponentGetter components) {
        textConsumer.accept(Component.translatable(
                "item.divorcesteal.player_head.killer",
                this.profile().name().map(Component::literal).orElseGet(() -> Component.translatable("divorcesteal.unknown"))
        ).withStyle(ChatFormatting.GRAY));
    }
}
