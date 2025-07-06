package net.pneumono.divorcesteal;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.pneumono.divorcesteal.gui.ReviveBeaconScreen;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;

public class DivorcestealClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HandledScreens.register(DivorcestealRegistry.REVIVE_BEACON_SCREEN_HANDLER, ReviveBeaconScreen::new);
	}
}