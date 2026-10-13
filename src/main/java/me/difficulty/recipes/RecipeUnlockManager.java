package me.difficulty.recipes;

import me.difficulty.day.DayManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RecipeUnlockManager {

    private final JavaPlugin plugin;
    private final DayManager dayManager;
    private final NamespacedKey corruptedStarRecipeKey;

    public RecipeUnlockManager(
            JavaPlugin plugin,
            DayManager dayManager,
            NamespacedKey corruptedStarRecipeKey
    ) {
        this.plugin = plugin;
        this.dayManager = dayManager;
        this.corruptedStarRecipeKey = corruptedStarRecipeKey;
    }

    /*
     * Comprueba qué recetas debe conocer
     * un jugador dependiendo del día.
     */
    public void updatePlayer(Player player) {

        if (dayManager.isAtLeast(7)) {

            player.discoverRecipe(
                    corruptedStarRecipeKey
            );

        } else {

            player.undiscoverRecipe(
                    corruptedStarRecipeKey
            );
        }
    }

    /*
     * Actualiza las recetas de todos
     * los jugadores conectados.
     */
    public void updateAllPlayers() {

        for (Player player :
                plugin.getServer().getOnlinePlayers()) {

            updatePlayer(player);
        }
    }
}