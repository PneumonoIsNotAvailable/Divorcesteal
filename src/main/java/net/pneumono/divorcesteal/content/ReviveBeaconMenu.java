package net.pneumono.divorcesteal.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ReviveBeaconMenu extends AbstractContainerMenu {
    private final Inventory playerInventory;
    private final Container inventory;
    private final ContainerLevelAccess context;
    private final Slot topHeartSlot;
    private final Slot leftHeartSlot;
    private final Slot rightHeartSlot;
    private final Slot headSlot;
    public List<NameAndId> revivableParticipants;
    private NameAndId target;
    private final DataSlot selectedParticipant = DataSlot.standalone();

    public ReviveBeaconMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(4), ContainerLevelAccess.NULL, List.of(), null);
    }

    public ReviveBeaconMenu(
            int syncId, Inventory playerInventory, Container inventory, ContainerLevelAccess access,
            List<NameAndId> revivableParticipants, NameAndId target
    ) {
        super(DivorcestealRegistry.REVIVE_BEACON_SCREEN_HANDLER, syncId);
        this.playerInventory = playerInventory;
        this.inventory = inventory;
        this.context = access;
        this.revivableParticipants = revivableParticipants;
        this.target = target;
        this.topHeartSlot = this.addSlot(new HeartSlot(inventory, 0, 111, 8));
        this.leftHeartSlot = this.addSlot(new HeartSlot(inventory, 1, 87, 48));
        this.rightHeartSlot = this.addSlot(new HeartSlot(inventory, 2, 135, 48));
        this.headSlot = this.addSlot(new HeadSlot(inventory, 3, 111, 34));
        this.addStandardInventorySlots(playerInventory, 39, 97);
        this.addDataSlot(this.selectedParticipant);
        this.selectedParticipant.set(-1);
    }

    public NameAndId getRevivableParticipant(int i) {
        return i >= revivableParticipants.size() ? null : this.revivableParticipants.get(i);
    }

    public boolean canRevive() {
        return this.selectedParticipant.get() >= 0 &&
                this.topHeartSlot.hasItem() &&
                this.leftHeartSlot.hasItem() &&
                this.rightHeartSlot.hasItem() &&
                this.headSlot.hasItem();
    }

    @Override
    public boolean clickMenuButton(@NonNull Player player, int id) {
        if (id >= 0 && id < this.revivableParticipants.size()) {
            this.selectedParticipant.set(id);
            return true;
        } else if (id == -2) {
            if (!canRevive()) return false;

            this.topHeartSlot.setByPlayer(ItemStack.EMPTY);
            this.leftHeartSlot.setByPlayer(ItemStack.EMPTY);
            this.rightHeartSlot.setByPlayer(ItemStack.EMPTY);
            this.headSlot.setByPlayer(ItemStack.EMPTY);
            context.execute(this::reviveSelectedParticipant);
            this.selectedParticipant.set(-1);

            return true;
        } else {
            return false;
        }
    }

    private void reviveSelectedParticipant(Level level, BlockPos pos) {
        if (!level.getBlockState(pos).is(DivorcestealRegistry.REVIVE_BEACON_BLOCK)) return;

        int selectedParticipant = getSelectedParticipant();
        NameAndId revived = selectedParticipant == -1 ? null : this.revivableParticipants.get(selectedParticipant);
        if (revived != null
                && level instanceof ServerLevel serverWorld
                && ReviveBeaconBlock.reviveParticipant(serverWorld, pos, revived.id(), playerInventory.player)
        ) {
            level.destroyBlock(pos, false);
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NonNull Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack returnStack = stack.copy();
        if (slotIndex < 4) {
            if (this.moveItemStackTo(stack, 4, 40, true)) {
                return returnStack;
            }
        } else {
            if (topHeartSlot.mayPlace(stack)) {
                if (this.moveItemStackTo(stack, 0, 3, false)) {
                    return returnStack;
                }
            } else if (headSlot.mayPlace(stack)) {
                if (this.moveItemStackTo(stack, 3, 4, false)) {
                    return returnStack;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return this.inventory.stillValid(player);
    }

    public void setRevivableParticipants(List<NameAndId> revivableParticipants) {
        this.revivableParticipants = revivableParticipants;
    }

    public void setTarget(NameAndId target) {
        this.target = target;
    }

    public int getSelectedParticipant() {
        return this.selectedParticipant.get();
    }

    public NameAndId getTarget() {
        return target;
    }

    public Slot getTopHeartSlot() {
        return topHeartSlot;
    }

    public Slot getLeftHeartSlot() {
        return leftHeartSlot;
    }

    public Slot getRightHeartSlot() {
        return rightHeartSlot;
    }

    public Slot getHeadSlot() {
        return headSlot;
    }

    private static class HeartSlot extends Slot {
        public HeartSlot(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (!(stack.getItem() instanceof HeartItem)) return false;

            return !stack.has(DivorcestealRegistry.CRAFTED_COMPONENT);
        }
    }

    private class HeadSlot extends Slot {
        public HeadSlot(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (!(stack.getItem() instanceof PlayerHeadItem)) return false;

            ResolvableProfile profileComponent = stack.get(DataComponents.PROFILE);
            return profileComponent != null && profileComponent.partialProfile().id().equals(
                    ReviveBeaconMenu.this.target.id()
            );
        }
    }
}
