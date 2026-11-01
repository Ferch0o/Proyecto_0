package me.difficulty.features.day17.items.elementalpotato;

import me.difficulty.features.day17.items.Day17Materials;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public final class ElementalPotatoRecipe {
    private final JavaPlugin plugin;
    private final ElementalPotato potato;
    private final Day17Materials ingredients;
    private final NamespacedKey key;

    public ElementalPotatoRecipe(JavaPlugin plugin, ElementalPotato potato, Day17Materials ingredients) {
        this.plugin = plugin;
        this.potato = potato;
        this.ingredients = ingredients;
        this.key = new NamespacedKey(plugin, "elemental_potato_recipe");
    }

    public NamespacedKey key() { return key; }

    public void register() {
        ShapedRecipe recipe = new ShapedRecipe(key, potato.createItem());
        // Carne Seca  Hueso Guardián  Pólvora Mágica
        // Corazón     Patata venenosa Corazón
        // Titanio     Ojo Goliat     Titanio
        recipe.shape("DBM", "HPH", "TET");
        recipe.setIngredient('D', new RecipeChoice.ExactChoice(ingredients.driedMeat().createItem()));
        recipe.setIngredient('B', new RecipeChoice.ExactChoice(ingredients.guardianBone().createItem()));
        recipe.setIngredient('M', new RecipeChoice.ExactChoice(ingredients.magicGunpowder().createItem()));
        recipe.setIngredient('H', new RecipeChoice.ExactChoice(ingredients.wardenHeart().createItem()));
        recipe.setIngredient('P', Material.POISONOUS_POTATO);
        recipe.setIngredient('T', new RecipeChoice.ExactChoice(ingredients.titaniumBlock().createItem()));
        recipe.setIngredient('E', new RecipeChoice.ExactChoice(ingredients.goliathEye().createItem()));
        plugin.getServer().addRecipe(recipe);
    }
}
