package me.difficulty.listeners;

import me.difficulty.recipes.RecipeUnlockManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class RecipeUnlockListener implements Listener {

    private final RecipeUnlockManager recipeUnlockManager;

    public RecipeUnlockListener(
            RecipeUnlockManager recipeUnlockManager
    ) {
        this.recipeUnlockManager =
                recipeUnlockManager;
    }

    @EventHandler
    public void onPlayerJoin(
            PlayerJoinEvent event
    ) {

        recipeUnlockManager.updatePlayer(
                event.getPlayer()
        );
    }
}