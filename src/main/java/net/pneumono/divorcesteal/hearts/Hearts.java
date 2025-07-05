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
import net.pneumono.divorcesteal.content.DivorcestealRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

public class Hearts {
    private static final Identifier HEARTS_ID = Divorcesteal.id("hearts");
    public static final String ZERO_HEART_BAN_ID = "zero_heart_ban";

    public static HeartDataState getHeartDataState(ServerWorld world) {
        return HeartDataState.create(world);
    }

    /**
     * @return Number of hearts added (may not be equal to {@code hearts} due to validation)
     */
    public static int addHeartsValidated(PlayerEntity player, int hearts, boolean allowDeathban) {
        PlayerHeartDataReference reference = PlayerHeartDataReference.create(player);
        int currentHearts = reference.getHearts();
        int finalHearts = MathHelper.clamp(currentHearts + hearts, allowDeathban ? 0 : 1, Math.max(DivorcestealConfig.MAX_HEARTS.getValue(), currentHearts));
        reference.setHearts(finalHearts);
        updateData(player);
        return finalHearts - currentHearts;
    }

    /**
     * @return {@code true} if the reviving was successful, {@code false} if the player wasn't revivable
     */
    public static boolean revive(ServerWorld world, GameProfile profile) {
        HeartDataState state = getHeartDataState(world);
        PlayerHeartDataReference reference = new PlayerHeartDataReference(state, profile);
        if (reference.getHearts() > 0) return false;

        int hearts = DivorcestealConfig.REVIVE_HEARTS.getValue();
        reference.setHearts(hearts);
        updateBan(world.getServer(), reference);
        return true;
    }

    public static void updateData(PlayerEntity player) {
        updateData(player, player.getServer(), PlayerHeartDataReference.create(player));
    }

    public static void updateData(@Nullable PlayerEntity player, @Nullable MinecraftServer server, PlayerHeartDataReference reference) {
        if (player != null) updateHearts(player, reference.getHearts());
        if (server != null) updateBan(server, reference);
    }

    private static void updateHearts(PlayerEntity player, int hearts) {
        EntityAttributeInstance entityAttributeInstance = player.getAttributes().getCustomInstance(EntityAttributes.MAX_HEALTH);
        if (entityAttributeInstance != null) {
            entityAttributeInstance.removeModifier(HEARTS_ID);
            entityAttributeInstance.addPersistentModifier(new EntityAttributeModifier(HEARTS_ID, (hearts * 2) - 20, EntityAttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void updateBan(MinecraftServer server, PlayerHeartDataReference reference) {
        if (reference.getHearts() == 0) {
            deathban(server, reference);
        } else {
            unban(server, reference);
        }
    }

    private static void deathban(MinecraftServer server, PlayerHeartDataReference reference) {
        GameProfile profile = reference.getGameProfile();
        BannedPlayerList bannedPlayerList = server.getPlayerManager().getUserBanList();

        if (!bannedPlayerList.contains(profile)) {
            Date date = new Date();
            BannedPlayerEntry bannedPlayerEntry = new BannedPlayerEntry(profile, date, ZERO_HEART_BAN_ID, null, "Zero-Heart Deathban (can be revoked at any time via Revive Beacons)");
            bannedPlayerList.add(bannedPlayerEntry);
            reference.setBanDate(date);

            ServerPlayerEntity player = server.getPlayerManager().getPlayer(profile.getId());
            if (player != null) {
                player.networkHandler.disconnect(Text.translatable("divorcesteal.deathban"));
            }

            for (ServerPlayerEntity globalPlayer : PlayerLookup.all(server)) {
                globalPlayer.playSoundToPlayer(DivorcestealRegistry.DEATHBAN_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
                Text banAnnouncement = Text.translatable("divorcesteal.deathban_global", profile.getName());
                globalPlayer.sendMessageToClient(banAnnouncement, false);
            }
        }
    }

    private static void unban(MinecraftServer server, PlayerHeartDataReference reference) {
        GameProfile profile = reference.getGameProfile();
        BannedPlayerList bannedPlayerList = server.getPlayerManager().getUserBanList();

        if (bannedPlayerList.contains(profile)) {
            bannedPlayerList.remove(profile);
            reference.setBanDate(null);

            for (ServerPlayerEntity globalPlayer : PlayerLookup.all(server)) {
                globalPlayer.playSoundToPlayer(DivorcestealRegistry.REVIVE_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
                Text banAnnouncement = Text.translatable("divorcesteal.revive_global", profile.getName());
                globalPlayer.sendMessageToClient(banAnnouncement, false);
            }
        }
    }
}
