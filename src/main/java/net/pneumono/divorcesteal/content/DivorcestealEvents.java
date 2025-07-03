package net.pneumono.divorcesteal.content;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.pneumono.divorcesteal.hearts.Hearts;

public class DivorcestealEvents {
    public static void registerDivorcestealEvents() {
        ServerPlayerEvents.JOIN.register(DivorcestealEvents::join);
        ServerLivingEntityEvents.AFTER_DEATH.register(DivorcestealEvents::afterDeath);
        ServerPlayerEvents.AFTER_RESPAWN.register(DivorcestealEvents::afterRespawn);
    }

    private static void join(ServerPlayerEntity player) {
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
}
