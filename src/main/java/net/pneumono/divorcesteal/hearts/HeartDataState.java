package net.pneumono.divorcesteal.hearts;

import com.mojang.serialization.Codec;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.DivorcestealConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HeartDataState extends PersistentState {
    public static final Codec<HeartDataState> CODEC = PlayerHeartData.CODEC.listOf().xmap(
            HeartDataState::new,
            HeartDataState::getHeartDataList
    );

    private static final PersistentStateType<HeartDataState> STATE_TYPE = new PersistentStateType<>(
            Divorcesteal.MOD_ID + "_hearts",
            context -> new HeartDataState(
                    context.getWorldOrThrow().getPlayers().stream().map(player -> new PlayerHeartData(player, DivorcestealConfig.DEFAULT_HEARTS.getValue())).toList()
            ),
            context -> CODEC,
            null
    );

    private final Map<UUID, SimpleHeartData> dataMap;
    private ServerWorld world;

    public HeartDataState(List<PlayerHeartData> dataList) {
        this.dataMap = new HashMap<>();
        for (PlayerHeartData data : dataList) {
            this.dataMap.put(data.uuid(), new SimpleHeartData(data));
        }
    }

    public static HeartDataState create(ServerWorld world) {
        HeartDataState state = world.getPersistentStateManager().getOrCreate(STATE_TYPE);
        state.world = world;
        return state;
    }

    public boolean hasData(UUID uuid) {
        return dataMap.containsKey(uuid);
    }

    public List<PlayerHeartData> getHeartDataList() {
        return this.dataMap.entrySet().stream().map(entry -> entry.getValue().toPlayerHeartData(entry.getKey())).toList();
    }

    public PlayerHeartData getOrCreateHeartData(UUID uuid, String name) {
        if (dataMap.containsKey(uuid)) {
            return getHeartData(uuid);
        } else {
            if (name == null) throw new IllegalArgumentException("Cannot create heart data without a name!");

            SimpleHeartData simpleData = new SimpleHeartData(name, DivorcestealConfig.DEFAULT_HEARTS.getValue());
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

    public void setHeartData(UUID uuid, String name, int hearts) {
        dataMap.put(uuid, new SimpleHeartData(name, hearts));
        markDirty();
    }

    private record SimpleHeartData(String name, int hearts) {
        public SimpleHeartData(String name, int hearts) {
            this.name = name;
            this.hearts = Math.max(hearts, 0);
        }

        public SimpleHeartData(PlayerHeartData data) {
            this(data.name(), data.hearts());
        }

        public PlayerHeartData toPlayerHeartData(UUID uuid) {
            return new PlayerHeartData(uuid, this.name, this.hearts);
        }
    }
}
