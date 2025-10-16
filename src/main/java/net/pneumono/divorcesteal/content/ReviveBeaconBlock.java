package net.pneumono.divorcesteal.content;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.pneumono.divorcesteal.content.component.KillTargetComponent;
import net.pneumono.divorcesteal.hearts.HeartDataState;
import net.pneumono.divorcesteal.hearts.Hearts;
import net.pneumono.divorcesteal.hearts.ParticipantHeartData;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public class ReviveBeaconBlock extends BlockWithEntity {
    public static final MapCodec<ReviveBeaconBlock> CODEC = createCodec(ReviveBeaconBlock::new);

    public ReviveBeaconBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ReviveBeaconBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient() && world.getBlockEntity(pos) instanceof ReviveBeaconBlockEntity blockEntity) {
            KillTargetComponent killTargetComponent = blockEntity.getOrCreateTarget(player.getUuid());
            if (killTargetComponent == null) return ActionResult.FAIL;

            OptionalInt optionalInt = player.openHandledScreen(blockEntity);
            if (player instanceof ServerPlayerEntity serverPlayer && optionalInt.isPresent()) {
                sendBeaconUpdatePacket(serverPlayer,
                        optionalInt.getAsInt(),
                        killTargetComponent.profile(),
                        getRevivableParticipants()
                );
            }
        }

        return ActionResult.SUCCESS;
    }

    public static List<ProfileComponent> getRevivableParticipants() {
        HeartDataState state = Hearts.getHeartDataState();
        return state.getHeartDataList().stream().filter(ParticipantHeartData::isBanned).map(data -> new ProfileComponent(data.getGameProfile())).toList();
    }

    public static Optional<GameProfile> getRandomTarget(ServerWorld world, UUID except) {
        HeartDataState state = Hearts.getHeartDataState();
        List<ParticipantHeartData> unbannedList = state.getHeartDataList().stream().filter(data -> !data.isBanned()).toList();
        List<ParticipantHeartData> filteredUnbannedList = unbannedList.stream().filter(data -> !data.getUuid().equals(except)).toList();
        if (!filteredUnbannedList.isEmpty()) {
            unbannedList = filteredUnbannedList;
        }

        if (unbannedList.isEmpty()) return Optional.empty();

        Random random = world.getRandom();
        return Optional.of(unbannedList.get(random.nextBetween(0, unbannedList.size() - 1)).getGameProfile());
    }

    public static void reviveParticipant(ServerWorld world, BlockPos pos, GameProfile participant, PlayerEntity reviver) {
        world.playSound(null, pos, DivorcestealRegistry.USE_REVIVE_BEACON_SOUND, SoundCategory.PLAYERS);
        reviver.incrementStat(DivorcestealRegistry.REVIVE_PLAYER_STAT);
        Hearts.revive(world, participant);
    }

    public static void sendBeaconUpdatePacket(ServerPlayerEntity player, int syncId, ProfileComponent target, List<ProfileComponent> revivableParticipants) {
        ServerPlayNetworking.send(player, new ReviveBeaconInfoS2CPayload(
                syncId,
                target,
                revivableParticipants
        ));
    }

    @Nullable
    @Override
    protected NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return super.createScreenHandlerFactory(state, world, pos);
    }
}
