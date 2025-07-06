package net.pneumono.divorcesteal.gui;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.content.ReviveBeaconScreenHandler;

public class ReviveBeaconScreen extends HandledScreen<ReviveBeaconScreenHandler> {
    private static final Identifier TEXTURE = Divorcesteal.id("textures/gui/revive.png");
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 133;

    public ReviveBeaconScreen(ReviveBeaconScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED, TEXTURE,
                (GUI_WIDTH - 176) / 2, (GUI_HEIGHT - 133) / 2,
                0.0F, 0.0F,
                GUI_WIDTH, GUI_HEIGHT,
                256, 256
        );
    }
}
