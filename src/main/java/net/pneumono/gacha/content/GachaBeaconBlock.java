package net.pneumono.gacha.content;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.pneumono.divorcesteal.hearts.HeartsUtil;
import net.pneumono.divorcesteal.hearts.Participant;
import net.pneumono.gacha.GachaRarity;
import net.pneumono.gacha.GachaRegistry;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public class GachaBeaconBlock extends BaseEntityBlock {
    public static final MapCodec<GachaBeaconBlock> CODEC = simpleCodec(GachaBeaconBlock::new);

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public static final int POSSIBLE_RESULT_CAP = 50;
    public static final int LEVER_PULL_TICKS = 10;
    public static final int AVERAGE_CHANGES = 40;

    public GachaBeaconBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new GachaBeaconBlockEntity(pos, state);
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hit) {
        if (!level.isClientSide() && canFunction(level) && level.getBlockEntity(pos) instanceof GachaBeaconBlockEntity blockEntity) {
            player.awardStat(GachaRegistry.INTERACT_WITH_GACHA_BEACON_STAT);

            OptionalInt optionalInt = player.openMenu(blockEntity);
            if (optionalInt.isPresent()) {
                blockEntity.sendClientScreenData(player, optionalInt.getAsInt());
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(
            @NonNull BlockState state,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull Block block,
            @Nullable Orientation orientation,
            boolean bl
    ) {
        if (level.getBlockEntity(pos) instanceof GachaBeaconBlockEntity blockEntity && blockEntity.getCurrentState() == GachaBeaconState.UNROLLED) {
            boolean receivingPower = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
            if (receivingPower) blockEntity.beginRolling();
        }
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NonNull Level level, @NonNull BlockState blockState, @NonNull BlockEntityType<T> blockEntityType) {
        if (!canFunction(level)) {
            return null;
        }

        return createTickerHelper(blockEntityType, GachaRegistry.GACHA_BEACON_ENTITY, level.isClientSide() ? GachaBeaconBlockEntity::clientTick : GachaBeaconBlockEntity::serverTick);
    }

    public static boolean canFunction(Level level) {
        return level.isClientSide() || !HeartsUtil.getParticipantMap().getParticipants().isEmpty();
    }

    public static int totalTicks(float randomSpinMultiplier) {
        float minimumRateOfChange = 0.004f;
        float targetValue = AVERAGE_CHANGES * randomSpinMultiplier;
        return (int) (10 - targetValue * Math.log(minimumRateOfChange));
    }

    public static GachaResult calcuateFinalResult(float randomSpinMultiplier, List<GachaResult> results) {
        return calcuateResult(totalTicks(randomSpinMultiplier), randomSpinMultiplier, results);
    }

    public static GachaResult calcuateResult(int spinTicks, float randomSpinMultiplier, List<GachaResult> results) {
        int totalChanges = GachaBeaconBlock.spinTickIntFunction(spinTicks, randomSpinMultiplier);
        return totalChanges == -1 ? null : results.get(totalChanges);
    }

    /**
     * @param spinTicks The amount of time the beacon has been handling spinTicks.
     *                  Different from time spent spinning, the first 10 spinTicks are for the lever pulling
     * @param randomSpinMultiplier The value to multiply the average 40 changes by.
     * @return The amount of times the beacon has changed it's selected result.
     */
    public static int spinTickIntFunction(int spinTicks, float randomSpinMultiplier) {
        return (int) spinTickDoubleFunction(spinTicks, randomSpinMultiplier);
    }

    /**
     * @param spinTicks The amount of time the beacon has been handling spinTicks.
     *                  Different from time spent spinning, the first 10 spinTicks are for the lever pulling
     * @param randomSpinMultiplier The value to multiply the average 40 changes by.
     * @return The amount of times the beacon has changed it's selected result.
     */
    public static double spinTickDoubleFunction(float spinTicks, float randomSpinMultiplier) {
        if (randomSpinMultiplier == 0) return -1;
        float activeSpinTicks = spinTicks - LEVER_PULL_TICKS;
        if (activeSpinTicks < 0) return -1;
        float targetValue = AVERAGE_CHANGES * randomSpinMultiplier;
        return targetValue - Math.pow(Math.E, Math.log(targetValue) - (activeSpinTicks / targetValue));
    }

    @Nullable
    public static List<GachaResult> generateRandomResultList(RandomSource random) {
        List<GachaResult> results = new ArrayList<>();
        for (int i = 0; i < POSSIBLE_RESULT_CAP; ++i) {
            GachaResult result = generateRandomResult(random);
            if (result == null) return null;
            results.add(result);
        }
        return results;
    }

    @Nullable
    private static GachaResult generateRandomResult(RandomSource random) {
        int rarityValue = random.nextIntBetweenInclusive(0, 63);
        GachaRarity rarity;
        if (rarityValue == 63) {
            rarity = GachaRarity.MYTHIC;
        } else if (rarityValue > 60) {
            rarity = GachaRarity.LEGENDARY;
        } else if (rarityValue > 56) {
            rarity = GachaRarity.EPIC;
        } else if (rarityValue > 48) {
            rarity = GachaRarity.RARE;
        } else if (rarityValue > 32) {
            rarity = GachaRarity.UNCOMMON;
        } else {
            rarity = GachaRarity.COMMON;
        }

        List<NameAndId> validResults = HeartsUtil.getParticipantMap().getParticipants().stream().map(Participant::getNameAndId).toList();

        if (validResults.isEmpty()) return null;

        NameAndId nameAndId = validResults.get(random.nextIntBetweenInclusive(0, validResults.size() - 1));

        return new GachaResult(rarity, nameAndId);
    }
}
