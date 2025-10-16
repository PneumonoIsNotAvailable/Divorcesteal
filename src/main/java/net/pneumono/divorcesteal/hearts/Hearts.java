package net.pneumono.divorcesteal.hearts;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.DivorcestealConfig;
import net.pneumono.divorcesteal.content.ReviveBeaconBlock;
import net.pneumono.divorcesteal.content.ReviveBeaconScreenHandler;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public class Hearts {
    public static final Identifier HEARTS_MODIFIER_ID = Divorcesteal.id("hearts");
    public static final String ZERO_HEART_BAN_ID = "zero_heart_ban";

    public static HeartDataState getHeartDataState() {
        return HeartDataState.get();
    }

    public static boolean isParticipant(@Nullable PlayerEntity player) {
        return player != null && getHeartDataState().getHeartData(player.getGameProfile().getId()) != null;
    }

    public static @Nullable ParticipantHeartData getParticipantHeartData(@Nullable PlayerEntity player) {
        if (player == null) return null;
        return getHeartDataState().getHeartData(player.getGameProfile().getId());
    }

    /**
     * @return Number of hearts added (may not be equal to {@code hearts} due to validation)
     */
    public static int addHeartsValidated(PlayerEntity player, int hearts, boolean allowDeathban) {
        ParticipantHeartData data = getParticipantHeartData(player);
        if (data == null) return 0;

        int currentHearts = data.getHearts();
        int finalHearts = MathHelper.clamp(currentHearts + hearts, allowDeathban ? 0 : 1, Math.max(DivorcestealConfig.MAX_HEARTS.getValue(), currentHearts));
        data.setHearts(finalHearts);
        updateData(player);
        return finalHearts - currentHearts;
    }

    public static boolean revive(ServerWorld world, GameProfile profile) {
        HeartDataState state = getHeartDataState();
        ParticipantHeartData data = state.getHeartData(profile.getId());
        if (data == null || !data.isBanned()) return false;

        data.setHearts(DivorcestealConfig.REVIVE_HEARTS.getValue());
        updateData(null, world.getServer(), data);

        return true;
    }

    public static void updateData(PlayerEntity player) {
        ParticipantHeartData data = getParticipantHeartData(player);
        if (data != null) {
            updateData(player, player.getServer(), data);
        }
    }

    public static void updateData(@Nullable PlayerEntity player, @Nullable MinecraftServer server, ParticipantHeartData data) {
        updateData(player, server, data, true);
    }

    public static void updateData(@Nullable PlayerEntity player, @Nullable MinecraftServer server, ParticipantHeartData data, boolean effects) {
        if (player != null) updateHearts(player, data.getHearts());
        if (server != null) updateBan(server, data, effects);
    }

    public static void updateHearts(PlayerEntity player, int hearts) {
        EntityAttributeInstance entityAttributeInstance = player.getAttributes().getCustomInstance(EntityAttributes.MAX_HEALTH);
        if (entityAttributeInstance != null) {
            entityAttributeInstance.removeModifier(HEARTS_MODIFIER_ID);
            entityAttributeInstance.addPersistentModifier(new EntityAttributeModifier(HEARTS_MODIFIER_ID, (hearts * 2) - 20, EntityAttributeModifier.Operation.ADD_VALUE));
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
            for (ServerPlayerEntity player : PlayerLookup.all(server)) {
                if (player.currentScreenHandler instanceof ReviveBeaconScreenHandler handler) {
                    ReviveBeaconBlock.sendBeaconUpdatePacket(player,
                            handler.syncId,
                            handler.getTarget(),
                            ReviveBeaconBlock.getRevivableParticipants()
                    );
                }
            }
        }
    }

    public static boolean deathban(MinecraftServer server, ParticipantHeartData data, boolean effects) {
        GameProfile profile = data.getGameProfile();
        BannedPlayerList bannedPlayerList = server.getPlayerManager().getUserBanList();

        if (!bannedPlayerList.contains(profile)) {
            if (effects) {
                for (ServerPlayerEntity globalPlayer : PlayerLookup.all(server)) {
                    globalPlayer.playSoundToPlayer(DivorcestealRegistry.DEATHBAN_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    Text banAnnouncement = Text.translatable("divorcesteal.deathban_global", profile.getName());
                    globalPlayer.sendMessageToClient(banAnnouncement, false);
                }
            }

            Date date = new Date();
            BannedPlayerEntry bannedPlayerEntry = new BannedPlayerEntry(profile, date, ZERO_HEART_BAN_ID, null, "Zero-Heart Deathban (can be revoked at any time via Revive Beacons)");
            bannedPlayerList.add(bannedPlayerEntry);

            return true;
        }
        return false;
    }

    public static boolean unban(MinecraftServer server, ParticipantHeartData data, boolean effects) {
        GameProfile profile = data.getGameProfile();
        BannedPlayerList bannedPlayerList = server.getPlayerManager().getUserBanList();

        BannedPlayerEntry entry = bannedPlayerList.get(profile);
        if (entry != null && entry.getSource().equals(Hearts.ZERO_HEART_BAN_ID)) {
            bannedPlayerList.remove(profile);

            if (effects) {
                for (ServerPlayerEntity globalPlayer : PlayerLookup.all(server)) {
                    globalPlayer.playSoundToPlayer(DivorcestealRegistry.REVIVE_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
                }
            }

            return true;
        }
        return false;
    }
}
