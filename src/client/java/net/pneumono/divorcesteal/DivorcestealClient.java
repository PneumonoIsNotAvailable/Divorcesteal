package net.pneumono.divorcesteal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.pneumono.divorcesteal.content.RevivablePlayersS2CPayload;

public class DivorcestealClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(RevivablePlayersS2CPayload.PAYLOAD_ID, (payload, context) -> {
			// Open player revive GUI
		});
	}
}