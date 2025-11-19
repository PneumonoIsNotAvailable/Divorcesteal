package net.pneumono.divorcesteal.hearts;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import net.pneumono.divorcesteal.DivorcestealConfig;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ParticipantMap {
    public static final Codec<ParticipantMap> CODEC = Participant.CODEC.listOf().xmap(
            ParticipantMap::new,
            ParticipantMap::getParticipants
    );

    private final Map<UUID, Participant> dataMap;

    protected ParticipantMap() {
        this(new ArrayList<>());
    }

    public ParticipantMap(List<Participant> participants) {
        this.dataMap = new HashMap<>();
        for (Participant participant : participants) {
            this.dataMap.put(participant.getUuid(), participant);
        }
    }

    public List<Participant> getParticipants() {
        return this.dataMap.values().stream().toList();
    }

    public @Nullable Participant getParticipant(UUID uuid) {
        return dataMap.get(uuid);
    }

    public void addParticipant(GameProfile profile) {
        dataMap.put(profile.getId(), new Participant(profile.getId(), profile.getName(), DivorcestealConfig.DEFAULT_HEARTS.getValue(), null));
    }

    public void removeParticipant(UUID uuid) {
        dataMap.remove(uuid);
    }
}
