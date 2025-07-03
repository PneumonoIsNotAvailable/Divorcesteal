package net.pneumono.divorcesteal.content;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.pneumono.divorcesteal.hearts.Hearts;

public class HeartItem extends Item {
    public HeartItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) return ActionResult.SUCCESS_SERVER;

        int addedHearts = Hearts.addHeartsValidated(user, 1, false);
        if (addedHearts > 0) {
            // play heart use sound
            ItemStack stack = user.getStackInHand(hand);
            user.getItemCooldownManager().set(stack, 10);
            user.getStackInHand(hand).decrement(1);
        }

        return ActionResult.SUCCESS_SERVER;
    }
}
