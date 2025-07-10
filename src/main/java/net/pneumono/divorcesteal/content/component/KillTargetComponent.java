package net.pneumono.divorcesteal.content.component;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public record KillTargetComponent(ProfileComponent profile) implements TooltipAppender {
    public static final Codec<KillTargetComponent> CODEC = ProfileComponent.CODEC.xmap(
            KillTargetComponent::new, KillTargetComponent::profile
    );
    public static final PacketCodec<ByteBuf, KillTargetComponent> PACKET_CODEC = ProfileComponent.PACKET_CODEC.xmap(
            KillTargetComponent::new, KillTargetComponent::profile
    );

    public KillTargetComponent(GameProfile profile) {
        this(new ProfileComponent(profile));
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        textConsumer.accept(Text.translatable(
                "item.divorcesteal.revive_beacon.target",
                this.profile().name().map(Text::literal).orElseGet(() -> Text.translatable("divorcesteal.unknown"))
        ).formatted(Formatting.GRAY));
    }
}
