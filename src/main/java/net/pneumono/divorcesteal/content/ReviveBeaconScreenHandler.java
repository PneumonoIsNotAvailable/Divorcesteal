package net.pneumono.divorcesteal.content;

import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pneumono.divorcesteal.hearts.PlayerHeartData;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;

import java.util.List;

public class ReviveBeaconScreenHandler extends ScreenHandler {
    private final PlayerInventory playerInventory;
    private final ScreenHandlerContext context;
    private final List<PlayerHeartData> players;
    private final ProfileComponent target;
    private int selectedPlayer = -1;

    public ReviveBeaconScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY, List.of(), null);
    }

    public ReviveBeaconScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, List<PlayerHeartData> players, ProfileComponent target) {
        super(DivorcestealRegistry.REVIVE_BEACON_SCREEN_HANDLER, syncId);
        this.playerInventory = playerInventory;
        this.context = context;
        this.players = players;
        this.target = target;
    }

    private void reviveSelectedPlayer() {
        context.run((world, pos) -> {
            PlayerHeartData revived = getSelectedPlayer();
            if (revived != null && playerInventory.player instanceof ServerPlayerEntity reviver && world instanceof ServerWorld serverWorld) {
                ReviveBeaconItem.revivePlayer(serverWorld, revived, reviver);
            }
        });
    }

    public PlayerHeartData getSelectedPlayer() {
        if (this.selectedPlayer == -1) return null;
        return this.players.get(this.selectedPlayer);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return ReviveBeaconItem.canUse(player);
    }
}
