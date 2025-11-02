package net.pneumono.divorcesteal.hearts;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
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

public class Hearts {
    public static final ResourceLocation HEARTS_MODIFIER_ID = Divorcesteal.id("hearts");
    public static final String ZERO_HEART_BAN_ID = "zero_heart_ban";

    public static HeartDataState getHeartDataState() {
        return HeartDataState.get();
    }

    public static boolean isParticipant(@Nullable Player player) {
        return player != null && getHeartDataState().getHeartData(player.getGameProfile().getId()) != null;
    }

    public static @Nullable ParticipantHeartData getParticipantHeartData(@Nullable Player player) {
        if (player == null) return null;
        return getHeartDataState().getHeartData(player.getGameProfile().getId());
    }

    /**
     * @return Number of hearts added (may not be equal to {@code hearts} due to validation)
     */
    public static int addHeartsValidated(Player player, int hearts, boolean allowDeathban) {
        ParticipantHeartData data = getParticipantHeartData(player);
        if (data == null) return 0;

        int currentHearts = data.getHearts();
        int finalHearts = Mth.clamp(currentHearts + hearts, allowDeathban ? 0 : 1, Math.max(DivorcestealConfig.MAX_HEARTS.getValue(), currentHearts));
        data.setHearts(finalHearts);
        updateData(player);
        return finalHearts - currentHearts;
    }

    public static boolean revive(ServerLevel level, GameProfile profile) {
        HeartDataState state = getHeartDataState();
        ParticipantHeartData data = state.getHeartData(profile.getId());
        if (data == null || !data.isBanned()) return false;

        data.setHearts(DivorcestealConfig.REVIVE_HEARTS.getValue());
        updateData(null, level.getServer(), data);

        return true;
    }

    public static void updateData(Player player) {
        ParticipantHeartData data = getParticipantHeartData(player);
        if (data != null) {
            updateData(player, player.getServer(), data);
        }
    }

    public static void updateData(@Nullable Player player, @Nullable MinecraftServer server, ParticipantHeartData data) {
        updateData(player, server, data, true);
    }

    public static void updateData(@Nullable Player player, @Nullable MinecraftServer server, ParticipantHeartData data, boolean effects) {
        if (player != null) updateHearts(player, data.getHearts());
        if (server != null) updateBan(server, data, effects);
    }

    public static void updateHearts(Player player, int hearts) {
        AttributeInstance entityAttributeInstance = player.getAttributes().getInstance(Attributes.MAX_HEALTH);
        if (entityAttributeInstance != null) {
            entityAttributeInstance.removeModifier(HEARTS_MODIFIER_ID);
            entityAttributeInstance.addPermanentModifier(new AttributeModifier(HEARTS_MODIFIER_ID, (hearts * 2) - 20, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static void updateBan(MinecraftServer server, ParticipantHeartData data, boolean effects) {
        boolean changed;
        if (data.isBanned()) {
            changed = deathban(server, data, effects);
        } else {
            changed = unban(server, data, effects);
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

    public static boolean deathban(MinecraftServer server, ParticipantHeartData data, boolean effects) {
        GameProfile profile = data.getGameProfile();
        UserBanList bannedPlayerList = server.getPlayerList().getBans();

        if (!bannedPlayerList.isBanned(profile)) {
            if (effects) {
                for (ServerPlayer globalPlayer : PlayerLookup.all(server)) {
                    globalPlayer.playNotifySound(DivorcestealRegistry.DEATHBAN_SOUND, SoundSource.PLAYERS, 1.0F, 1.0F);
                    Component banAnnouncement = Component.translatable("divorcesteal.deathban_global", profile.getName());
                    globalPlayer.sendSystemMessage(banAnnouncement, false);
                }
            }

            Date date = new Date();
            UserBanListEntry bannedPlayerEntry = new UserBanListEntry(profile, date, ZERO_HEART_BAN_ID, null, "Zero-Heart Deathban (can be revoked at any time via Revive Beacons)");
            bannedPlayerList.add(bannedPlayerEntry);

            return true;
        }
        return false;
    }

    public static boolean unban(MinecraftServer server, ParticipantHeartData data, boolean effects) {
        GameProfile profile = data.getGameProfile();
        UserBanList bannedPlayerList = server.getPlayerList().getBans();

        UserBanListEntry entry = bannedPlayerList.get(profile);
        if (entry != null && entry.getSource().equals(Hearts.ZERO_HEART_BAN_ID)) {
            bannedPlayerList.remove(profile);

            if (effects) {
                for (ServerPlayer globalPlayer : PlayerLookup.all(server)) {
                    globalPlayer.playNotifySound(DivorcestealRegistry.REVIVE_SOUND, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }

            return true;
        }
        return false;
    }
}
