package net.pneumono.divorcesteal.datagen;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Mostly boilerplate from ShapedRecipeJsonBuilder
// Allows using ItemStacks as results
// Might add something similar to PneumonoCore
@SuppressWarnings("unused")
public class ShapedStackRecipeJsonBuilder implements CraftingRecipeJsonBuilder {
    private final RegistryEntryLookup<Item> registryLookup;
    private final RecipeCategory category;
    private final ItemStack output;
    private final List<String> pattern = Lists.newArrayList();
    private final Map<Character, Ingredient> inputs = Maps.newLinkedHashMap();
    private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;
    private boolean showNotification = true;

    private ShapedStackRecipeJsonBuilder(RegistryEntryLookup<Item> registryLookup, RecipeCategory category, ItemStack output) {
        this.registryLookup = registryLookup;
        this.category = category;
        this.output = output;
    }

    public static ShapedStackRecipeJsonBuilder create(RegistryEntryLookup<Item> registryLookup, RecipeCategory category, ItemStack output) {
        return new ShapedStackRecipeJsonBuilder(registryLookup, category, output);
    }

    public ShapedStackRecipeJsonBuilder input(Character c, TagKey<Item> tag) {
        return this.input(c, Ingredient.ofTag(this.registryLookup.getOrThrow(tag)));
    }

    public ShapedStackRecipeJsonBuilder input(Character c, ItemConvertible item) {
        return this.input(c, Ingredient.ofItem(item));
    }

    public ShapedStackRecipeJsonBuilder input(Character c, Ingredient ingredient) {
        if (this.inputs.containsKey(c)) {
            throw new IllegalArgumentException("Symbol '" + c + "' is already defined!");
        } else if (c == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            this.inputs.put(c, ingredient);
            return this;
        }
    }

    public ShapedStackRecipeJsonBuilder pattern(String patternStr) {
        if (!this.pattern.isEmpty() && patternStr.length() != this.pattern.getFirst().length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.pattern.add(patternStr);
            return this;
        }
    }

    public ShapedStackRecipeJsonBuilder criterion(String string, AdvancementCriterion<?> advancementCriterion) {
        this.criteria.put(string, advancementCriterion);
        return this;
    }

    public ShapedStackRecipeJsonBuilder group(@Nullable String string) {
        this.group = string;
        return this;
    }

    public ShapedStackRecipeJsonBuilder showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    @Override
    public Item getOutputItem() {
        return this.output.getItem();
    }

    @Override
    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeKey) {
        RawShapedRecipe rawShapedRecipe = this.validate(recipeKey);
        Advancement.Builder builder = exporter.getAdvancementBuilder()
                .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeKey))
                .rewards(AdvancementRewards.Builder.recipe(recipeKey))
                .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        this.criteria.forEach(builder::criterion);

        ShapedRecipe shapedRecipe = new ShapedRecipe(
                Objects.requireNonNullElse(this.group, ""),
                CraftingRecipeJsonBuilder.toCraftingCategory(this.category),
                rawShapedRecipe,
                this.output,
                this.showNotification
        );
        exporter.accept(recipeKey, shapedRecipe, builder.build(recipeKey.getValue().withPrefixedPath("recipes/" + this.category.getName() + "/")));
    }

    private RawShapedRecipe validate(RegistryKey<Recipe<?>> recipeKey) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeKey.getValue());
        } else {
            return RawShapedRecipe.create(this.inputs, this.pattern);
        }
    }
}
