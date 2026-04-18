package net.pneumono.aprilfools.gacha.content;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.pneumono.aprilfools.gacha.GachaRarity;
import net.pneumono.aprilfools.gacha.GachaRegistry;
import net.pneumono.aprilfools.gacha.networking.GachaBeaconResultS2CPayload;
import net.pneumono.aprilfools.gacha.networking.GachaBeaconSpinDataS2CPayload;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class GachaBeaconBlockEntity extends BlockEntity implements MenuProvider {
    private GachaBeaconState state = GachaBeaconState.UNROLLED;
    // Null if state = UNROLLED
    @Nullable
    private GachaResult finalResult = null;
    // Null if state = ROLLED
    // Length = 40;
    @Nullable
    private List<GachaResult> possibleResults = null;
    // 0 once roll finishes
    private float randomSpinMultiplier = 0;
    private int spinTicks = -1;
    private long age = 0;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int i) {
            if (i == 0) {
                return GachaBeaconBlockEntity.this.spinTicks;
            } else if (i == 1) {
                return switch (GachaBeaconBlockEntity.this.state) {
                    case UNROLLED -> 0;
                    case ROLLING -> 1;
                    case ROLLED -> 2;
                };
            } else {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public void set(int i, int j) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public GachaBeaconBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(GachaRegistry.GACHA_BEACON_ENTITY, blockPos, blockState);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);

        valueOutput.storeNullable("final_result", GachaResult.CODEC, this.finalResult);
        valueOutput.storeNullable("possible_results", GachaResult.CODEC.listOf(), this.possibleResults);
        if (this.randomSpinMultiplier != 0) {
            valueOutput.putFloat("random_spin_multiplier", this.randomSpinMultiplier);
        }
        if (this.spinTicks != -1) {
            valueOutput.putInt("spin_ticks", this.spinTicks);
        }
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput valueInput) {
        super.loadAdditional(valueInput);

        this.finalResult = valueInput.read("final_result", GachaResult.CODEC).orElse(null);
        this.possibleResults = valueInput.read("possible_results", GachaResult.CODEC.listOf()).orElse(null);
        this.randomSpinMultiplier = valueInput.getFloatOr("random_spin_multiplier", 0);
        this.spinTicks = valueInput.getIntOr("spin_ticks", -1);

        if (this.finalResult == null) {
            this.state = GachaBeaconState.UNROLLED;
        } else if (this.possibleResults == null) {
            this.state = GachaBeaconState.ROLLED;
        } else {
            this.state = GachaBeaconState.ROLLING;
        }
    }

    @Override
    protected void applyImplicitComponents(@NonNull DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.finalResult = components.get(GachaRegistry.GACHA_DATA_COMPONENT);
        if (this.finalResult != null) {
            this.state = GachaBeaconState.ROLLED;
        } else {
            GachaRoll roll = components.get(GachaRegistry.GACHA_ROLL_COMPONENT);
            if (roll != null) {
                this.randomSpinMultiplier = roll.randomSpinMultiplier();
                this.possibleResults = roll.possibleResults();
            }
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.@NonNull Builder builder) {
        super.collectImplicitComponents(builder);
        if (this.state == GachaBeaconState.ROLLED) {
            builder.set(GachaRegistry.GACHA_DATA_COMPONENT, this.finalResult);
        } else if (this.possibleResults != null) {
            builder.set(GachaRegistry.GACHA_ROLL_COMPONENT, new GachaRoll(this.randomSpinMultiplier, this.possibleResults));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeComponentsFromTag(@NonNull ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard("possible_results");
        output.discard("spin_ticks");
    }

    public GachaBeaconState getCurrentState() {
        return this.state;
    }

    public void beginRolling() {
        this.spinTicks++;
        validatePreSpinValues();
        this.finalResult = GachaBeaconBlock.calcuateFinalResult(this.randomSpinMultiplier, this.possibleResults);
        setChangedAndUpdateBlocks(getLevel());
        this.state = GachaBeaconState.ROLLING;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GachaBeaconBlockEntity entity) {
        commonTick(level, pos, state, entity);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, GachaBeaconBlockEntity entity) {
        boolean finishRoll = commonTick(level, pos, state, entity);

        entity.age++;

        if (shouldPlayTickSound(entity.spinTicks, entity.randomSpinMultiplier)) {
            playTickSound(level, pos);
        }

        GachaResult finalResult = entity.finalResult;
        if (finishRoll && finalResult != null) {
            addSuccessParticles(level, pos, finalResult);
            playSuccessSound(level, pos, finalResult);
        }

        RandomSource random = level.getRandom();
        if (entity.state == GachaBeaconState.ROLLED && finalResult != null && finalResult.rarity() == GachaRarity.MYTHIC && random.nextFloat() >= 0.9) {
            GachaRarityParticleOption particleOption = new GachaRarityParticleOption(GachaRegistry.GACHA_FINISH_PARTICLE, GachaRarity.MYTHIC);
            level.addParticle(
                    particleOption,
                    pos.getX() - 0.5 + random.nextFloat() + random.nextFloat(), pos.getY() - 0.5 + random.nextFloat() + random.nextFloat(), pos.getZ() - 0.5 + random.nextFloat() + random.nextFloat(),
                    0, 0, 0
            );
        }
    }

    public static boolean commonTick(Level level, BlockPos pos, BlockState state, GachaBeaconBlockEntity entity) {
        if (entity.spinTicks >= 0) {
            if (entity.spinTicks < GachaBeaconBlock.totalTicks(entity.randomSpinMultiplier)) {
                entity.state = GachaBeaconState.ROLLING;
                entity.spinTicks++;
            } else {
                entity.state = GachaBeaconState.ROLLED;
                entity.spinTicks = -1;
                entity.possibleResults = null;
                entity.randomSpinMultiplier = 0;
                return true;
            }
        }
        return false;
    }

    private void validatePreSpinValues() {
        boolean changed = false;
        if (this.possibleResults == null) {
            this.possibleResults = GachaBeaconBlock.generateRandomResultList(getLevel().getRandom());
            changed = true;
        }
        if (this.randomSpinMultiplier == 0) {
            this.randomSpinMultiplier = (float) (((getLevel().getRandom().nextFloat() - 0.5) / 5f) + 1);
            changed = true;
        }
        if (changed) {
            this.setChangedAndUpdateBlocks(getLevel());
        }
    }

    public static boolean shouldPlayTickSound(int spinTicks, float randomSpinMultiplier) {
        int previousChanges = GachaBeaconBlock.spinTickIntFunction(spinTicks - 1, randomSpinMultiplier);
        int changes = GachaBeaconBlock.spinTickIntFunction(spinTicks, randomSpinMultiplier);
        return previousChanges != changes;
    }

    public static void playTickSound(Level level, BlockPos pos) {
        level.playLocalSound(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.BLOCKS,
                3.0F, (float) Math.pow(2, 1/2f), false
        );
    }

    public static void addSuccessParticles(Level level, BlockPos pos, GachaResult result) {
        GachaRarity rarity = result.rarity();
        GachaRarityParticleOption particleOption = new GachaRarityParticleOption(GachaRegistry.GACHA_FINISH_PARTICLE, rarity);

        double particleX = pos.getX() + 0.5;
        double particleY = pos.getY() + 1;
        double particleZ = pos.getZ() + 0.5;

        int particleCount = switch (rarity) {
            case COMMON -> 1;
            case UNCOMMON -> 2;
            case RARE -> 3;
            case EPIC -> 6;
            case LEGENDARY -> 8;
            case MYTHIC -> 12;
        };

        RandomSource random = level.getRandom();
        for (int i = 0; i < particleCount; i++) {
            double forceX = (random.nextDouble() - 0.5) / 1.5;
            double forceY = random.nextDouble() / 3;
            double forceZ = (random.nextDouble() - 0.5) / 1.5;
            level.addParticle(
                    particleOption,
                    particleX, particleY, particleZ,
                    forceX, forceY, forceZ
            );
        }
    }

    public static void playSuccessSound(Level level, BlockPos pos, GachaResult result) {
        GachaRarity rarity = result.rarity();

        switch (rarity) {
            case COMMON -> level.playLocalSound(
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS,
                    3.0F, 1, false
            );
            case UNCOMMON -> level.playLocalSound(
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS,
                    3.0F, (float) Math.pow(2, (17 - 12) / 12f), false
            );
            case RARE -> level.playLocalSound(
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.BLOCKS,
                    3.0F, (float) Math.pow(2, (17 - 12) / 12f), false
            );
            case EPIC -> level.playLocalSound(
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS,
                    3.0F, 1, false
            );
            case LEGENDARY -> level.playLocalSound(
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.WITHER_AMBIENT, SoundSource.BLOCKS,
                    3.0F, 1, false
            );
            case MYTHIC -> level.playLocalSound(
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.ENDER_DRAGON_AMBIENT, SoundSource.BLOCKS,
                    3.0F, 1, false
            );
        }
    }

    public void setChangedAndUpdateBlocks(Level level) {
        setChanged();
        level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public void sendClientScreenData(Player player, int containerId) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (this.state == GachaBeaconState.ROLLED) {
                ServerPlayNetworking.send(serverPlayer, new GachaBeaconResultS2CPayload(containerId, this.finalResult));
            } else {
                validatePreSpinValues();
                ServerPlayNetworking.send(serverPlayer, new GachaBeaconSpinDataS2CPayload(containerId, this.possibleResults, this.randomSpinMultiplier));
            }
        }
    }

    public long getClientRenderAge() {
        return this.age;
    }

    public @Nullable GachaResult getFinalResult() {
        return finalResult;
    }

    public @Nullable List<GachaResult> getPossibleResults() {
        return possibleResults;
    }

    public float getRandomSpinMultiplier() {
        return randomSpinMultiplier;
    }

    public int getSpinTicks() {
        return spinTicks;
    }

    @Override
    public @NonNull Component getDisplayName() {
        return Component.translatable("block.divorcesteal.gacha_beacon");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, @NonNull Inventory inventory, @NonNull Player player) {
        return new GachaBeaconMenu(
                i,
                inventory,
                ContainerLevelAccess.create(getLevel(), getBlockPos()),
                this.dataAccess
        );
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
