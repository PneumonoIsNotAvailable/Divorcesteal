package net.pneumono.divorcesteal.hearts;

import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Hearts {
    private static final Map<UUID, SimpleHeartData> DATA_MAP = new HashMap<>();

    private record SimpleHeartData(String name, int hearts) {
        public PlayerHeartData toPlayerHeartData(UUID uuid) {
            return new PlayerHeartData(uuid, this.name, this.hearts);
        }
    }

    public static int getHearts(PlayerEntity player) {
        return getHeartData(player).hearts();
    }

    public static PlayerHeartData getHeartData(PlayerEntity player) {
        UUID uuid = player.getUuid();
        if (DATA_MAP.containsKey(uuid)) {
            return getHeartData(uuid);
        } else {
            SimpleHeartData simpleData = new SimpleHeartData(player.getGameProfile().getName(), 10);
            DATA_MAP.put(uuid, simpleData);
            return simpleData.toPlayerHeartData(uuid);
        }
    }

    /**
     * Throws an {@link IllegalStateException} if no heart data exists for the UUID.
     */
    private static PlayerHeartData getHeartData(UUID uuid) {
        SimpleHeartData simpleData = DATA_MAP.get(uuid);
        if (simpleData == null) throw new IllegalStateException("No heart data exists for UUID: " + uuid);
        return simpleData.toPlayerHeartData(uuid);
    }

    public static void setHearts(PlayerEntity player, int hearts) {
        setHeartData(player.getGameProfile().getId(), player.getGameProfile().getName(), hearts);
    }

    public static void setHeartData(PlayerHeartData playerHeartData) {
        setHeartData(playerHeartData.uuid(), playerHeartData.name(), playerHeartData.hearts());
    }

    private static void setHeartData(UUID uuid, String name, int hearts) {
        DATA_MAP.put(uuid, new SimpleHeartData(name, hearts));
    }
}
