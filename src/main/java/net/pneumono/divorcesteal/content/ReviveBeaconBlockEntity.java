package net.pneumono.divorcesteal.content;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeamEmitter;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.pneumono.divorcesteal.content.component.KillTargetComponent;
import net.pneumono.divorcesteal.hearts.HeartDataState;
import net.pneumono.divorcesteal.hearts.Hearts;
import net.pneumono.divorcesteal.hearts.PlayerHeartData;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ReviveBeaconBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, BeamEmitter {
    private KillTargetComponent target;

    public ReviveBeaconBlockEntity(BlockPos pos, BlockState state) {
        super(DivorcestealRegistry.REVIVE_BEACON_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, ReviveBeaconBlockEntity blockEntity) {

    }

    @Override
    public List<BeamSegment> getBeamSegments() {
        return List.of();
    }

    public KillTargetComponent getOrCreateTarget() {
        if (this.target != null) return target;

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            GameProfile randomTarget = getRandomTarget(serverWorld).orElse(null);
            if (randomTarget != null) {
                this.target = new KillTargetComponent(randomTarget);
                return this.target;
            }
        }

        return this.target;
    }

    public static List<GameProfile> getRevivablePlayers(ServerWorld world) {
        HeartDataState state = Hearts.getHeartDataState(world);
        return state.getHeartDataList().stream().filter(data -> !data.isBanned()).map(PlayerHeartData::gameProfile).toList();
    }

    public static Optional<GameProfile> getRandomTarget(ServerWorld world) {
        HeartDataState state = Hearts.getHeartDataState(world);
        List<PlayerHeartData> unbannedList = state.getHeartDataList().stream().filter(data -> !data.isBanned()).toList();

        if (unbannedList.isEmpty()) return Optional.empty();

        Random random = world.getRandom();
        return Optional.of(unbannedList.get(random.nextBetween(0, unbannedList.size() - 1)).gameProfile());
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return null;

        KillTargetComponent target = getOrCreateTarget();
        return new ReviveBeaconScreenHandler(syncId, playerInventory,
                ScreenHandlerContext.create(player.getWorld(), player.getBlockPos()),
                getRevivablePlayers(serverWorld),
                target == null ? null : target.gameProfile()
        );
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.target = view.read("target", KillTargetComponent.CODEC).orElse(null);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putNullable("target", KillTargetComponent.CODEC, getOrCreateTarget());
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        this.target = components.get(DivorcestealRegistry.KILL_TARGET_COMPONENT);
    }

    @Override
    protected void addComponents(ComponentMap.Builder builder) {
        super.addComponents(builder);
        if (getOrCreateTarget() != null) {
            builder.add(DivorcestealRegistry.KILL_TARGET_COMPONENT, this.target);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeFromCopiedStackData(WriteView view) {
        view.remove("target");
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("divorcesteal.gui.revive.title");
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return this.createComponentlessNbt(registries);
    }
}
