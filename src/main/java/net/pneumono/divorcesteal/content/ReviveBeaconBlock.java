package net.pneumono.divorcesteal.content;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.pneumono.divorcesteal.content.component.KillTargetComponent;
import net.pneumono.divorcesteal.hearts.HeartDataState;
import net.pneumono.divorcesteal.hearts.Hearts;
import net.pneumono.divorcesteal.hearts.ParticipantHeartData;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReviveBeaconBlockEntity(pos, state);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ReviveBeaconBlockEntity blockEntity) {
            KillTargetComponent killTargetComponent = blockEntity.getOrCreateTarget(player.getUUID());
            if (killTargetComponent == null) {
                player.displayClientMessage(Component.translatable("block.divorcesteal.revive_beacon.fail_roll"), true);
                return InteractionResult.FAIL;
            }

            player.awardStat(DivorcestealRegistry.INTERACT_WITH_REVIVE_BEACON_STAT);

            OptionalInt optionalInt = player.openMenu(blockEntity);
            if (player instanceof ServerPlayer serverPlayer && optionalInt.isPresent()) {
                sendBeaconUpdatePacket(serverPlayer,
                        optionalInt.getAsInt(),
                        killTargetComponent.profile(),
                        getRevivableParticipants()
                );
            }
        }

        return InteractionResult.SUCCESS;
    }

    public static List<ResolvableProfile> getRevivableParticipants() {
        HeartDataState state = Hearts.getHeartDataState();
        return state.getHeartDataList().stream().filter(ParticipantHeartData::isBanned).map(data -> new ResolvableProfile(data.getGameProfile())).toList();
    }

    public static Optional<GameProfile> getRandomTarget(ServerLevel level, UUID except) {
        HeartDataState state = Hearts.getHeartDataState();
        List<ParticipantHeartData> unbannedList = state.getHeartDataList().stream().filter(data -> !data.isBanned()).toList();
        List<ParticipantHeartData> filteredUnbannedList = unbannedList.stream().filter(data -> !data.getUuid().equals(except)).toList();
        if (!filteredUnbannedList.isEmpty()) {
            unbannedList = filteredUnbannedList;
        }

        if (unbannedList.isEmpty()) return Optional.empty();

        RandomSource random = level.getRandom();
        return Optional.of(unbannedList.get(random.nextIntBetweenInclusive(0, unbannedList.size() - 1)).getGameProfile());
    }

    public static boolean reviveParticipant(ServerLevel level, BlockPos pos, GameProfile participant, Player reviver) {
        if (Hearts.revive(level, participant)) {
            level.playSound(null, pos, DivorcestealRegistry.USE_REVIVE_BEACON_SOUND, SoundSource.PLAYERS);
            reviver.awardStat(DivorcestealRegistry.REVIVE_PLAYER_STAT);
            return true;
        } else {
            return false;
        }
    }

    public static void sendBeaconUpdatePacket(ServerPlayer player, int containerId, ResolvableProfile target, List<ResolvableProfile> revivableParticipants) {
        ServerPlayNetworking.send(player, new ReviveBeaconInfoS2CPayload(
                containerId,
                target,
                revivableParticipants
        ));
    }
}
