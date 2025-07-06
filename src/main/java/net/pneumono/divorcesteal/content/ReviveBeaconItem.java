package net.pneumono.divorcesteal.content;

import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.pneumono.divorcesteal.hearts.HeartDataState;
import net.pneumono.divorcesteal.hearts.Hearts;
import net.pneumono.divorcesteal.hearts.PlayerHeartData;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;

import java.util.List;

public class ReviveBeaconItem extends Item {
    public ReviveBeaconItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world instanceof ServerWorld serverWorld) {
            HeartDataState state = Hearts.getHeartDataState(serverWorld);
            List<PlayerHeartData> bannedList = state.getHeartDataList().stream().filter(PlayerHeartData::isBanned).toList();
            List<PlayerHeartData> unbannedList = state.getHeartDataList().stream().filter(data -> !data.isBanned()).toList();

            if (bannedList.isEmpty() || unbannedList.isEmpty()) return ActionResult.FAIL;

            ItemStack stack = user.getStackInHand(hand);
            Random random = user.getRandom();
            ProfileComponent profile = stack.getOrDefault(DivorcestealRegistry.KILL_TARGET,
                    new ProfileComponent(unbannedList.get(random.nextBetween(0, unbannedList.size() - 1)).gameProfile())
            );
            stack.set(DivorcestealRegistry.KILL_TARGET, profile);

            user.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, playerInventory, player) -> new ReviveBeaconScreenHandler(
                    syncId, playerInventory,
                    ScreenHandlerContext.create(world, user.getBlockPos()),
                    bannedList, profile
            ), Text.translatable("divorcesteal.gui.revive.title")));
        }
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        return ActionResult.SUCCESS;
    }

    public static void revivePlayer(ServerWorld world, PlayerHeartData revived, ServerPlayerEntity reviver) {
        if (!canUse(reviver)) return;

        ItemStack stack = reviver.getMainHandStack();
        stack.decrement(1);
        world.playSound(null, reviver.getBlockPos(), DivorcestealRegistry.USE_REVIVE_BEACON_SOUND, SoundCategory.PLAYERS);
        reviver.incrementStat(DivorcestealRegistry.REVIVE_PLAYER_STAT);
        Hearts.revive(world, revived.gameProfile());
    }

    public static boolean canUse(PlayerEntity player) {
        return player.getMainHandStack().isOf(DivorcestealRegistry.REVIVE_BEACON_ITEM) || player.getOffHandStack().isOf(DivorcestealRegistry.REVIVE_BEACON_ITEM);
    }
}
