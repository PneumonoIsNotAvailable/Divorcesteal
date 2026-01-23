package net.pneumono.divorcesteal.hearts;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.DivorcestealConfig;
import net.pneumono.divorcesteal.content.ReviveBeaconBlock;
import net.pneumono.divorcesteal.content.ReviveBeaconMenu;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

public class HeartsUtil {
    public static final Identifier HEARTS_MODIFIER_ID = Divorcesteal.id("hearts");
    public static final String ZERO_HEART_BAN_ID = "zero_heart_ban";

    public static ParticipantMap getParticipantMap() {
        return DataSaving.getState();
    }

    public static boolean isParticipant(@Nullable Player player) {
        return player != null && getParticipantMap().getParticipant(player.getGameProfile().id()) != null;
    }

    public static @Nullable Participant getParticipant(@Nullable Player player) {
        if (player == null) return null;
        return getParticipant(player.getGameProfile().id());
    }

    public static @Nullable Participant getParticipant(@Nullable NameAndId nameAndId) {
        if (nameAndId == null) return null;
        return getParticipant(nameAndId.id());
    }

    public static @Nullable Participant getParticipant(@Nullable UUID uuid) {
        return getParticipantMap().getParticipant(uuid);
    }

    /**
     * @return Number of hearts added (may not be equal to {@code hearts} due to validation)
     */
    public static int addHeartsValidated(Player player, int hearts, boolean allowDeathban) {
        Participant participant = getParticipant(player);
        if (participant == null) return 0;

        int currentHearts = participant.getHearts();
        int finalHearts = Mth.clamp(currentHearts + hearts, allowDeathban ? 0 : 1, Math.max(DivorcestealConfig.MAX_HEARTS.getValue(), currentHearts));
        participant.setHearts(finalHearts);
        updateParticipant(player);
        return finalHearts - currentHearts;
    }

    public static boolean revive(ServerLevel level, UUID uuid) {
        ParticipantMap map = getParticipantMap();
        Participant participant = map.getParticipant(uuid);
        if (participant == null || !participant.isBanned()) return false;

        participant.setHearts(DivorcestealConfig.REVIVE_HEARTS.getValue());
        updateParticipant(null, level.getServer(), participant);

        return true;
    }

    public static void updateParticipant(Player player) {
        Participant participant = getParticipant(player);
        if (participant != null) {
            updateParticipant(player, player.level().getServer(), participant);
        }
    }

    public static void updateParticipant(@Nullable Player player, @Nullable MinecraftServer server, Participant participant) {
        updateParticipant(player, server, participant, true);
    }

    public static void updateParticipant(@Nullable Player player, @Nullable MinecraftServer server, Participant participant, boolean effects) {
        if (player != null) updateHearts(player, participant.getHearts());
        if (server != null) updateBan(server, participant, effects);
    }

    public static void updateHearts(Player player, int hearts) {
        AttributeInstance entityAttributeInstance = player.getAttributes().getInstance(Attributes.MAX_HEALTH);
        if (entityAttributeInstance != null) {
            entityAttributeInstance.removeModifier(HEARTS_MODIFIER_ID);
            entityAttributeInstance.addPermanentModifier(new AttributeModifier(HEARTS_MODIFIER_ID, (hearts * 2) - 20, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static void updateBan(MinecraftServer server, Participant participant, boolean effects) {
        boolean changed;
        if (participant.isBanned()) {
            changed = deathban(server, participant, effects);
        } else {
            changed = unban(server, participant, effects);
        }

        if (changed) {
            for (ServerPlayer player : PlayerLookup.all(server)) {
                if (player.containerMenu instanceof ReviveBeaconMenu handler) {
                    ReviveBeaconBlock.sendBeaconUpdatePacket(player,
                            handler.containerId,
                            handler.getTarget(),
                            ReviveBeaconBlock.getRevivableParticipants()
                    );
                }
            }
        }
    }

    public static boolean deathban(MinecraftServer server, Participant participant, boolean effects) {
        UserBanList bannedPlayerList = server.getPlayerList().getBans();
        NameAndId nameAndId = participant.getNameAndId();

        if (!bannedPlayerList.isBanned(nameAndId)) {
            if (effects) {
                for (ServerPlayer globalPlayer : PlayerLookup.all(server)) {
                    sendSound(globalPlayer, DivorcestealRegistry.DEATHBAN_SOUND);
                    Component banAnnouncement = Component.translatable("divorcesteal.deathban_global", nameAndId.name());
                    globalPlayer.sendSystemMessage(banAnnouncement, false);
                }
            }

            Date date = new Date();
            UserBanListEntry bannedPlayerEntry = new UserBanListEntry(nameAndId, date, ZERO_HEART_BAN_ID, null, "Zero-Heart Deathban (can be revoked at any time via Revive Beacons)");
            bannedPlayerList.add(bannedPlayerEntry);

            return true;
        }
        return false;
    }

    public static boolean unban(MinecraftServer server, Participant participant, boolean effects) {
        UserBanList bannedPlayerList = server.getPlayerList().getBans();
        NameAndId nameAndId = participant.getNameAndId();

        UserBanListEntry entry = bannedPlayerList.get(nameAndId);
        if (entry != null && entry.getSource().equals(HeartsUtil.ZERO_HEART_BAN_ID)) {
            bannedPlayerList.remove(nameAndId);

            if (effects) {
                for (ServerPlayer globalPlayer : PlayerLookup.all(server)) {
                    sendSound(globalPlayer, DivorcestealRegistry.REVIVE_SOUND);
                }
            }

            return true;
        }
        return false;
    }

    private static void sendSound(ServerPlayer player, SoundEvent soundEvent) {
        player.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent),
                SoundSource.PLAYERS,
                player.getX(), player.getY(), player.getZ(),
                1.0F, 1.0F, 1
        ));
    }
}
