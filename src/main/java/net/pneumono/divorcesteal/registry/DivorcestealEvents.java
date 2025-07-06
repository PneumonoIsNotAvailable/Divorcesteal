package net.pneumono.divorcesteal.registry;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pneumono.divorcesteal.DivorcestealConfig;
import net.pneumono.divorcesteal.content.KillerComponent;
import net.pneumono.divorcesteal.hearts.Hearts;
import net.pneumono.divorcesteal.hearts.PlayerHeartDataReference;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;

public class DivorcestealEvents {
    public static void registerDivorcestealEvents() {
        ServerPlayerEvents.JOIN.register(DivorcestealEvents::join);
        ServerLivingEntityEvents.AFTER_DEATH.register(DivorcestealEvents::afterDeath);
        ServerPlayerEvents.AFTER_RESPAWN.register(DivorcestealEvents::afterRespawn);
        ServerTickEvents.START_WORLD_TICK.register(DivorcestealEvents::startWorldTick);
    }

    private static void join(ServerPlayerEntity player) {
        PlayerHeartDataReference reference = PlayerHeartDataReference.create(player);
        reference.setName(player.getGameProfile().getName());
        if (reference.getHearts() == 0) reference.setHearts(DivorcestealConfig.REVIVE_HEARTS.getValue());
        Hearts.updateData(player);
    }

    private static void afterDeath(Entity entity, DamageSource damageSource) {
        if (!(entity instanceof ServerPlayerEntity player)) return;

        Hearts.addHeartsValidated(player, -1, true);

        if (player.getAttacker() instanceof ServerPlayerEntity attacker) {
            ItemStack headStack = Items.PLAYER_HEAD.getDefaultStack().copy();
            headStack.set(DataComponentTypes.PROFILE, new ProfileComponent(player.getGameProfile()));
            headStack.set(DivorcestealRegistry.KILLER, new KillerComponent(attacker.getDisplayName()));
            player.dropItem(headStack, true, false);

            if (!attacker.getUuid().equals(entity.getUuid()) && Hearts.addHeartsValidated(attacker, 1, false) == 0) {

                ItemStack heartStack = DivorcestealRegistry.HEART_ITEM.getDefaultStack();
                if (!heartStack.isEmpty() && !attacker.getInventory().insertStack(heartStack)) {
                    ItemEntity itemEntity = attacker.dropItem(heartStack, false);
                    if (itemEntity != null) {
                        itemEntity.resetPickupDelay();
                        itemEntity.setOwner(attacker.getUuid());
                    }
                }
            }
        }
    }

    private static void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        Hearts.updateData(newPlayer);
    }

    private static void startWorldTick(ServerWorld world) {
        if (world.getTime() % 200 != 0 || DivorcestealConfig.REVIVE_DAYS.getValue() < 0) return;

        BannedPlayerList bannedPlayerList = world.getServer().getPlayerManager().getUserBanList();

        Date now = new Date();
        for (BannedPlayerEntry entry : bannedPlayerList.values()) {
            if (entry.getSource().equals(Hearts.ZERO_HEART_BAN_ID) && DateUtils.addDays(entry.getCreationDate(), DivorcestealConfig.REVIVE_DAYS.getValue()).before(now)) {
                bannedPlayerList.remove(entry);
            }
        }
    }
}
