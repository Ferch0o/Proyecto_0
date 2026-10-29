
package me.difficulty.bootstrap;

import me.difficulty.DifficultyPlugin;
import me.difficulty.core.day.DayManager;
import me.difficulty.core.items.CustomItemRegistry;
import me.difficulty.features.day7.corruptedstar.*;

import org.bukkit.plugin.PluginManager;

import java.util.Objects;

public final class Day7Bootstrap {

    private final DifficultyPlugin plugin;
    private final DayManager dayManager;

    private CorruptedStar corruptedStar;
    private CorruptedStarRecipe corruptedStarRecipe;

    public Day7Bootstrap(
            DifficultyPlugin plugin,
            DayManager dayManager
    ) {
        this.plugin = plugin;
        this.dayManager = dayManager;
    }

    // 1. CREAR Y REGISTRAR EL ITEM
    public void registerItem(CustomItemRegistry itemRegistry) {

        corruptedStar = new CorruptedStar(plugin);

        itemRegistry.register(corruptedStar);
    }

    // 2. CREAR LA RECETA
    public void createRecipe() {

        corruptedStarRecipe = new CorruptedStarRecipe(
                plugin,
                corruptedStar
        );
    }

    // 3. REGISTRAR LA RECETA
    public void registerRecipe() {

        corruptedStarRecipe.register();
    }

    // Acceso a la receta para el sistema general
    public CorruptedStarRecipe getRecipe() {
        return corruptedStarRecipe;
    }

    // 4. REGISTRAR LISTENERS
    public void registerListeners() {

        PluginManager pm =
                plugin.getServer().getPluginManager();

        pm.registerEvents(
                new CorruptedStarListener(
                        corruptedStar,
                        dayManager
                ),
                plugin
        );

        pm.registerEvents(
                new CorruptedStarRecipeListener(
                        corruptedStarRecipe.getRecipeKey(),
                        corruptedStar,
                        dayManager
                ),
                plugin
        );

        pm.registerEvents(
                new CorruptedStarRecipeBookListener(
                        corruptedStarRecipe.getRecipeKey(),
                        corruptedStar,
                        dayManager
                ),
                plugin
        );

        pm.registerEvents(
                new CorruptedStarCooldownPersistenceListener(plugin),
                plugin
        );
    }

    // 5. REGISTRAR COMANDO
    public void registerCommand() {

        Objects.requireNonNull(
                plugin.getCommand("givecorruptedstar")
        ).setExecutor(
                new GiveCorruptedStarCommand(
                        corruptedStar
                )
        );
    }
}
