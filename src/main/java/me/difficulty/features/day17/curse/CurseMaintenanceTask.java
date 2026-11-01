package me.difficulty.features.day17.curse;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class CurseMaintenanceTask extends BukkitRunnable {
    private final CurseManager manager;
    private final CurseInventoryService inventory;
    private final NamespacedKey potatoRecipeKey;

    public CurseMaintenanceTask(CurseManager manager, CurseInventoryService inventory,
                                NamespacedKey potatoRecipeKey) {
        this.manager = manager;
        this.inventory = inventory;
        this.potatoRecipeKey = potatoRecipeKey;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            manager.refresh(player);
            inventory.synchronize(player);
            // Desbloquear también la receta en el libro a partir del día 17.
            boolean discovered = player.hasDiscoveredRecipe(potatoRecipeKey);
            if (manager.currentDay() >= 17 && !discovered) player.discoverRecipe(potatoRecipeKey);
            if (manager.currentDay() < 17 && discovered) player.undiscoverRecipe(potatoRecipeKey);
        }
    }
}
