package net.pneumono.divorcesteal;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.Models;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.pneumono.divorcesteal.registry.DivorcestealRegistry;
import net.pneumono.divorcesteal.datagen.DivorcestealLanguageProviders;

import java.util.concurrent.CompletableFuture;

public class DivorcestealDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(ModelProvider::new);
		pack.addProvider(RecipeProvider::new);
		DivorcestealLanguageProviders.addProviders(pack);
	}

	public static class ModelProvider extends FabricModelProvider {
		public ModelProvider(FabricDataOutput output) {
			super(output);
		}

		@Override
		public void generateBlockStateModels(BlockStateModelGenerator generator) {
			generator.registerItemModel(DivorcestealRegistry.REVIVE_BEACON_ITEM, Divorcesteal.id("block/revive_beacon"));
		}

		@Override
		public void generateItemModels(ItemModelGenerator generator) {
			generator.registerWithTextureSource(DivorcestealRegistry.HEART_ITEM, Items.NETHER_STAR, Models.GENERATED);
		}
	}

	public static class RecipeProvider extends FabricRecipeProvider {
		public RecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter recipeExporter) {
			return new RecipeGenerator(wrapperLookup, recipeExporter) {
				@Override
				public void generate() {
					createShaped(RecipeCategory.COMBAT, DivorcestealRegistry.HEART_ITEM)
							.pattern("@#@")
							.pattern("R/R")
							.pattern("@#@")
							.input('@', Items.NAUTILUS_SHELL)
							.input('#', Items.DIAMOND_BLOCK)
							.input('R', Items.RED_GLAZED_TERRACOTTA)
							.input('/', Items.SPYGLASS)
							.criterion("exists", TickCriterion.Conditions.createTick())
							.offerTo(exporter);

					createShaped(RecipeCategory.COMBAT, DivorcestealRegistry.REVIVE_BEACON_ITEM)
							.pattern("4#+")
							.pattern("B+B")
							.pattern("+#H")
							.input('4', Items.OMINOUS_TRIAL_KEY)
							.input('#', Items.DIAMOND_BLOCK)
							.input('+', DivorcestealRegistry.HEART_ITEM)
							.input('B', Items.BLUE_GLAZED_TERRACOTTA)
							.input('H', Items.ELYTRA)
							.criterion("exists", TickCriterion.Conditions.createTick())
							.offerTo(exporter);
				}
			};
		}

		@Override
		public String getName() {
			return "Recipes";
		}
	}
}
