package net.pneumono.divorcesteal.content;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
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
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.pneumono.divorcesteal.content.component.KillTargetComponent;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ReviveBeaconBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, Nameable {
    private KillTargetComponent target;
    @Nullable
    private Text customName;

    public ReviveBeaconBlockEntity(BlockPos pos, BlockState state) {
        super(DivorcestealRegistry.REVIVE_BEACON_ENTITY, pos, state);
    }

    public KillTargetComponent getOrCreateTarget(UUID except) {
        if (this.target != null) return target;

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            GameProfile randomTarget = ReviveBeaconBlock.getRandomTarget(serverWorld, except).orElse(null);
            if (randomTarget != null) {
                this.target = new KillTargetComponent(randomTarget);
                markDirty();
                return this.target;
            }
        }

        return this.target;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return null;

        KillTargetComponent target = getOrCreateTarget(player.getUuid());
        if (target == null) return null;

        return new ReviveBeaconScreenHandler(syncId, playerInventory,
                ScreenHandlerContext.create(serverWorld, this.getPos()),
                ReviveBeaconBlock.getRevivablePlayers(serverWorld),
                target.profile()
        );
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.target = view.read("target", KillTargetComponent.CODEC).orElse(null);
        this.customName = tryParseCustomName(view, "CustomName");
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putNullable("target", KillTargetComponent.CODEC, this.target);
        view.putNullable("CustomName", TextCodecs.CODEC, this.customName);
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        this.target = components.get(DivorcestealRegistry.KILL_TARGET_COMPONENT);
        this.customName = components.get(DataComponentTypes.CUSTOM_NAME);
    }

    @Override
    protected void addComponents(ComponentMap.Builder builder) {
        super.addComponents(builder);
        builder.add(DivorcestealRegistry.KILL_TARGET_COMPONENT, this.target);
        builder.add(DataComponentTypes.CUSTOM_NAME, this.customName);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeFromCopiedStackData(WriteView view) {
        view.remove("target");
    }

    @Override
    public Text getName() {
        return this.customName == null ? Text.translatable("divorcesteal.gui.revive_beacon.title") : this.customName;
    }

    @Override
    public Text getDisplayName() {
        return getName();
    }

    @Nullable
    @Override
    public Text getCustomName() {
        return this.customName;
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return this.createNbt(registries);
    }
}
