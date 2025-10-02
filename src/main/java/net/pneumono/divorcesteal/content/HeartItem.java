package net.pneumono.divorcesteal.content;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.pneumono.divorcesteal.DivorcestealConfig;
import net.pneumono.divorcesteal.hearts.Hearts;
import net.pneumono.divorcesteal.hearts.PlayerHeartData;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;

public class HeartItem extends Item {
    public HeartItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient()) return ActionResult.CONSUME;

        if (stack.contains(DivorcestealRegistry.CRAFTED_COMPONENT) && getHearts(user) >= DivorcestealConfig.CRAFTED_HEART_LIMIT.getValue()) return ActionResult.FAIL;

        int addedHearts = Hearts.addHeartsValidated(user, 1, false);

        if (addedHearts > 0) {
            world.playSound(null, user.getBlockPos(), DivorcestealRegistry.USE_HEART_SOUND, SoundCategory.PLAYERS);
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            user.getItemCooldownManager().set(stack, 1);
            stack.decrement(1);
            return ActionResult.SUCCESS_SERVER;

        } else {
            return ActionResult.FAIL;
        }
    }

    private static int getHearts(PlayerEntity user) {
        PlayerHeartData data = Hearts.getHeartDataState().getHeartData(user.getGameProfile().getId());
        return data == null ? -1 : data.hearts();
    }
}
