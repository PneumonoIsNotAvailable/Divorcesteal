package net.pneumono.divorcesteal.content;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
        if (reference.getHearts() == 0) reference.setHearts(Hearts.REVIVE_HEARTS.get());
        Hearts.updateData(player);
    }

    private static void afterDeath(Entity entity, DamageSource damageSource) {
        if (!(entity instanceof ServerPlayerEntity player)) return;

        Hearts.addHeartsValidated(player, -1, true);

        if (player.getAttacker() instanceof ServerPlayerEntity attacker && !attacker.getUuid().equals(entity.getUuid())) {
            if (Hearts.addHeartsValidated(attacker, 1, false) == 0) {

                ItemStack stack = DivorcestealRegistry.HEART_ITEM.getDefaultStack();
                if (!stack.isEmpty() && !attacker.getInventory().insertStack(stack)) {
                    ItemEntity itemEntity = attacker.dropItem(stack, false);
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
        if (world.getTime() % 200 != 0 || Hearts.BAN_TIME.get() < 0) return;

        BannedPlayerList bannedPlayerList = world.getServer().getPlayerManager().getUserBanList();

        Date now = new Date();
        for (BannedPlayerEntry entry : bannedPlayerList.values()) {
            if (entry.getSource().equals(Hearts.ZERO_HEART_BAN_ID) && DateUtils.addDays(entry.getCreationDate(), Hearts.BAN_TIME.get()).before(now)) {
                bannedPlayerList.remove(entry);
            }
        }
    }
}
