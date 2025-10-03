package net.pneumono.divorcesteal.hearts;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import net.pneumono.divorcesteal.DivorcestealConfig;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HeartDataState {
    public static final Codec<HeartDataState> CODEC = ParticipantHeartData.CODEC.listOf().xmap(
            HeartDataState::new,
            HeartDataState::getHeartDataList
    );

    private final Map<UUID, ParticipantHeartData> dataMap;

    protected HeartDataState() {
        this(new ArrayList<>());
    }

    public HeartDataState(List<ParticipantHeartData> dataList) {
        this.dataMap = new HashMap<>();
        for (ParticipantHeartData data : dataList) {
            this.dataMap.put(data.getUuid(), data);
        }
    }

    public static HeartDataState get() {
        return DataSaving.getState();
    }

    public List<ParticipantHeartData> getHeartDataList() {
        return this.dataMap.values().stream().toList();
    }

    public @Nullable ParticipantHeartData getHeartData(UUID uuid) {
        return dataMap.get(uuid);
    }

    public void addParticipant(GameProfile profile) {
        dataMap.put(profile.getId(), new ParticipantHeartData(profile.getId(), profile.getName(), DivorcestealConfig.DEFAULT_HEARTS.getValue(), null));
    }

    public void removeParticipant(UUID uuid) {
        dataMap.remove(uuid);
    }
}
