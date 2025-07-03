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
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class Hearts {
    private static final Identifier HEARTS_ID = Divorcesteal.id("hearts");

    public static final Supplier<Integer> MAX_HEARTS = () -> 20;
    public static final Supplier<Integer> DEFAULT_HEARTS = () -> 10;
    public static final Supplier<Integer> REVIVE_HEARTS = () -> 3;

    public static HeartDataState getHeartDataState(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(HeartDataState.STATE_TYPE);
    }

    /**
     * @return Number of hearts added (may not be equal to {@code hearts} due to validation)
     */
    public static int addHeartsValidated(PlayerEntity player, int hearts, boolean allowDeathban) {
        PlayerHeartDataReference reference = PlayerHeartDataReference.create(player);
        int currentHearts = reference.getHearts();
        int finalHearts = MathHelper.clamp(currentHearts + hearts, allowDeathban ? 0 : 1, Math.max(MAX_HEARTS.get(), currentHearts));
        reference.setHearts(finalHearts);
        updateData(player, finalHearts);
        return finalHearts - currentHearts;
    }

    /**
     * @return {@code true} if the reviving was successful, {@code false} if the player wasn't revivable
     */
    public static boolean revive(ServerWorld world, GameProfile profile) {
        HeartDataState state = getHeartDataState(world);
        PlayerHeartDataReference reference = new PlayerHeartDataReference(state, profile);
        if (reference.getHearts() > 0) return false;

        int hearts = REVIVE_HEARTS.get();
        reference.setHearts(hearts);
        updateBan(world.getServer(), profile, hearts);
        return true;
    }

    public static void updateData(PlayerEntity player) {
        updateData(player, PlayerHeartDataReference.create(player).getHearts());
    }

    public static void updateData(PlayerEntity player, int hearts) {
        updateData(player, player.getServer(), player.getGameProfile(), hearts);
    }

    public static void updateData(@Nullable PlayerEntity player, @Nullable MinecraftServer server, GameProfile profile, int hearts) {
        if (player != null) updateHearts(player, hearts);
        if (server != null) updateBan(server, profile, hearts);
    }

    private static void updateHearts(PlayerEntity player, int hearts) {
        EntityAttributeInstance entityAttributeInstance = player.getAttributes().getCustomInstance(EntityAttributes.MAX_HEALTH);
        if (entityAttributeInstance != null) {
            entityAttributeInstance.removeModifier(HEARTS_ID);
            entityAttributeInstance.addPersistentModifier(new EntityAttributeModifier(HEARTS_ID, (hearts * 2) - 20, EntityAttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void updateBan(MinecraftServer server, GameProfile profile, int hearts) {
        if (hearts == 0) {
            deathban(server, profile);
        } else {
            unban(server, profile);
        }
    }

    private static void deathban(MinecraftServer server, GameProfile profile) {
        BannedPlayerList bannedPlayerList = server.getPlayerManager().getUserBanList();

        if (!bannedPlayerList.contains(profile)) {
            BannedPlayerEntry bannedPlayerEntry = new BannedPlayerEntry(profile, null, "zero_heart_ban", null, "Zero-Heart Deathban (may be revoked at any time via revives)");
            bannedPlayerList.add(bannedPlayerEntry);

            ServerPlayerEntity player = server.getPlayerManager().getPlayer(profile.getId());
            if (player != null) {
                player.networkHandler.disconnect(Text.translatable("divorcesteal.deathban"));
            }

            for (ServerPlayerEntity globalPlayer : server.getPlayerManager().getPlayerList()) {
                // Add deathban sound!
                Text banAnnouncement = Text.translatable("divorcesteal.deathban_global", profile.getName());
                globalPlayer.sendMessageToClient(banAnnouncement, false);
            }
        }
    }

    private static void unban(MinecraftServer server, GameProfile profile) {
        BannedPlayerList bannedPlayerList = server.getPlayerManager().getUserBanList();

        if (bannedPlayerList.contains(profile)) {
            bannedPlayerList.remove(profile);
            for (ServerPlayerEntity globalPlayer : server.getPlayerManager().getPlayerList()) {
                // Add revive sound!
            }
        }
    }
}
