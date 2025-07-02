package net.pneumono.divorcesteal.hearts;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.pneumono.divorcesteal.Divorcesteal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HeartDataState extends PersistentState {
    public static final Codec<HeartDataState> CODEC = PlayerHeartData.CODEC.listOf().xmap(
            HeartDataState::new,
            data -> data.dataMap.entrySet().stream().map(
                    entry -> {
                        SimpleHeartData simpleData = entry.getValue();
                        return new PlayerHeartData(entry.getKey(), simpleData.name, simpleData.hearts);
                    }
            ).toList()
    );

    public static final PersistentStateType<HeartDataState> STATE_TYPE = new PersistentStateType<>(
            "DivorcestealHearts",
            context -> new HeartDataState(
                    context.getWorldOrThrow().getPlayers().stream().map(player -> new PlayerHeartData(player, Divorcesteal.DEFAULT_HEARTS.get())).toList()
            ),
            context -> CODEC,
            null
    );

    private final Map<UUID, SimpleHeartData> dataMap;

    public HeartDataState(List<PlayerHeartData> dataList) {
        this.dataMap = new HashMap<>();
        for (PlayerHeartData data : dataList) {
            this.dataMap.put(data.uuid(), new SimpleHeartData(data));
        }
    }

    public PlayerHeartData getOrCreateHeartData(PlayerEntity player) {
        UUID uuid = player.getUuid();
        if (dataMap.containsKey(uuid)) {
            return getHeartData(uuid);
        } else {
            SimpleHeartData simpleData = new SimpleHeartData(player.getGameProfile().getName(), 10);
            dataMap.put(uuid, simpleData);
            return simpleData.toPlayerHeartData(uuid);
        }
    }

    /**
     * Throws an {@link IllegalStateException} if no heart data exists for the UUID.
     */
    private PlayerHeartData getHeartData(UUID uuid) {
        SimpleHeartData simpleData = dataMap.get(uuid);
        if (simpleData == null) throw new IllegalStateException("No heart data exists for UUID: " + uuid);
        return simpleData.toPlayerHeartData(uuid);
    }

    public void setHeartData(PlayerEntity player, int hearts) {
        GameProfile profile = player.getGameProfile();
        setHeartData(profile.getId(), profile.getName(), hearts);
    }

    private void setHeartData(UUID uuid, String name, int hearts) {
        dataMap.put(uuid, new SimpleHeartData(name, hearts));
        markDirty();
    }

    private record SimpleHeartData(String name, int hearts) {
        public SimpleHeartData(PlayerHeartData data) {
            this(data.name(), data.hearts());
        }

        public PlayerHeartData toPlayerHeartData(UUID uuid) {
            return new PlayerHeartData(uuid, this.name, this.hearts);
        }
    }
}
