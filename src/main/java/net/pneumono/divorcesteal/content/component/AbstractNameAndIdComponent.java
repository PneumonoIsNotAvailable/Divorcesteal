package net.pneumono.divorcesteal.content.component;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.item.component.ResolvableProfile;
import net.pneumono.divorcesteal.registry.DivorcestealNetworking;

import java.util.function.Function;

public abstract class AbstractNameAndIdComponent {
    private final NameAndId nameAndId;

    protected static <T extends AbstractNameAndIdComponent> Codec<T> createCodec(Function<NameAndId, T> constructor) {
        return Codec.either(ResolvableProfile.CODEC.xmap(
                profile -> constructor.apply(new NameAndId(profile.partialProfile())),
                component -> ResolvableProfile.createResolved(new GameProfile(component.nameAndId().id(), component.nameAndId().name()))
        ), NameAndId.CODEC.xmap(
                constructor, T::nameAndId
        )).xmap(either -> {
            if (either.left().isPresent()) {
                return either.left().get();
            } else if (either.right().isPresent()) {
                return either.right().get();
            } else {
                throw new IllegalStateException();
            }
        }, Either::left);
    }

    protected static <T extends AbstractNameAndIdComponent> StreamCodec<RegistryFriendlyByteBuf, T> createStreamCodec(Function<NameAndId, T> constructor) {
        return DivorcestealNetworking.NAME_AND_ID_CODEC.map(
                constructor, T::nameAndId
        );
    }

    public AbstractNameAndIdComponent(NameAndId nameAndId) {
        this.nameAndId = nameAndId;
    }

    public NameAndId nameAndId() {
        return nameAndId;
    }
}
