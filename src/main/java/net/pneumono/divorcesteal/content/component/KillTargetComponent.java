package net.pneumono.divorcesteal.content.component;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
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
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.TooltipProvider;
import net.pneumono.divorcesteal.registry.DivorcestealNetworking;
import org.jspecify.annotations.NonNull;

public record KillTargetComponent(NameAndId nameAndId) implements TooltipProvider {
    public static final Codec<KillTargetComponent> CODEC = Codec.either(ResolvableProfile.CODEC.xmap(
            profile -> new KillTargetComponent(profile.partialProfile()),
            component -> ResolvableProfile.createResolved(new GameProfile(component.nameAndId.id(), component.nameAndId.name()))
    ), NameAndId.CODEC.xmap(
            KillTargetComponent::new, KillTargetComponent::nameAndId
    )).xmap(either -> {
        if (either.left().isPresent()) {
            return either.left().get();
        } else if (either.right().isPresent()) {
            return either.right().get();
        } else {
            throw new IllegalStateException();
        }
    }, Either::left);

    public static final StreamCodec<RegistryFriendlyByteBuf, KillTargetComponent> PACKET_CODEC = DivorcestealNetworking.NAME_AND_ID_CODEC.map(
            KillTargetComponent::new, KillTargetComponent::nameAndId
    );

    public KillTargetComponent(GameProfile profile) {
        this(new NameAndId(profile));
    }

    @Override
    public void addToTooltip(Item.@NonNull TooltipContext context, Consumer<Component> textConsumer, @NonNull TooltipFlag flag, @NonNull DataComponentGetter components) {
        textConsumer.accept(Component.translatable(
                "item.divorcesteal.revive_beacon.wanted",
                this.nameAndId.name()
        ).withStyle(ChatFormatting.GRAY));
    }
}
