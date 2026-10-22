package me.difficulty.recipes;

import me.difficulty.items.day10.CorruptedInk;
import me.difficulty.items.day10.ExplosiveCrossbow;
import me.difficulty.items.day10.MinotaurHorn;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import org.bukkit.plugin.java.JavaPlugin;


public class ExplosiveCrossbowRecipe {


    private final JavaPlugin plugin;

    private final ExplosiveCrossbow explosiveCrossbow;

    private final CorruptedInk corruptedInk;

    private final MinotaurHorn minotaurHorn;

    private final NamespacedKey recipeKey;


    public ExplosiveCrossbowRecipe(
            JavaPlugin plugin,
            ExplosiveCrossbow explosiveCrossbow,
            CorruptedInk corruptedInk,
            MinotaurHorn minotaurHorn
    ) {

        this.plugin =
                plugin;

        this.explosiveCrossbow =
                explosiveCrossbow;

        this.corruptedInk =
                corruptedInk;

        this.minotaurHorn =
                minotaurHorn;

        this.recipeKey =
                new NamespacedKey(
                        plugin,
                        "explosive_crossbow_recipe"
                );
    }


    public void register() {


        ShapedRecipe recipe =
                new ShapedRecipe(
                        recipeKey,
                        explosiveCrossbow.createItem()
                );


        /*
         * Categoría del libro.
         */

        recipe.setCategory(
                CraftingBookCategory.EQUIPMENT
        );


        /*
         * ========================================
         * FORMA
         * ========================================
         *
         * I = Tinta Corrupta
         * H = Cuerno de Minotauro
         * S = Cabeza de Wither Skeleton
         * C = Ballesta vanilla
         *
         * I H I
         * S C S
         * I H I
         */

        recipe.shape(
                "IHI",
                "SCS",
                "IHI"
        );


        /*
         * ========================================
         * TINTA CORRUPTA
         * ========================================
         *
         * ExactChoice hace que Minecraft
         * reconozca específicamente nuestro
         * item custom.
         *
         * La cantidad 5 sigue siendo controlada
         * por nuestro listener.
         */

        recipe.setIngredient(
                'I',
                new RecipeChoice.ExactChoice(
                        corruptedInk.createItem()
                )
        );


        /*
         * ========================================
         * CUERNO DE MINOTAURO
         * ========================================
         */

        recipe.setIngredient(
                'H',
                new RecipeChoice.ExactChoice(
                        minotaurHorn.createItem()
                )
        );


        /*
         * ========================================
         * CABEZA DE WITHER
         * ========================================
         */

        recipe.setIngredient(
                'S',
                Material.WITHER_SKELETON_SKULL
        );


        /*
         * ========================================
         * BALLESTA VANILLA
         * ========================================
         *
         * La dejamos por Material porque
         * nuestro listener ya comprueba
         * que NO tenga custom_item_id.
         */

        recipe.setIngredient(
                'C',
                Material.CROSSBOW
        );


        plugin.getServer()
                .addRecipe(
                        recipe
                );
    }


    public NamespacedKey getRecipeKey() {

        return recipeKey;
    }
}