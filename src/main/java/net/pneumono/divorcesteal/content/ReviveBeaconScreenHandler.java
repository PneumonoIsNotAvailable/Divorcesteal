package net.pneumono.divorcesteal.content;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;

import java.util.List;

public class ReviveBeaconScreenHandler extends ScreenHandler {
    private final PlayerInventory playerInventory;
    private final ScreenHandlerContext context;
    private final List<GameProfile> players;
    private final GameProfile target;
    private int selectedPlayer = -1;

    public ReviveBeaconScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY, List.of(), null);
    }

    public ReviveBeaconScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, List<GameProfile> players, GameProfile target) {
        super(DivorcestealRegistry.REVIVE_BEACON_SCREEN_HANDLER, syncId);
        this.playerInventory = playerInventory;
        this.context = context;
        this.players = players;
        this.target = target;
    }

    private void reviveSelectedPlayer() {
        context.run((world, pos) -> {
            GameProfile revived = getSelectedPlayer();
            if (revived != null && playerInventory.player instanceof ServerPlayerEntity reviver && world instanceof ServerWorld serverWorld) {
                ReviveBeaconBlock.revivePlayer(serverWorld, revived, reviver);
            }
        });
    }

    public GameProfile getSelectedPlayer() {
        if (this.selectedPlayer == -1) return null;
        return this.players.get(this.selectedPlayer);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return ReviveBeaconBlock.canUse(player);
    }
}
