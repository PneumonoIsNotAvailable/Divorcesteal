package net.pneumono.divorcesteal.content;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.pneumono.divorcesteal.content.component.KillTargetComponent;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ReviveBeaconBlockEntity extends LockableContainerBlockEntity {
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(4, ItemStack.EMPTY);
    private KillTargetComponent target;

    public ReviveBeaconBlockEntity(BlockPos pos, BlockState state) {
        super(DivorcestealRegistry.REVIVE_BEACON_ENTITY, pos, state);
    }

    public @Nullable KillTargetComponent getOrCreateTarget(UUID except) {
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

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        KillTargetComponent target = getOrCreateTarget(playerInventory.player.getUuid());
        if (target == null) return null;

        return new ReviveBeaconScreenHandler(syncId, playerInventory, this,
                ScreenHandlerContext.create(getWorld(), this.getPos()),
                ReviveBeaconBlock.getRevivableParticipants(),
                target.profile()
        );
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        Inventories.readData(view, this.inventory);
        this.target = view.read("target", KillTargetComponent.CODEC).orElse(null);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, this.inventory);
        view.putNullable("target", KillTargetComponent.CODEC, this.target);
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        this.target = components.get(DivorcestealRegistry.KILL_TARGET_COMPONENT);
    }

    @Override
    protected void addComponents(ComponentMap.Builder builder) {
        super.addComponents(builder);
        builder.add(DivorcestealRegistry.KILL_TARGET_COMPONENT, this.target);
    }

    @Override
    public void removeFromCopiedStackData(WriteView view) {
        super.removeFromCopiedStackData(view);
        view.remove("target");
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("divorcesteal.gui.revive_beacon.title");
    }

    @Override
    protected DefaultedList<ItemStack> getHeldStacks() {
        return this.inventory;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
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
