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

import java.util.function.Supplier;

public class Hearts {
    private static final Identifier HEARTS_ID = Divorcesteal.id("hearts");

    public static final Supplier<Integer> MAX_HEARTS = () -> 20;
    public static final Supplier<Integer> DEFAULT_HEARTS = () -> 10;
    public static final Supplier<Integer> REVIVE_HEARTS = () -> 3;

    public static HeartDataState getHeartDataState(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(HeartDataState.STATE_TYPE);
    }

    public static int getHearts(PlayerEntity player) {
        PlayerHeartDataReference reference = PlayerHeartDataReference.create(player);
        return reference.getHearts();
    }

    public static void setHearts(PlayerEntity player, int hearts) {
        PlayerHeartDataReference reference = PlayerHeartDataReference.create(player);
        reference.setHearts(hearts);
        updateHearts(player, hearts);
    }

    public static void addHearts(PlayerEntity player, int hearts) {
        PlayerHeartDataReference reference = PlayerHeartDataReference.create(player);
        int finalHearts = reference.getHearts() + hearts;
        reference.setHearts(finalHearts);
        updateHearts(player, finalHearts);
    }

    /**
     * @return Number of hearts added (may not be equal to {@code hearts} due to validation)
     */
    public static int addHeartsValidated(PlayerEntity player, int hearts, boolean allowDeathban) {
        PlayerHeartDataReference reference = PlayerHeartDataReference.create(player);
        int currentHearts = reference.getHearts();
        int finalHearts = MathHelper.clamp(currentHearts + hearts, allowDeathban ? 0 : 1, Math.max(MAX_HEARTS.get(), currentHearts));
        reference.setHearts(finalHearts);
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
