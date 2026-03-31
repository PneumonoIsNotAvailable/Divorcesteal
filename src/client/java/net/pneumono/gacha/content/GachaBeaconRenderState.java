package net.pneumono.gacha.content;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.jspecify.annotations.Nullable;

public class GachaBeaconRenderState extends BlockEntityRenderState {
    public @Nullable DisplayPlayer displayPlayer;
    public float rotation = 0;
}
