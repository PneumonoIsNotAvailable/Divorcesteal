package net.pneumono.divorcesteal.hearts;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

public class PlayerHeartDataReference {
    private final HeartDataState state;
    private final UUID uuid;
    private String name;
    private int hearts;
    @Nullable
    private Date banDate;

    public PlayerHeartDataReference(HeartDataState state, PlayerHeartData data) {
        this.state = state;
        this.uuid = data.uuid();
        this.name = data.name();
        this.hearts = data.hearts();
        this.banDate = data.banDate();
    }

    public PlayerHeartDataReference(HeartDataState state, GameProfile profile) {
        this.state = state;
        this.uuid = profile.getId();

        PlayerHeartData data = state.getOrCreateHeartData(uuid, profile.getName());
        this.name = data.name();
        this.hearts = data.hearts();
        this.banDate = data.banDate();
    }


    public static PlayerHeartDataReference create(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) throw new IllegalStateException("Cannot set player hearts on the logical client!");

        return new PlayerHeartDataReference(Hearts.getHeartDataState(serverWorld), player.getGameProfile());
    }

    public GameProfile getGameProfile() {
        return new GameProfile(this.uuid, this.name);
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
        this.name = name;
        updateHeartData();
    }

    public void setHearts(int hearts) {
        this.hearts = hearts;
        updateHeartData();
    }

    public void setBanDate(@Nullable Date banDate) {
        this.banDate = banDate;
        updateHeartData();
    }

    private void updateHeartData() {
        this.state.setHeartData(this.uuid, this.name, this.hearts, this.banDate);
    }
}
