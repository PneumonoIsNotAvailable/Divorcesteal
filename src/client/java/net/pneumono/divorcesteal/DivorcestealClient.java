package net.pneumono.divorcesteal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.screen.ScreenHandler;
import net.pneumono.divorcesteal.content.ReviveBeaconScreenHandler;
import net.pneumono.divorcesteal.content.ReviveBeaconTargetS2CPayload;
import net.pneumono.divorcesteal.gui.ReviveBeaconScreen;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;

public class DivorcestealClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HandledScreens.register(DivorcestealRegistry.REVIVE_BEACON_SCREEN_HANDLER, ReviveBeaconScreen::new);
		ClientPlayNetworking.registerGlobalReceiver(ReviveBeaconTargetS2CPayload.ID, DivorcestealClient::syncReviveBeaconTarget);

		BlockRenderLayerMap.putBlock(DivorcestealRegistry.REVIVE_BEACON_BLOCK, BlockRenderLayer.CUTOUT);
	}

	private static void syncReviveBeaconTarget(ReviveBeaconTargetS2CPayload payload, ClientPlayNetworking.Context context) {
		ScreenHandler currentScreenHandler = context.player().currentScreenHandler;
		if (currentScreenHandler.syncId != payload.syncId() || !(currentScreenHandler instanceof ReviveBeaconScreenHandler beaconHandler)) {
			Divorcesteal.LOGGER.warn("Failed to sync revive beacon target!");
			return;
		}

		beaconHandler.setTarget(payload.profileComponent());
	}
}