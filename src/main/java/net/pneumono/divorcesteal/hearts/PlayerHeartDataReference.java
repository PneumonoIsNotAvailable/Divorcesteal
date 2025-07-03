package net.pneumono.divorcesteal.hearts;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

public class PlayerHeartDataReference {
    private final HeartDataState state;
    private final UUID uuid;
    private String name;
    private int hearts;

    public PlayerHeartDataReference(HeartDataState state, UUID uuid) {
        this.state = state;
        this.uuid = uuid;

        refresh();
    }

    public PlayerHeartDataReference(HeartDataState state, GameProfile profile) {
        this.state = state;
        this.uuid = profile.getId();

        PlayerHeartData data = state.getOrCreateHeartData(uuid, profile.getName());
        this.name = data.name();
        this.hearts = data.hearts();
    }


    public static PlayerHeartDataReference create(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) throw new IllegalStateException("Cannot set player hearts on the logical client!");

        return new PlayerHeartDataReference(serverWorld.getPersistentStateManager().getOrCreate(HeartDataState.STATE_TYPE), player.getGameProfile());
    }

    public void refresh() {
        PlayerHeartData data = state.getOrCreateHeartData(uuid, name);
        this.name = data.name();
        this.hearts = data.hearts();
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public int getHearts() {
        return this.hearts;
    }

    public void setName(String name) {
        this.state.setHeartData(uuid, name, hearts);
        this.name = name;
    }

    public void setHearts(int hearts) {
        this.state.setHeartData(uuid, name, hearts);
        this.hearts = hearts;
    }
}
