
package me.difficulty.bootstrap;

import me.difficulty.core.day.DayManager;
import me.difficulty.features.day7.corruptedstar.CorruptedStar;
import me.difficulty.features.day7.corruptedstar.CorruptedStarListener;
import me.difficulty.features.day7.corruptedstar.CorruptedStarRecipe;
import me.difficulty.features.day7.corruptedstar.CorruptedStarRecipeListener;
import me.difficulty.features.day7.corruptedstar.CorruptedStarRecipeBookListener;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Day7Bootstrap {

    private final JavaPlugin plugin;
    private final DayManager dayManager;
    private final CorruptedStar corruptedStar;
    private final CorruptedStarRecipe corruptedStarRecipe;

    public Day7Bootstrap(
            JavaPlugin plugin,
            DayManager dayManager,
            CorruptedStar corruptedStar,
            CorruptedStarRecipe corruptedStarRecipe
    ) {
        this.plugin = plugin;
        this.dayManager = dayManager;
        this.corruptedStar = corruptedStar;
        this.corruptedStarRecipe = corruptedStarRecipe;
    }

    public void registerListeners() {

        PluginManager pm =
                plugin.getServer().getPluginManager();

        // Uso de la Estrella Corrupta
        pm.registerEvents(
                new CorruptedStarListener(
                        corruptedStar,
                        dayManager
                ),
                plugin
        );

        // Protección de su receta
        pm.registerEvents(
                new CorruptedStarRecipeListener(
                        corruptedStarRecipe.getRecipeKey(),
                        corruptedStar,
                        dayManager
                ),
                plugin
        );

        // Integración con el libro de recetas
        pm.registerEvents(
                new CorruptedStarRecipeBookListener(
                        corruptedStarRecipe.getRecipeKey(),
                        corruptedStar,
                        dayManager
                ),
                plugin
        );
    }
}
