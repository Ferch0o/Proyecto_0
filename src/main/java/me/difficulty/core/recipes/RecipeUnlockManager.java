package me.difficulty.core.recipes;

import me.difficulty.core.day.DayManager;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class RecipeUnlockManager {


    private final JavaPlugin plugin;

    private final DayManager dayManager;


    /*
     * ========================================
     * RECETAS
     * ========================================
     */

    private final NamespacedKey corruptedStarRecipeKey;

    private final NamespacedKey explosiveCrossbowRecipeKey;


    public RecipeUnlockManager(
            JavaPlugin plugin,
            DayManager dayManager,
            NamespacedKey corruptedStarRecipeKey,
            NamespacedKey explosiveCrossbowRecipeKey
    ) {

        this.plugin =
                plugin;

        this.dayManager =
                dayManager;

        this.corruptedStarRecipeKey =
                corruptedStarRecipeKey;

        this.explosiveCrossbowRecipeKey =
                explosiveCrossbowRecipeKey;
    }


    /*
     * ========================================
     * ACTUALIZAR RECETAS DEL JUGADOR
     * ========================================
     */

    public void updatePlayer(
            Player player
    ) {


        /*
         * ========================================
         * ESTRELLA CORRUPTA
         * DÍA 7+
         * ========================================
         */

        if (dayManager.isAtLeast(7)) {

            player.discoverRecipe(
                    corruptedStarRecipeKey
            );

        } else {

            player.undiscoverRecipe(
                    corruptedStarRecipeKey
            );
        }


        /*
         * ========================================
         * BALLESTA EXPLOSIVA
         * DÍA 10+
         * ========================================
         */

        if (dayManager.isAtLeast(10)) {

            player.discoverRecipe(
                    explosiveCrossbowRecipeKey
            );

        } else {

            player.undiscoverRecipe(
                    explosiveCrossbowRecipeKey
            );
        }
    }


    /*
     * ========================================
     * ACTUALIZAR TODOS LOS JUGADORES
     * ========================================
     */

    public void updateAllPlayers() {


        for (Player player :
                plugin.getServer()
                        .getOnlinePlayers()) {

            updatePlayer(
                    player
            );
        }
    }
}