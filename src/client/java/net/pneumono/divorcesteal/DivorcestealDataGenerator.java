package net.pneumono.divorcesteal;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Blocks;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.ModelIds;
import net.minecraft.client.data.Models;
import net.minecraft.item.Items;
import net.pneumono.divorcesteal.content.DivorcestealRegistry;
import net.pneumono.divorcesteal.datagen.DivorcestealLanguageProviders;

public class DivorcestealDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(ModelProvider::new);
		DivorcestealLanguageProviders.addProviders(pack);
	}

	public static class ModelProvider extends FabricModelProvider {
		public ModelProvider(FabricDataOutput output) {
			super(output);
		}

		@Override
		public void generateBlockStateModels(BlockStateModelGenerator generator) {
			generator.registerItemModel(DivorcestealRegistry.REVIVE_BEACON_ITEM, ModelIds.getBlockModelId(Blocks.BEACON));
		}

		@Override
		public void generateItemModels(ItemModelGenerator generator) {
			generator.registerWithTextureSource(DivorcestealRegistry.HEART_ITEM, Items.NETHER_STAR, Models.GENERATED);
		}
	}
}
