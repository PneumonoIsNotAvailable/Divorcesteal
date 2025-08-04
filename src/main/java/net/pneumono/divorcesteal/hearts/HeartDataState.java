package net.pneumono.divorcesteal.hearts;

import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;
import net.pneumono.divorcesteal.DivorcestealConfig;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HeartDataState {
    public static final Codec<HeartDataState> CODEC = PlayerHeartData.CODEC.listOf().xmap(
            HeartDataState::new,
            HeartDataState::getHeartDataList
    );

    private final Map<UUID, SimpleHeartData> dataMap;
    private MinecraftServer server;

    protected HeartDataState() {
        this(new ArrayList<>());
    }

    public HeartDataState(List<PlayerHeartData> dataList) {
        this.dataMap = new HashMap<>();
        for (PlayerHeartData data : dataList) {
            this.dataMap.put(data.uuid(), new SimpleHeartData(data));
        }
    }

    protected void setServer(MinecraftServer server) {
        this.server = server;
    }

    public static HeartDataState create(MinecraftServer server) {
        return DataSaving.getState(server);
    }

    public List<PlayerHeartData> getHeartDataList() {
        return this.dataMap.entrySet().stream().map(entry -> entry.getValue().toPlayerHeartData(entry.getKey())).toList();
    }

    public PlayerHeartData getOrCreateHeartData(UUID uuid, String name) {
        if (dataMap.containsKey(uuid)) {
            return getHeartData(uuid);
        } else {
            if (name == null) throw new IllegalArgumentException("Cannot create heart data without a name!");

            SimpleHeartData simpleData = new SimpleHeartData(name, DivorcestealConfig.DEFAULT_HEARTS.getValue(), null);
            dataMap.put(uuid, simpleData);
            return simpleData.toPlayerHeartData(uuid);
        }
    }

    /**
     * Throws an {@link IllegalStateException} if no heart data exists for the UUID.
     */
    public PlayerHeartData getHeartData(UUID uuid) {
        SimpleHeartData simpleData = dataMap.get(uuid);
        if (simpleData == null) throw new IllegalStateException("No heart data exists for UUID: " + uuid);
        return simpleData.toPlayerHeartData(uuid);
    }

    public void setHeartData(UUID uuid, String name, int hearts, @Nullable Date banDate) {
        dataMap.put(uuid, new SimpleHeartData(name, hearts, banDate));
        markDirty(this.server);
    }

    public void deleteHeartData(UUID uuid) {
        dataMap.remove(uuid);
        markDirty(this.server);
    }

    public void markDirty(MinecraftServer server) {
        DataSaving.save(server);
    }

    private record SimpleHeartData(String name, int hearts, @Nullable Date banDate) {
        public SimpleHeartData(String name, int hearts, @Nullable Date banDate) {
            this.name = name;
            this.hearts = Math.max(hearts, 0);
            this.banDate = banDate;
        }

        public SimpleHeartData(PlayerHeartData data) {
            this(data.name(), data.hearts(), data.banDate());
        }

        public PlayerHeartData toPlayerHeartData(UUID uuid) {
            return new PlayerHeartData(uuid, this.name, this.hearts, this.banDate);
        }
    }
}
