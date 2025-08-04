package net.pneumono.divorcesteal.backwards_compat;

import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.DivorcestealConfig;
import net.pneumono.divorcesteal.hearts.HeartDataState;
import net.pneumono.divorcesteal.hearts.PlayerHeartData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OldDataState extends PersistentState {
    public static final Codec<OldDataState> CODEC = PlayerHeartData.CODEC.listOf().xmap(
            OldDataState::new,
            OldDataState::getHeartDataList
    );

    public static final PersistentStateType<OldDataState> STATE_TYPE = new PersistentStateType<>(
            Divorcesteal.MOD_ID + "_hearts",
            context -> new OldDataState(
                    context.getWorldOrThrow().getPlayers().stream().map(player -> new PlayerHeartData(player, DivorcestealConfig.DEFAULT_HEARTS.getValue())).toList()
            ),
            context -> CODEC,
            null
    );

    private final Map<UUID, SimpleHeartData> dataMap;

    private OldDataState(List<PlayerHeartData> dataList) {
        this.dataMap = new HashMap<>();
        for (PlayerHeartData data : dataList) {
            this.dataMap.put(data.uuid(), new SimpleHeartData(data));
        }
    }

    public List<PlayerHeartData> getHeartDataList() {
        return this.dataMap.entrySet().stream().map(entry -> entry.getValue().toPlayerHeartData(entry.getKey())).toList();
    }

    public void clear() {
        this.dataMap.clear();
        markDirty();
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

    public static void updateFromOld(MinecraftServer server) {
        List<UUID> uuids = new ArrayList<>();
        Map<UUID, String> names = new HashMap<>();
        Map<UUID, Integer> hearts = new HashMap<>();
        Map<UUID, Date> dates = new HashMap<>();

        server.getWorlds().forEach(world -> {
            OldDataState state = world.getPersistentStateManager().getOrCreate(OldDataState.STATE_TYPE);

            state.getHeartDataList().forEach(data -> {
                UUID uuid = data.uuid();
                uuids.add(uuid);
                names.put(uuid, data.name());
                hearts.put(uuid, hearts.getOrDefault(uuid, 10) - 10 + data.hearts());
                if (data.banDate() != null && data.banDate().before(dates.getOrDefault(uuid, new Date()))) {
                    dates.put(uuid, data.banDate());
                }
            });

            state.clear();
        });

        for (UUID uuid : uuids) {
            HeartDataState.create(server).setHeartData(
                    uuid,
                    names.getOrDefault(uuid, "???"),
                    hearts.getOrDefault(uuid, 10),
                    dates.get(uuid)
            );
        }
    }
}
