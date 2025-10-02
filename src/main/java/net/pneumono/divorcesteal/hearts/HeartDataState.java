package net.pneumono.divorcesteal.hearts;

import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;
import net.pneumono.divorcesteal.DivorcestealConfig;

import java.util.*;

public class HeartDataState {
    public static final Codec<HeartDataState> CODEC = PlayerHeartData.CODEC.listOf().xmap(
            HeartDataState::new,
            HeartDataState::getHeartDataList
    );

    private final Map<UUID, PlayerHeartData> dataMap;
    private MinecraftServer server;

    protected HeartDataState() {
        this(new ArrayList<>());
    }

    public HeartDataState(List<PlayerHeartData> dataList) {
        this.dataMap = new HashMap<>();
        for (PlayerHeartData data : dataList) {
            this.dataMap.put(data.uuid(), data);
        }
    }

    protected void setServer(MinecraftServer server) {
        this.server = server;
    }

    public static HeartDataState create(MinecraftServer server) {
        return DataSaving.getState(server);
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
        DataSaving.save(this.server);
    }
}
