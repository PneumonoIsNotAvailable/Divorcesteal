package net.pneumono.divorcesteal;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.Models;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.CopyComponentsLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
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
		pack.addProvider(LootTableProvider::new);
		DivorcestealLanguageProviders.addProviders(pack);
	}

	public static class ModelProvider extends FabricModelProvider {
		public ModelProvider(FabricDataOutput output) {
			super(output);
		}

		@Override
		public void generateBlockStateModels(BlockStateModelGenerator generator) {
			generator.registerSimpleState(DivorcestealRegistry.REVIVE_BEACON_BLOCK);
		}

		@Override
		public void generateItemModels(ItemModelGenerator generator) {
			generator.register(DivorcestealRegistry.HEART_ITEM, Models.GENERATED);
		}
	}

	public static class LootTableProvider extends FabricBlockLootTableProvider {
		public LootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
			super(dataOutput, registryLookup);
		}

		@Override
		public void generate() {
			addDrop(DivorcestealRegistry.REVIVE_BEACON_BLOCK, LootTable.builder().pool(
					this.addSurvivesExplosionCondition(
							DivorcestealRegistry.REVIVE_BEACON_ITEM,
							LootPool.builder()
									.rolls(ConstantLootNumberProvider.create(1.0F))
									.with(
											ItemEntry.builder(DivorcestealRegistry.REVIVE_BEACON_ITEM)
													.apply(CopyComponentsLootFunction.builder(CopyComponentsLootFunction.Source.BLOCK_ENTITY).include(DataComponentTypes.CUSTOM_NAME))
													.apply(CopyComponentsLootFunction.builder(CopyComponentsLootFunction.Source.BLOCK_ENTITY).include(DivorcestealRegistry.KILL_TARGET))
									)
					))
			);
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
							.pattern("I#I")
							.pattern("UTU")
							.pattern("I#I")
							.input('I', Items.NETHERITE_INGOT)
							.input('#', Items.END_CRYSTAL)
							.input('U', Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
							.input('T', Items.TOTEM_OF_UNDYING)
							.criterion("exists", TickCriterion.Conditions.createTick())
							.offerTo(exporter);

					createShaped(RecipeCategory.COMBAT, DivorcestealRegistry.REVIVE_BEACON_ITEM)
							.pattern("P#P")
							.pattern("B+B")
							.pattern("CHC")
							.input('P', Items.DRAGON_BREATH)
							.input('#', Items.END_CRYSTAL)
							.input('B', Items.NETHERITE_BLOCK)
							.input('+', Items.NETHER_STAR)
							.input('C', Items.SCULK_CATALYST)
							.input('H', Items.CREAKING_HEART)
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
