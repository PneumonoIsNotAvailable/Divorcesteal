package net.pneumono.gacha.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.pneumono.gacha.GachaRegistry;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class GachaBeaconMenu extends AbstractContainerMenu {
    private final Player player;
    private final ContainerLevelAccess access;
    private final ContainerData beaconData;
    private GachaResult finalResult = null;
    private List<GachaResult> possibleResults = null;
    private float randomSpinMultiplier = 0;

    public GachaBeaconMenu(int i, Inventory playerInventory) {
        this(i, playerInventory, ContainerLevelAccess.NULL, new SimpleContainerData(2));
    }

    public GachaBeaconMenu(int i, Inventory playerInventory, ContainerLevelAccess access, ContainerData beaconData) {
        super(GachaRegistry.GACHA_BEACON_MENU, i);
        checkContainerDataCount(beaconData, 2);
        this.player = playerInventory.player;
        this.access = access;
        this.beaconData = beaconData;
        this.addStandardInventorySlots(playerInventory, 8, 87);
        this.addDataSlots(beaconData);
    }

    @Override
    public boolean clickMenuButton(@NonNull Player player, int i) {
        if (getState() == GachaBeaconState.UNROLLED) {
            this.access.execute(this::beginRolling);
            return true;
        } else {
            return false;
        }
    }

    public void beginRolling(Level level, BlockPos pos) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof GachaBeaconBlockEntity entity) {
            entity.beginRolling();
            this.player.awardStat(GachaRegistry.SPIN_GACHA_BEACON_STAT);
            entity.sendClientScreenData(this.player, this.containerId);
            setFinalResult(entity.getFinalResult());
        }
    }

    public GachaBeaconState getState() {
        return switch (this.beaconData.get(1)) {
            case 0 -> GachaBeaconState.UNROLLED;
            case 1 -> GachaBeaconState.ROLLING;
            case 2 -> GachaBeaconState.ROLLED;
            default -> throw new IllegalStateException();
        };
    }

    public int getSpinTicks() {
        return this.beaconData.get(0);
    }

    public GachaResult getFinalResult() {
        return finalResult;
    }

    public List<GachaResult> getPossibleResults() {
        return possibleResults;
    }

    public float getRandomSpinMultiplier() {
        return this.randomSpinMultiplier;
    }

    public void setFinalResult(GachaResult finalResult) {
        this.finalResult = finalResult;
    }

    public void setPossibleResults(List<GachaResult> possibleResults) {
        this.possibleResults = possibleResults;
    }

    public void setRandomSpinMultiplier(float randomSpinMultiplier) {
        this.randomSpinMultiplier = randomSpinMultiplier;
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return stillValid(this.access, player, GachaRegistry.GACHA_BEACON_BLOCK);
    }
}
