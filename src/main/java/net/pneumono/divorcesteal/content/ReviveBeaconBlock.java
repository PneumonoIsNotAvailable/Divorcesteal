package net.pneumono.divorcesteal.content;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.divorcesteal.hearts.Hearts;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, DivorcestealRegistry.REVIVE_BEACON_ENTITY, ReviveBeaconBlockEntity::tick);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && world.getBlockEntity(pos) instanceof ReviveBeaconBlockEntity blockEntity && blockEntity.canOpen()) {
            OptionalInt optionalInt = player.openHandledScreen(blockEntity);
            if (player instanceof ServerPlayerEntity serverPlayer && optionalInt.isPresent()) {
                ServerPlayNetworking.send(serverPlayer, new ReviveBeaconTargetS2CPayload(
                        optionalInt.getAsInt(),
                        blockEntity.getOrCreateTarget().profile()
                ));
            }
        }

        return ActionResult.SUCCESS;
    }

    public static void revivePlayer(ServerWorld world, BlockPos pos, GameProfile revived, PlayerEntity reviver) {
        world.playSound(null, pos, DivorcestealRegistry.USE_REVIVE_BEACON_SOUND, SoundCategory.PLAYERS);
        reviver.incrementStat(DivorcestealRegistry.REVIVE_PLAYER_STAT);
        Hearts.revive(world, revived);
    }

    @Nullable
    @Override
    protected NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return super.createScreenHandlerFactory(state, world, pos);
    }

    /*
    private static void test() {
        if (world instanceof ServerWorld serverWorld) {
            HeartDataState state = Hearts.getHeartDataState(serverWorld);
            List<PlayerHeartData> bannedList = state.getHeartDataList().stream().filter(PlayerHeartData::isBanned).toList();
            List<PlayerHeartData> unbannedList = state.getHeartDataList().stream().filter(data -> !data.isBanned()).toList();

            if (bannedList.isEmpty() || unbannedList.isEmpty()) return ActionResult.FAIL;

            ItemStack stack = user.getStackInHand(hand);
            Random random = user.getRandom();
            ProfileComponent profile = stack.getOrDefault(DivorcestealRegistry.KILL_TARGET,
                    new ProfileComponent(unbannedList.get(random.nextBetween(0, unbannedList.size() - 1)).gameProfile())
            );
            stack.set(DivorcestealRegistry.KILL_TARGET, profile);

            user.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, player) -> new ReviveBeaconScreenHandler(
                    syncId, playerInventory,
                    ScreenHandlerContext.create(world, user.getBlockPos()),
                    bannedList, profile
            ), Text.translatable("divorcesteal.gui.revive.title")));
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        return ActionResult.SUCCESS;
    }
     */
}
