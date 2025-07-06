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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pneumono.divorcesteal.DivorcestealConfig;
import net.pneumono.divorcesteal.content.KillerComponent;
import net.pneumono.divorcesteal.hearts.HeartDataState;
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
        Hearts.updateData(player);
    }

    private static void afterDeath(Entity entity, DamageSource damageSource) {
        if (!(entity instanceof ServerPlayerEntity player)) return;

        Hearts.addHeartsValidated(player, -1, true);

        if (player.getAttacker() instanceof ServerPlayerEntity attacker && !attacker.getUuid().equals(entity.getUuid())) {

            ItemStack headStack = Items.PLAYER_HEAD.getDefaultStack().copy();
            headStack.set(DataComponentTypes.PROFILE, new ProfileComponent(player.getGameProfile()));
            headStack.set(DivorcestealRegistry.KILLER, new KillerComponent(attacker.getDisplayName()));
            player.dropItem(headStack, true, false);

            if (PlayerHeartDataReference.create(player).isBanned()) {
                player.incrementStat(DivorcestealRegistry.DEATHBAN_SELF_STAT);
                attacker.incrementStat(DivorcestealRegistry.DEATHBAN_PLAYER_STAT);
            }

            attacker.incrementStat(DivorcestealRegistry.STEAL_LIFE_STAT);
            if (Hearts.addHeartsValidated(attacker, 1, false) == 0) {

                ItemStack heartStack = DivorcestealRegistry.HEART_ITEM.getDefaultStack();
                if (!attacker.getInventory().insertStack(heartStack)) {
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
        if (world.getTime() % 200 != 0) return;

        if (DivorcestealConfig.REVIVE_DAYS.getValue() < 0) return;

        HeartDataState state = Hearts.getHeartDataState(world);
        for (PlayerHeartDataReference reference : state.getHeartDataList().stream().map(data -> new PlayerHeartDataReference(state, data)).toList()) {

            if (reference.getBanDate().isPresent() && DateUtils.addDays(
                    reference.getBanDate().get(), DivorcestealConfig.REVIVE_DAYS.getValue()
            ).before(new Date())) {

                Hearts.unban(world.getServer(), reference);
                Hearts.updateData(null, world.getServer(), reference);
            }
        }
    }
}
