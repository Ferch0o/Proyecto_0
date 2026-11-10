
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
     * RECETAS EXISTENTES
     * ========================================
     */

    private final NamespacedKey corruptedStarRecipeKey;
    private final NamespacedKey explosiveCrossbowRecipeKey;

    /*
     * ========================================
     * NUEVAS RECETAS DEL DIA 20
     * ========================================
     */

    private NamespacedKey multiPearlsRecipeKey;
    private NamespacedKey panicScytheRecipeKey;

    public RecipeUnlockManager(
            JavaPlugin plugin,
            DayManager dayManager,
            NamespacedKey corruptedStarRecipeKey,
            NamespacedKey explosiveCrossbowRecipeKey
    ) {

        this.plugin = plugin;
        this.dayManager = dayManager;

        this.corruptedStarRecipeKey =
                corruptedStarRecipeKey;

        this.explosiveCrossbowRecipeKey =
                explosiveCrossbowRecipeKey;
    }

    /*
     * ========================================
     * REGISTRAR RECETA MULTI PEARLS
     * ========================================
     */
    public void registerMultiPearlsRecipe(
            NamespacedKey recipeKey
    ) {

        this.multiPearlsRecipeKey = recipeKey;
    }

    /*
     * ========================================
     * REGISTRAR RECETA GUADAÑA DEL PANICO
     * ========================================
     */
    public void registerPanicScytheRecipe(
            NamespacedKey recipeKey
    ) {

        this.panicScytheRecipeKey = recipeKey;
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
         * ESTRELLA CORRUPTA - DIA 7
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
         * BALLESTA EXPLOSIVA - DIA 10
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

        /*
         * ========================================
         * MULTI PEARLS - DIA 20
         * ========================================
         */
        if (multiPearlsRecipeKey != null) {

            if (dayManager.isAtLeast(20)) {

                player.discoverRecipe(
                        multiPearlsRecipeKey
                );

            } else {

                player.undiscoverRecipe(
                        multiPearlsRecipeKey
                );
            }
        }

        /*
         * ========================================
         * GUADAÑA DEL PANICO - DIA 20
         * ========================================
         */
        if (panicScytheRecipeKey != null) {

            if (dayManager.isAtLeast(20)) {

                player.discoverRecipe(
                        panicScytheRecipeKey
                );

            } else {

                player.undiscoverRecipe(
                        panicScytheRecipeKey
                );
            }
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

            updatePlayer(player);
        }
    }
}