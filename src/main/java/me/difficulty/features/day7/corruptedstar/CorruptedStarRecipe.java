package me.difficulty.features.day7.corruptedstar;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class CorruptedStarRecipe {

    private final JavaPlugin plugin;
    private final CorruptedStar corruptedStar;
    private final NamespacedKey recipeKey;

    public CorruptedStarRecipe(
            JavaPlugin plugin,
            CorruptedStar corruptedStar
    ) {
        this.plugin = plugin;
        this.corruptedStar = corruptedStar;
        this.recipeKey = new NamespacedKey(
                plugin,
                "corrupted_star_recipe"
        );
    }

    public void register() {

        ShapedRecipe recipe = new ShapedRecipe(
                recipeKey,
                corruptedStar.createItem()
        );

        recipe.shape(
                "DFD",
                "PNS",
                "DFD"
        );

        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('F', Material.ECHO_SHARD);
        recipe.setIngredient('P', Material.RABBIT_FOOT);
        recipe.setIngredient('N', new RecipeChoice.ExactChoice(new ItemStack(Material.NETHER_STAR)));
        recipe.setIngredient('S', Material.FERMENTED_SPIDER_EYE);

        plugin.getServer().addRecipe(recipe);
    }

    public NamespacedKey getRecipeKey() {
        return recipeKey;
    }
}