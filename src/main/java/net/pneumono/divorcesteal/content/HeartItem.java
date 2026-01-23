package net.pneumono.divorcesteal.content;

import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.pneumono.divorcesteal.DivorcestealConfig;
import net.pneumono.divorcesteal.hearts.HeartsUtil;
import net.pneumono.divorcesteal.hearts.Participant;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class HeartItem extends Item {
    public HeartItem(Properties settings) {
        super(settings);
    }

    @Override
    public @NotNull InteractionResult use(Level level, Player user, @NonNull InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResult.CONSUME;

        if (stack.has(DivorcestealRegistry.CRAFTED_COMPONENT) && getHearts(user) >= DivorcestealConfig.CRAFTED_HEART_LIMIT.getValue()) return InteractionResult.FAIL;

        int addedHearts = HeartsUtil.addHeartsValidated(user, 1, false);

        if (addedHearts > 0) {
            level.playSound(null, user.blockPosition(), DivorcestealRegistry.USE_HEART_SOUND, SoundSource.PLAYERS);
            user.awardStat(Stats.ITEM_USED.get(this));
            user.getCooldowns().addCooldown(stack, 1);
            stack.consume(1, user);
            return InteractionResult.SUCCESS_SERVER;

        } else {
            return InteractionResult.FAIL;
        }
    }

    private static int getHearts(Player user) {
        Participant participant = HeartsUtil.getHeartDataState().getParticipant(user.getGameProfile().id());
        return participant == null ? -1 : participant.getHearts();
    }
}
