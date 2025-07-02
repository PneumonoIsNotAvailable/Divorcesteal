package net.pneumono.divorcesteal.hearts;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class Hearts {
    public static int getHearts(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) throw new IllegalStateException("Cannot get player hearts on the logical client!");

        return serverWorld.getPersistentStateManager().getOrCreate(HeartDataState.STATE_TYPE).getOrCreateHeartData(player).hearts();
    }

    public static void setHearts(PlayerEntity player, int hearts) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) throw new IllegalStateException("Cannot set player hearts on the logical client!");

        serverWorld.getPersistentStateManager().getOrCreate(HeartDataState.STATE_TYPE).setHeartData(player, hearts);
    }
}
