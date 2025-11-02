package net.pneumono.divorcesteal.content;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.pneumono.divorcesteal.content.component.KillTargetComponent;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ReviveBeaconBlockEntity extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
    private KillTargetComponent target;

    public ReviveBeaconBlockEntity(BlockPos pos, BlockState state) {
        super(DivorcestealRegistry.REVIVE_BEACON_ENTITY, pos, state);
    }

    public @Nullable KillTargetComponent getOrCreateTarget(UUID except) {
        if (this.target != null) return target;

        if (this.getLevel() instanceof ServerLevel serverLevel) {
            GameProfile randomTarget = ReviveBeaconBlock.getRandomTarget(serverLevel, except).orElse(null);
            if (randomTarget != null) {
                this.target = new KillTargetComponent(randomTarget);
                setChanged();
                return this.target;
            }
        }

        return this.target;
    }

    // No idea, it'll probably cause some horrible problems if this returns null but oh well.
    // The mod can crash the server for fun, as a treat.
    @SuppressWarnings("DataFlowIssue")
    @Override
    protected @NotNull AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
        KillTargetComponent target = getOrCreateTarget(playerInventory.player.getUUID());
        if (target == null) return null;

        return new ReviveBeaconMenu(syncId, playerInventory, this,
                ContainerLevelAccess.create(getLevel(), this.getBlockPos()),
                ReviveBeaconBlock.getRevivableParticipants(),
                target.profile()
        );
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.inventory);
        this.target = input.read("target", KillTargetComponent.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.inventory);
        output.storeNullable("target", KillTargetComponent.CODEC, this.target);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.target = components.get(DivorcestealRegistry.KILL_TARGET_COMPONENT);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DivorcestealRegistry.KILL_TARGET_COMPONENT, this.target);
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        super.removeComponentsFromTag(output);
        output.discard("target");
    }

    @Override
    public int getContainerSize() {
        return this.inventory.size();
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("divorcesteal.gui.revive_beacon.title");
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
