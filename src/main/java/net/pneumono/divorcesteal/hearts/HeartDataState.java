package net.pneumono.divorcesteal.hearts;

import com.mojang.serialization.Codec;
import net.pneumono.divorcesteal.DivorcestealConfig;

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

    public PlayerHeartData getOrCreateHeartData(UUID uuid, String name) {
        if (dataMap.containsKey(uuid)) {
            return getHeartData(uuid);
        } else {
            if (name == null) throw new IllegalArgumentException("Cannot create heart data without a name!");

            PlayerHeartData data = new PlayerHeartData(uuid, name, DivorcestealConfig.DEFAULT_HEARTS.getValue(), null);
            dataMap.put(uuid, data);
            return data;
        }
    }

    /**
     * Throws an {@link IllegalStateException} if no heart data exists for the UUID.
     */
    public PlayerHeartData getHeartData(UUID uuid) {
        PlayerHeartData data = dataMap.get(uuid);
        if (data == null) throw new IllegalStateException("No heart data exists for UUID: " + uuid);
        return data;
    }

    public void deleteHeartData(UUID uuid) {
        dataMap.remove(uuid);
        markDirty();
    }

    public void markDirty() {
        DataSaving.save();
    }
}
