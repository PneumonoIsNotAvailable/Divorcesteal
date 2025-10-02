package net.pneumono.divorcesteal.hearts;

import com.mojang.authlib.GameProfile;
import net.pneumono.divorcesteal.DivorcestealConfig;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class PlayerHeartDataReference {
    private final HeartDataState state;
    private final PlayerHeartData data;

    public PlayerHeartDataReference(HeartDataState state, PlayerHeartData data) {
        this.state = state;
        this.data = data;
    }

    public void delete() {
        this.data.setHearts(DivorcestealConfig.DEFAULT_HEARTS.getValue());
        this.state.removePlayer(this.data.uuid());
    }

    public boolean isBanned() {
        return this.data.isBanned();
    }

    public GameProfile getGameProfile() {
        return this.data.gameProfile();
    }

    public UUID getUUID() {
        return this.data.uuid();
    }

    public String getName() {
        return this.data.name();
    }

    public int getHearts() {
        return this.data.hearts();
    }

    public Optional<Date> getBanDate() {
        return Optional.ofNullable(this.data.banDate());
    }

    public void setName(String name) {
        this.data.setName(name);
        this.state.markDirty();
    }

    public void setHearts(int hearts) {
        this.data.setHearts(hearts);
        this.state.markDirty();
    }
}
