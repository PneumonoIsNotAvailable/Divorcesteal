package net.pneumono.divorcesteal.registry;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pneumono.divorcesteal.Divorcesteal;
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

    private static void join(ServerPlayerEntity player) {
        ParticipantHeartData data = Hearts.getParticipantHeartData(player);
        if (data != null) {
            data.setName(player.getGameProfile().getName());
            Hearts.updateData(player);
        }
    }

    private static void afterDeath(Entity entity, DamageSource damageSource) {
        if (!(entity instanceof ServerPlayerEntity target) || !Hearts.isParticipant(target)) return;

        Hearts.addHeartsValidated(target, -1, true);

        if (
                target.getAttacker() instanceof ServerPlayerEntity attacker &&
                Hearts.isParticipant(attacker) &&
                !attacker.getUuid().equals(target.getUuid())
        ) {
            ItemStack headStack = new ItemStack(Items.PLAYER_HEAD);

            ProfileComponent targetComponent = createProfileComponent(target);
            if (targetComponent != null) {
                headStack.set(DataComponentTypes.PROFILE, targetComponent);
            }
            ProfileComponent attackerComponent = createProfileComponent(attacker);
            if (attackerComponent != null) {
                headStack.set(DivorcestealRegistry.KILLED_BY_COMPONENT, new KilledByComponent(attackerComponent));
            }

            ItemEntity headItemEntity = target.dropItem(headStack, true, false);
            if (headItemEntity != null) {
                headItemEntity.resetPickupDelay();
            }

            ParticipantHeartData data = Hearts.getParticipantHeartData(target);
            if (data != null && data.isBanned()) {
                target.incrementStat(DivorcestealRegistry.DEATHBAN_SELF_STAT);
                attacker.incrementStat(DivorcestealRegistry.DEATHBAN_PLAYER_STAT);
            }

            attacker.incrementStat(DivorcestealRegistry.STEAL_LIFE_STAT);
            if (Hearts.addHeartsValidated(attacker, 1, false) == 0) {

                ItemStack heartStack = new ItemStack(DivorcestealRegistry.HEART_ITEM);
                if (!attacker.getInventory().insertStack(heartStack)) {
                    ItemEntity heartItemEntity = attacker.dropItem(heartStack, false);
                    if (heartItemEntity != null) {
                        heartItemEntity.resetPickupDelay();
                    }
                }
            }
        }
    }

    // Copied from elsewhere idfk what this is
    private static ProfileComponent createProfileComponent(ServerPlayerEntity player) {
        String texturePropertyValue = "";
        for (Property textureProperty : player.getGameProfile().getProperties().get("textures")) {
            if (textureProperty.name().equals("textures")) {
                texturePropertyValue = textureProperty.value();
                break;
            }
        }

        if (!texturePropertyValue.contains("cHJvZmlsZUlk")) {
            Divorcesteal.LOGGER.info("Could not create profile component for player {}. I think this is due to authentication issues???", player.getGameProfile().getName());
            return null;
        }

        String textures = "ewogICJ0aW1lc3RhbXAiIDogMCwKICAicHJvZmlsZUlk" + texturePropertyValue.split("cHJvZmlsZUlk")[1];

        GameProfile newProfile = new GameProfile(player.getGameProfile().getId(), player.getGameProfile().getName());
        newProfile.getProperties().put("textures", new Property("textures", textures));

        return new ProfileComponent(newProfile);
    }

    private static void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        Hearts.updateData(newPlayer);
    }

    private static void startWorldTick(ServerWorld world) {
        if (world.getTime() % 200 != 0) return;

        if (DivorcestealConfig.REVIVE_DAYS.getValue() < 0) return;

        HeartDataState state = Hearts.getHeartDataState();
        for (ParticipantHeartData data : state.getHeartDataList().stream().toList()) {

            if (data.getBanDate() != null && DateUtils.addDays(
                    data.getBanDate(), DivorcestealConfig.REVIVE_DAYS.getValue()
            ).before(new Date())) {

                Hearts.revive(world, data.getGameProfile());
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
