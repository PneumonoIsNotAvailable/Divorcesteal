package net.pneumono.divorcesteal.hearts;

import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

public record PlayerHeartData(UUID uuid, String name, int hearts) {
    public PlayerHeartData(PlayerEntity player, int hearts) {
        this(player.getGameProfile().getId(), player.getGameProfile().getName(), hearts);
    }
}
