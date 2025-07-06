package net.pneumono.divorcesteal.gui;

import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.pneumono.divorcesteal.hearts.Hearts;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DeathbanScreen extends DeathScreen {
    public DeathbanScreen(@Nullable Text message, boolean isHardcore) {
        super(message, isHardcore);
    }

    public static boolean showShow(PlayerEntity player) {
        EntityAttributeInstance instance = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (instance != null) {
            EntityAttributeModifier modifier = instance.getModifier(Hearts.HEARTS_MODIFIER_ID);
            if (modifier != null) {
                return (int) modifier.value() == -18;
            }
        }
        return false;
    }

    // Same as DeathScreen but the respawn button is not created
    @Override
    protected void init() {
        Objects.requireNonNull(this.client);
        this.ticksSinceDeath = 0;
        this.buttons.clear();
        this.titleScreenButton = this.addDrawableChild(
                ButtonWidget.builder(
                                Text.translatable("deathScreen.titleScreen"),
                                button -> this.client.getAbuseReportContext().tryShowDraftScreen(
                                        this.client, this, this::quitLevel, true
                                )
                        )
                        .dimensions(this.width / 2 - 100, this.height / 4 + 96, 200, 20)
                        .build()
        );
        this.buttons.add(this.titleScreenButton);
        this.setButtonsActive(false);
        this.scoreText = Text.translatable(
                "deathScreen.score.value",
                Text.literal(
                        Integer.toString(Objects.requireNonNull(this.client.player).getScore())
                ).formatted(Formatting.YELLOW));
    }
}
