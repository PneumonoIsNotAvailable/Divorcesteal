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
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;

import java.util.List;

public class ReviveBeaconScreenHandler extends ScreenHandler {
    private final Inventory input = new SimpleInventory(4) {
        @Override
        public int getMaxCountPerStack() {
            return 1;
        }
    };
    private final PlayerInventory playerInventory;
    private final ScreenHandlerContext context;
    private final Slot topHeartSlot;
    private final Slot leftHeartSlot;
    private final Slot rightHeartSlot;
    private final Slot headSlot;
    private final List<ProfileComponent> players;
    private ProfileComponent target;
    private final Property selectedPlayer = Property.create();

    public ReviveBeaconScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY, List.of(), null);
    }

    public ReviveBeaconScreenHandler(
            int syncId, PlayerInventory playerInventory, ScreenHandlerContext context,
            List<ProfileComponent> players, ProfileComponent target
    ) {
        super(DivorcestealRegistry.REVIVE_BEACON_SCREEN_HANDLER, syncId);
        this.playerInventory = playerInventory;
        this.context = context;
        this.players = players;
        this.target = target;
        this.topHeartSlot = this.addSlot(new HeartSlot(input, 0, 111, 9));
        this.leftHeartSlot = this.addSlot(new HeartSlot(input, 1, 87, 49));
        this.rightHeartSlot = this.addSlot(new HeartSlot(input, 2, 135, 49));
        this.headSlot = this.addSlot(new HeadSlot(input, 3, 111, 35));
        this.addPlayerSlots(playerInventory, 39, 97);
    }

    private void reviveSelectedPlayer() {
        context.run((world, pos) -> {
            int selectedPlayer = getSelectedPlayer();
            ProfileComponent revived = selectedPlayer == -1 ? null : this.players.get(selectedPlayer);
            if (revived != null && world instanceof ServerWorld serverWorld) {
                ReviveBeaconBlock.revivePlayer(serverWorld, pos, revived.gameProfile(), playerInventory.player);
            }
        });
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        // todo
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        // Scuffed as hell, but I genuinely have no idea why canUse won't work
        return true;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.context.run((world, pos) -> this.dropInventory(player, this.input));
    }

    public int getSelectedPlayer() {
        return this.selectedPlayer.get();
    }

    public void setTarget(ProfileComponent target) {
        this.target = target;
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
        public boolean canInsert(ItemStack stack) {
            if (!(stack.getItem() instanceof PlayerHeadItem)) return false;

            ProfileComponent profileComponent = stack.get(DataComponentTypes.PROFILE);
            return profileComponent != null && profileComponent.gameProfile().getId().equals(
                    ReviveBeaconScreenHandler.this.target.gameProfile().getId()
            );
        }
    }
}
