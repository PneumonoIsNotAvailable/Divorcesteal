package net.pneumono.divorcesteal.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.pneumono.divorcesteal.gui.DeathbanScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow @Nullable public LocalPlayer player;

    @WrapOperation(
            method = "setScreen",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/network/chat/Component;Z)Lnet/minecraft/client/gui/screens/DeathScreen;"
            )
    )
    private DeathScreen setDeathbanScreen(Component message, boolean isHardcore, Operation<DeathScreen> original) {
        Player player = Objects.requireNonNull(this.player);
        return DeathbanScreen.showShow(player) ? new DeathbanScreen(message, isHardcore) : original.call(message, isHardcore);
    }
}
