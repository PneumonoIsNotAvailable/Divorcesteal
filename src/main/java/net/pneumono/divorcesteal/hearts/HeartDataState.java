package net.pneumono.divorcesteal.hearts;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import net.pneumono.divorcesteal.DivorcestealConfig;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HeartDataState {
    public static final Codec<HeartDataState> CODEC = PlayerHeartData.CODEC.listOf().xmap(
            HeartDataState::new,
            HeartDataState::getHeartDataList
    );

    private final Map<UUID, PlayerHeartData> dataMap;

    protected HeartDataState() {
        this(new ArrayList<>());
    }

    public HeartDataState(List<PlayerHeartData> dataList) {
        this.dataMap = new HashMap<>();
        for (PlayerHeartData data : dataList) {
            this.dataMap.put(data.uuid(), data);
        }
    }

    public static HeartDataState get() {
        return DataSaving.getState();
    }

    public List<PlayerHeartData> getHeartDataList() {
        return this.dataMap.values().stream().toList();
    }

    public @Nullable PlayerHeartData getHeartData(UUID uuid) {
        return dataMap.get(uuid);
    }

    public @Nullable PlayerHeartDataReference getHeartDataReference(UUID uuid) {
        PlayerHeartData data = getHeartData(uuid);
        return data == null ? null : new PlayerHeartDataReference(this, data);
    }

    public void addPlayer(GameProfile profile) {
        dataMap.put(profile.getId(), new PlayerHeartData(profile.getId(), profile.getName(), DivorcestealConfig.DEFAULT_HEARTS.getValue(), null));
        markDirty();
    }

    public void removePlayer(UUID uuid) {
        dataMap.remove(uuid);
        markDirty();
    }

    public void markDirty() {
        DataSaving.save();
    }
}
