package net.pneumono.divorcesteal.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.pneumono.divorcesteal.hearts.HeartsUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DeathbanScreen extends DeathScreen {
    public DeathbanScreen(@Nullable Component message, boolean isHardcore) {
        super(message, isHardcore);
    }

    public static boolean showShow(Player player) {
        AttributeInstance instance = player.getAttribute(Attributes.MAX_HEALTH);
        if (instance != null) {
            AttributeModifier modifier = instance.getModifier(HeartsUtil.HEARTS_MODIFIER_ID);
            if (modifier != null) {
                return (int) modifier.amount() == -18;
            }
        }
        return false;
    }

    // Same as DeathScreen but the respawn button is not created
    @Override
    protected void init() {
        Objects.requireNonNull(this.minecraft);
        this.delayTicker = 0;
        this.exitButtons.clear();
        this.exitToTitleButton = this.addRenderableWidget(
                Button.builder(
                                Component.translatable("deathScreen.titleScreen"),
                                button -> this.minecraft.getReportingContext().draftReportHandled(
                                        this.minecraft, this, this::exitToTitleScreen, true
                                )
                        )
                        .bounds(this.width / 2 - 100, this.height / 4 + 96, 200, 20)
                        .build()
        );
        this.exitButtons.add(this.exitToTitleButton);
        this.setButtonsActive(false);
        this.deathScore = Component.translatable(
                "deathScreen.score.value",
                Component.literal(
                        Integer.toString(Objects.requireNonNull(this.minecraft.player).getScore())
                ).withStyle(ChatFormatting.YELLOW));
    }
}
