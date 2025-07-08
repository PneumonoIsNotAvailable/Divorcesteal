package net.pneumono.divorcesteal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.BlockRenderLayer;
import net.pneumono.divorcesteal.gui.ReviveBeaconScreen;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;

public class DivorcestealClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HandledScreens.register(DivorcestealRegistry.REVIVE_BEACON_SCREEN_HANDLER, ReviveBeaconScreen::new);
		BlockRenderLayerMap.putBlock(DivorcestealRegistry.REVIVE_BEACON_BLOCK, BlockRenderLayer.CUTOUT);
	}
}