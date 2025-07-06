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
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
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

    public static boolean revive(ServerWorld world, GameProfile profile) {
        HeartDataState state = getHeartDataState(world);
        PlayerHeartDataReference reference = new PlayerHeartDataReference(state, profile);
        if (!reference.isBanned()) return false;

        unban(world.getServer(), reference);
        return true;
    }

    public static void updateData(PlayerEntity player) {
        updateData(player, player.getServer(), PlayerHeartDataReference.create(player));
    }

    public static void updateData(@Nullable PlayerEntity player, @Nullable MinecraftServer server, PlayerHeartDataReference reference) {
        reference.setHearts(reference.getHearts());
        reference.setName(reference.getName());
        if (player != null) updateHearts(player, reference.getHearts());
        if (server != null) updateBan(server, reference);
    }

    public static void updateHearts(PlayerEntity player, int hearts) {
        EntityAttributeInstance entityAttributeInstance = player.getAttributes().getCustomInstance(EntityAttributes.MAX_HEALTH);
        if (entityAttributeInstance != null) {
            entityAttributeInstance.removeModifier(HEARTS_ID);
            entityAttributeInstance.addPersistentModifier(new EntityAttributeModifier(HEARTS_ID, (hearts * 2) - 20, EntityAttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static void updateBan(MinecraftServer server, PlayerHeartDataReference reference) {
        if (reference.isBanned()) {
            deathban(server, reference);
        } else {
            unban(server, reference);
        }
    }

    public static void deathban(MinecraftServer server, PlayerHeartDataReference reference) {
        GameProfile profile = reference.getGameProfile();
        BannedPlayerList bannedPlayerList = server.getPlayerManager().getUserBanList();

        if (!bannedPlayerList.contains(profile)) {
            for (ServerPlayerEntity globalPlayer : PlayerLookup.all(server)) {
                globalPlayer.playSoundToPlayer(DivorcestealRegistry.DEATHBAN_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
                Text banAnnouncement = Text.translatable("divorcesteal.deathban_global", profile.getName());
                globalPlayer.sendMessageToClient(banAnnouncement, false);
            }

            Date date = new Date();
            BannedPlayerEntry bannedPlayerEntry = new BannedPlayerEntry(profile, date, ZERO_HEART_BAN_ID, null, "Zero-Heart Deathban (can be revoked at any time via Revive Beacons)");
            bannedPlayerList.add(bannedPlayerEntry);
            reference.setHearts(0);
        }
    }

    public static void unban(MinecraftServer server, PlayerHeartDataReference reference) {
        GameProfile profile = reference.getGameProfile();
        BannedPlayerList bannedPlayerList = server.getPlayerManager().getUserBanList();

        BannedPlayerEntry entry = bannedPlayerList.get(profile);
        if (entry != null && entry.getSource().equals(Hearts.ZERO_HEART_BAN_ID)) {
            bannedPlayerList.remove(profile);
            reference.setHearts(DivorcestealConfig.REVIVE_HEARTS.getValue());

            for (ServerPlayerEntity globalPlayer : PlayerLookup.all(server)) {
                globalPlayer.playSoundToPlayer(DivorcestealRegistry.REVIVE_SOUND, SoundCategory.PLAYERS, 1.0F, 1.0F);
                Text banAnnouncement = Text.translatable("divorcesteal.revive_global", profile.getName());
                globalPlayer.sendMessageToClient(banAnnouncement, false);
            }
        }
    }
}
