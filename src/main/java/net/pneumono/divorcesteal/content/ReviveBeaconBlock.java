package net.pneumono.divorcesteal.content;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.pneumono.divorcesteal.content.component.KillTargetComponent;
import net.pneumono.divorcesteal.hearts.HeartsUtil;
import net.pneumono.divorcesteal.hearts.ParticipantMap;
import net.pneumono.divorcesteal.hearts.Participant;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public class ReviveBeaconBlock extends BaseEntityBlock {
    public static final MapCodec<ReviveBeaconBlock> CODEC = simpleCodec(ReviveBeaconBlock::new);

    public ReviveBeaconBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new ReviveBeaconBlockEntity(pos, state);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NonNull BlockState state, Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ReviveBeaconBlockEntity blockEntity) {
            KillTargetComponent killTargetComponent = blockEntity.getOrCreateTarget(player.getUUID());
            if (killTargetComponent == null) {
                player.displayClientMessage(Component.translatable("block.divorcesteal.revive_beacon.fail_roll"), true);
                return InteractionResult.SUCCESS;
            }

            player.awardStat(DivorcestealRegistry.INTERACT_WITH_REVIVE_BEACON_STAT);

            OptionalInt optionalInt = player.openMenu(blockEntity);
            if (player instanceof ServerPlayer serverPlayer && optionalInt.isPresent()) {
                sendBeaconUpdatePacket(serverPlayer,
                        optionalInt.getAsInt(),
                        killTargetComponent.nameAndId(),
                        getRevivableParticipants()
                );
            }
        }

        return InteractionResult.SUCCESS;
    }

    public static List<NameAndId> getRevivableParticipants() {
        ParticipantMap map = HeartsUtil.getParticipantMap();
        return map.getParticipants().stream().filter(Participant::isBanned).map(Participant::getNameAndId).toList();
    }

    public static Optional<NameAndId> getRandomTarget(ServerLevel level, UUID except) {
        ParticipantMap map = HeartsUtil.getParticipantMap();
        List<Participant> unbannedList = map.getParticipants().stream().filter(participant -> !participant.isBanned()).toList();
        List<Participant> filteredUnbannedList = unbannedList.stream().filter(participant -> !participant.getUuid().equals(except)).toList();
        if (!filteredUnbannedList.isEmpty()) {
            unbannedList = filteredUnbannedList;
        }

        if (unbannedList.isEmpty()) return Optional.empty();

        RandomSource random = level.getRandom();
        return Optional.of(unbannedList.get(random.nextIntBetweenInclusive(0, unbannedList.size() - 1)).getNameAndId());
    }

    public static boolean reviveParticipant(ServerLevel level, BlockPos pos, UUID uuid, Player reviver) {
        if (HeartsUtil.revive(level, uuid)) {
            level.playSound(null, pos, DivorcestealRegistry.USE_REVIVE_BEACON_SOUND, SoundSource.PLAYERS);
            reviver.awardStat(DivorcestealRegistry.REVIVE_PLAYER_STAT);
            return true;
        } else {
            return false;
        }
    }

    public static void sendBeaconUpdatePacket(ServerPlayer player, int containerId, NameAndId target, List<NameAndId> revivableParticipants) {
        ServerPlayNetworking.send(player, new ReviveBeaconInfoS2CPayload(
                containerId,
                target,
                revivableParticipants
        ));
    }
}
