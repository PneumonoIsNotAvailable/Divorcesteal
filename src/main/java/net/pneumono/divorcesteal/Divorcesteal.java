package net.pneumono.divorcesteal;

import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
import net.pneumono.divorcesteal.content.DivorcestealCommands;
import net.pneumono.divorcesteal.content.DivorcestealRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class Divorcesteal implements ModInitializer {
	public static final String MOD_ID = "divorcesteal";

	public static final Logger LOGGER = LoggerFactory.getLogger("Divorcesteal");

	public static final Supplier<Integer> MAX_HEARTS = () -> 20;
	public static final Supplier<Integer> DEFAULT_HEARTS = () -> 10;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Divorcesteal");
		DivorcestealRegistry.registerDivorcestealContent();
		DivorcestealCommands.registerDivorcestealCommands();
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}