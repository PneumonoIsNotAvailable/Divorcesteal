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

public record KilledByComponent(ProfileComponent profile) implements TooltipAppender {
    public static final Codec<KilledByComponent> CODEC = ProfileComponent.CODEC.xmap(
            KilledByComponent::new, KilledByComponent::profile
    );
    public static final PacketCodec<ByteBuf, KilledByComponent> PACKET_CODEC = ProfileComponent.PACKET_CODEC.xmap(
            KilledByComponent::new, KilledByComponent::profile
    );

    public KilledByComponent(GameProfile profile) {
        this(new ProfileComponent(profile));
    }

    public GameProfile gameProfile() {
        return this.profile.gameProfile();
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        textConsumer.accept(Text.translatable(
                "item.divorcesteal.player_head.killer",
                this.profile().name().orElse("???")
        ).formatted(Formatting.GRAY));
    }
}
