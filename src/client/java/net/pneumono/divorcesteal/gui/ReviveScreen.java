package net.pneumono.divorcesteal.gui;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.pneumono.divorcesteal.Divorcesteal;
import net.pneumono.divorcesteal.content.RevivePlayerC2SPayload;

import java.util.List;

public class ReviveScreen extends Screen {
    private static final Identifier TEXTURE = Divorcesteal.id("textures/gui/revive.png");
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 133;

    private final List<GameProfile> players;
    private int selectedPlayer = -1;

    public ReviveScreen(List<GameProfile> players) {
        super(Text.translatable("divorcesteal.gui.revive.title"));
        this.players = players;
    }

    private void reviveSelectedPlayer() {
        GameProfile player = getSelectedPlayer();
        if (player != null) ClientPlayNetworking.send(new RevivePlayerC2SPayload(player));
    }

    public GameProfile getSelectedPlayer() {
        if (this.selectedPlayer == -1) return null;
        return this.players.get(this.selectedPlayer);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        this.renderInGameBackground(context);
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED, TEXTURE,
                (GUI_WIDTH - 176) / 2, (GUI_HEIGHT - 133) / 2,
                0.0F, 0.0F,
                GUI_WIDTH, GUI_HEIGHT,
                256, 256
        );
    }
}
