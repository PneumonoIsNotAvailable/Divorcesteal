package net.pneumono.divorcesteal.registry;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.pneumono.divorcesteal.DivorcestealConfig;
import net.pneumono.divorcesteal.content.component.KilledByComponent;
import net.pneumono.divorcesteal.hearts.*;
import org.apache.commons.lang3.time.DateUtils;

import java.util.*;

public class DivorcestealEvents {
    public static void registerDivorcestealEvents() {
        ServerPlayerEvents.JOIN.register(DivorcestealEvents::join);
        ServerLivingEntityEvents.AFTER_DEATH.register(DivorcestealEvents::afterDeath);
        ServerPlayerEvents.AFTER_RESPAWN.register(DivorcestealEvents::afterRespawn);
        ServerTickEvents.START_WORLD_TICK.register(DivorcestealEvents::startWorldTick);
        ServerLifecycleEvents.SERVER_STARTING.register(DivorcestealEvents::serverStarting);
        ServerLifecycleEvents.AFTER_SAVE.register(DivorcestealEvents::afterSave);
    }

    private static void join(ServerPlayer player) {
        ParticipantHeartData data = Hearts.getParticipantHeartData(player);
        if (data != null) {
            data.setName(player.getGameProfile().getName());
            Hearts.updateData(player);
        }
    }

    private static void afterDeath(Entity entity, DamageSource damageSource) {
        if (!(entity instanceof ServerPlayer target) || !Hearts.isParticipant(target)) return;

        Hearts.addHeartsValidated(target, -1, true);

        if (
                target.getLastHurtByMob() instanceof ServerPlayer attacker &&
                Hearts.isParticipant(attacker) &&
                !attacker.getUUID().equals(target.getUUID())
        ) {
            ItemStack headStack = new ItemStack(Items.PLAYER_HEAD);

            headStack.set(DataComponents.PROFILE, new ResolvableProfile(target.getGameProfile()));
            headStack.set(DivorcestealRegistry.KILLED_BY_COMPONENT, new KilledByComponent(attacker.getGameProfile()));

            ItemEntity headItemEntity = target.drop(headStack, true, false);
            if (headItemEntity != null) {
                headItemEntity.setNoPickUpDelay();
            }

            ParticipantHeartData data = Hearts.getParticipantHeartData(target);
            if (data != null && data.isBanned()) {
                target.awardStat(DivorcestealRegistry.DEATHBAN_SELF_STAT);
                attacker.awardStat(DivorcestealRegistry.DEATHBAN_PLAYER_STAT);
            }

            attacker.awardStat(DivorcestealRegistry.STEAL_LIFE_STAT);
            if (Hearts.addHeartsValidated(attacker, 1, false) == 0) {

                ItemStack heartStack = new ItemStack(DivorcestealRegistry.HEART_ITEM);
                ItemEntity heartItemEntity = target.drop(heartStack, true, false);
                if (heartItemEntity != null) {
                    heartItemEntity.setNoPickUpDelay();
                }
            }
        }
    }

    private static void afterRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        Hearts.updateData(newPlayer);
    }

    private static void startWorldTick(ServerLevel level) {
        if (level.getGameTime() % 200 != 0) return;

        if (DivorcestealConfig.REVIVE_DAYS.getValue() < 0) return;

        HeartDataState state = Hearts.getHeartDataState();
        for (ParticipantHeartData data : state.getHeartDataList().stream().toList()) {

            if (data.getBanDate() != null && DateUtils.addDays(
                    data.getBanDate(), DivorcestealConfig.REVIVE_DAYS.getValue()
            ).before(new Date())) {

                Hearts.revive(level, data.getGameProfile());
            }
        }
    }

    private static void serverStarting(MinecraftServer server) {
        DataSaving.backupAndLoadHeartDataState(server);
    }

    private static void afterSave(MinecraftServer server, boolean flush, boolean force) {
        DataSaving.save();
    }
}
