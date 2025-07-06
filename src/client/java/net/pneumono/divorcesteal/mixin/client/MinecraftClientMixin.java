package net.pneumono.divorcesteal.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.pneumono.divorcesteal.gui.DeathbanScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow @Nullable public ClientPlayerEntity player;

    @WrapOperation(
            method = "setScreen",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/text/Text;Z)Lnet/minecraft/client/gui/screen/DeathScreen;"
            )
    )
    private DeathScreen setDeathbanScreen(Text message, boolean isHardcore, Operation<DeathScreen> original) {
        PlayerEntity player = Objects.requireNonNull(this.player);
        return DeathbanScreen.showShow(player) ? new DeathbanScreen(message, isHardcore) : original.call(message, isHardcore);
    }
}
