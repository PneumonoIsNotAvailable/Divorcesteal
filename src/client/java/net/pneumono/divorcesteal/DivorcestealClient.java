package net.pneumono.divorcesteal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.pneumono.aprilfools.AprilFoolsClientRegistry;
import net.pneumono.divorcesteal.content.ReviveBeaconMenu;
import net.pneumono.divorcesteal.content.ReviveBeaconInfoS2CPayload;
import net.pneumono.divorcesteal.gui.ReviveBeaconScreen;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;

public class DivorcestealClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(DivorcestealRegistry.REVIVE_BEACON_SCREEN_HANDLER, ReviveBeaconScreen::new);
		ClientPlayNetworking.registerGlobalReceiver(ReviveBeaconInfoS2CPayload.ID, DivorcestealClient::syncReviveBeaconTarget);

		ResourceLoader.registerBuiltinPack(
				Divorcesteal.id("old_hearts"),
				FabricLoader.getInstance().getModContainer(Divorcesteal.MOD_ID).orElseThrow(),
				Component.translatable("divorcesteal.resource_pack.old_hearts"),
				PackActivationType.NORMAL
		);

		AprilFoolsClientRegistry.register();
	}

	private static void syncReviveBeaconTarget(ReviveBeaconInfoS2CPayload payload, ClientPlayNetworking.Context context) {
		AbstractContainerMenu currentMenu = context.player().containerMenu;
		if (currentMenu.containerId != payload.containerId() || !(currentMenu instanceof ReviveBeaconMenu beaconHandler)) {
			Divorcesteal.LOGGER.warn("Failed to sync revive beacon target!");
			return;
		}

		beaconHandler.setTarget(payload.target());
		beaconHandler.setRevivableParticipants(payload.revivableParticipants());
	}
}