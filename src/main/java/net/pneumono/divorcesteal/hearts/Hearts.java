package net.pneumono.divorcesteal.hearts;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.pneumono.divorcesteal.Divorcesteal;

public class Hearts {
    private static final Identifier HEARTS_ID = Divorcesteal.id("hearts");

    public static int getHearts(PlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) throw new IllegalStateException("Cannot get player hearts on the logical client!");

        return serverWorld.getPersistentStateManager().getOrCreate(HeartDataState.STATE_TYPE).getOrCreateHeartData(player).hearts();
    }

    public static void setHearts(PlayerEntity player, int hearts) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) throw new IllegalStateException("Cannot set player hearts on the logical client!");

        serverWorld.getPersistentStateManager().getOrCreate(HeartDataState.STATE_TYPE).setHeartData(player, hearts);
        updateHearts(player, hearts);
    }

    public static void addHearts(PlayerEntity player, int hearts) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) throw new IllegalStateException("Cannot set player hearts on the logical client!");

        HeartDataState heartDataState = serverWorld.getPersistentStateManager().getOrCreate(HeartDataState.STATE_TYPE);
        int finalHearts = heartDataState.getOrCreateHeartData(player).hearts() + hearts;
        heartDataState.setHeartData(player, finalHearts);
        updateHearts(player, finalHearts);
    }

    /**
     * @return Number of hearts added (may not be equal to {@code hearts} due to validation)
     */
    public static int addHeartsValidated(PlayerEntity player, int hearts, boolean allowDeathban) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) throw new IllegalStateException("Cannot set player hearts on the logical client!");

        HeartDataState heartDataState = serverWorld.getPersistentStateManager().getOrCreate(HeartDataState.STATE_TYPE);
        int currentHearts = heartDataState.getOrCreateHeartData(player).hearts();
        int finalHearts = MathHelper.clamp(currentHearts + hearts, allowDeathban ? 0 : 1, Math.max(Divorcesteal.MAX_HEARTS.get(), currentHearts));
        heartDataState.setHeartData(player, finalHearts);
        updateHearts(player, finalHearts);
        return finalHearts - currentHearts;
    }

    public static void updateHearts(PlayerEntity player) {
        updateHearts(player, getHearts(player));
    }

    private static void updateHearts(PlayerEntity player, int hearts) {
        EntityAttributeInstance entityAttributeInstance = player.getAttributes().getCustomInstance(EntityAttributes.MAX_HEALTH);
        if (entityAttributeInstance != null) {
            entityAttributeInstance.removeModifier(HEARTS_ID);
            entityAttributeInstance.addPersistentModifier(new EntityAttributeModifier(HEARTS_ID, (hearts * 2) - 20, EntityAttributeModifier.Operation.ADD_VALUE));
        }

        MinecraftServer server = player.getServer();
        if (server != null) {
            if (hearts == 0) {
                deathban(server, player);
            } else {
                unban(server, player);
            }
        }
    }

    private static void deathban(MinecraftServer server, PlayerEntity player) {
        BannedPlayerList bannedPlayerList = server.getPlayerManager().getUserBanList();
        GameProfile profile = player.getGameProfile();

        if (!bannedPlayerList.contains(profile)) {
            BannedPlayerEntry bannedPlayerEntry = new BannedPlayerEntry(profile, null, "zero_heart_ban", null, "Zero Heart Deathban (may be revoked at any time via revives)");
            bannedPlayerList.add(bannedPlayerEntry);

            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.networkHandler.disconnect(Text.literal("You ran out of hearts!"));
            }

            for (ServerPlayerEntity globalPlayer : server.getPlayerManager().getPlayerList()) {
                // Add deathban sound!
                Text banAnnouncement = player.getName().copy().append(Text.literal(" has been deathbanned!"));
                globalPlayer.sendMessageToClient(banAnnouncement, false);
            }
        }
    }

    private static void unban(MinecraftServer server, PlayerEntity player) {
        BannedPlayerList bannedPlayerList = server.getPlayerManager().getUserBanList();
        GameProfile profile = player.getGameProfile();

        if (bannedPlayerList.contains(profile)) {
            bannedPlayerList.remove(profile);
            for (ServerPlayerEntity globalPlayer : server.getPlayerManager().getPlayerList()) {
                // Add revive sound!
            }
        }
    }
}
