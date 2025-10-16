package net.pneumono.divorcesteal.content;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;

import java.util.List;

public class ReviveBeaconScreenHandler extends ScreenHandler {
    private final PlayerInventory playerInventory;
    private final Inventory inventory;
    private final ScreenHandlerContext context;
    private final Slot topHeartSlot;
    private final Slot leftHeartSlot;
    private final Slot rightHeartSlot;
    private final Slot headSlot;
    public List<ProfileComponent> revivableParticipants;
    private ProfileComponent target;
    private final Property selectedParticipant = Property.create();

    public ReviveBeaconScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(4), ScreenHandlerContext.EMPTY, List.of(), null);
    }

    public ReviveBeaconScreenHandler(
            int syncId, PlayerInventory playerInventory, Inventory inventory, ScreenHandlerContext context,
            List<ProfileComponent> revivableParticipants, ProfileComponent target
    ) {
        super(DivorcestealRegistry.REVIVE_BEACON_SCREEN_HANDLER, syncId);
        this.playerInventory = playerInventory;
        this.inventory = inventory;
        this.context = context;
        this.revivableParticipants = revivableParticipants;
        this.target = target;
        this.topHeartSlot = this.addSlot(new HeartSlot(inventory, 0, 111, 8));
        this.leftHeartSlot = this.addSlot(new HeartSlot(inventory, 1, 87, 48));
        this.rightHeartSlot = this.addSlot(new HeartSlot(inventory, 2, 135, 48));
        this.headSlot = this.addSlot(new HeadSlot(inventory, 3, 111, 34));
        this.addPlayerSlots(playerInventory, 39, 97);
        this.addProperty(this.selectedParticipant);
        this.selectedParticipant.set(-1);
    }

    public ProfileComponent getRevivableParticipant(int i) {
        return i >= revivableParticipants.size() ? null : this.revivableParticipants.get(i);
    }

    public boolean canRevive() {
        return this.selectedParticipant.get() >= 0 &&
                this.topHeartSlot.hasStack() &&
                this.leftHeartSlot.hasStack() &&
                this.rightHeartSlot.hasStack() &&
                this.headSlot.hasStack();
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id >= 0 && id < this.revivableParticipants.size()) {
            this.selectedParticipant.set(id);
            return true;
        } else if (id == -2) {
            if (!canRevive()) return false;

            this.topHeartSlot.setStack(ItemStack.EMPTY);
            this.leftHeartSlot.setStack(ItemStack.EMPTY);
            this.rightHeartSlot.setStack(ItemStack.EMPTY);
            this.headSlot.setStack(ItemStack.EMPTY);
            context.run(this::reviveSelectedParticipant);
            this.selectedParticipant.set(-1);

            return true;
        } else {
            return false;
        }
    }

    private void reviveSelectedParticipant(World world, BlockPos pos) {
        if (!world.getBlockState(pos).isOf(DivorcestealRegistry.REVIVE_BEACON_BLOCK)) return;

        int selectedParticipant = getSelectedParticipant();
        ProfileComponent revived = selectedParticipant == -1 ? null : this.revivableParticipants.get(selectedParticipant);
        if (revived != null && world instanceof ServerWorld serverWorld) {
            ReviveBeaconBlock.reviveParticipant(serverWorld, pos, revived.gameProfile(), playerInventory.player);
        }
        world.breakBlock(pos, false);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasStack()) return ItemStack.EMPTY;

        ItemStack stack = slot.getStack();
        ItemStack returnStack = stack.copy();
        if (slotIndex < 4) {
            if (this.insertItem(stack, 4, 40, true)) {
                return returnStack;
            }
        } else {
            if (topHeartSlot.canInsert(stack)) {
                if (this.insertItem(stack, 0, 3, false)) {
                    return returnStack;
                }
            } else if (headSlot.canInsert(stack)) {
                if (this.insertItem(stack, 3, 4, false)) {
                    return returnStack;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    public void setRevivableParticipants(List<ProfileComponent> revivableParticipants) {
        this.revivableParticipants = revivableParticipants;
    }

    public void setTarget(ProfileComponent target) {
        this.target = target;
    }

    public int getSelectedParticipant() {
        return this.selectedParticipant.get();
    }

    public ProfileComponent getTarget() {
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
        public HeartSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public int getMaxItemCount() {
            return 1;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            if (!(stack.getItem() instanceof HeartItem)) return false;

            return !stack.contains(DivorcestealRegistry.CRAFTED_COMPONENT);
        }
    }

    private class HeadSlot extends Slot {
        public HeadSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public int getMaxItemCount() {
            return 1;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            if (!(stack.getItem() instanceof PlayerHeadItem)) return false;

            ProfileComponent profileComponent = stack.get(DataComponentTypes.PROFILE);
            return profileComponent != null && profileComponent.gameProfile().getId().equals(
                    ReviveBeaconScreenHandler.this.target.gameProfile().getId()
            );
        }
    }
}
