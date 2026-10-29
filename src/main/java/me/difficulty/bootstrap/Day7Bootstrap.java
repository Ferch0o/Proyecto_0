
package me.difficulty.bootstrap;

import me.difficulty.DifficultyPlugin;
import me.difficulty.core.day.DayManager;
import me.difficulty.core.items.CustomItemRegistry;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.features.day7.corruptedstar.*;

import me.difficulty.features.day7.gigante.GiantAiTask;
import me.difficulty.features.day7.gigante.GiantCombatTask;
import me.difficulty.features.day7.gigante.GiantZombie;
import me.difficulty.features.day7.gigante.SpawnGiantCommand;
import org.bukkit.plugin.PluginManager;

import java.util.Objects;

public final class Day7Bootstrap {

    private final DifficultyPlugin plugin;
    private final DayManager dayManager;

    private CorruptedStar corruptedStar;
    private CorruptedStarRecipe corruptedStarRecipe;
    private GiantZombie giantZombie;

    public Day7Bootstrap(
            DifficultyPlugin plugin,
            DayManager dayManager
    ) {
        this.plugin = plugin;
        this.dayManager = dayManager;
    }

    // REGISTRAR ZOMBIE GIGANTE DEL DIA 7
    public void registerMob(CustomMobRegistry mobRegistry) {

        giantZombie = new GiantZombie(plugin);

        mobRegistry.register(giantZombie);
    }

    // REGISTRAR TAREAS DEL ZOMBIE GIGANTE
    public void registerGiantTasks(
            CustomMobRegistry mobRegistry
    ) {

        // IA DE MOVIMIENTO Y PERSECUCION
        plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                new GiantAiTask(
                        plugin,
                        dayManager,
                        mobRegistry
                ),
                20L,
                20L
        );

        // IA DE COMBATE
        plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                new GiantCombatTask(
                        plugin,
                        dayManager,
                        mobRegistry
                ),
                5L,
                5L
        );
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


        // COMANDO /spawngiant
        Objects.requireNonNull(
                plugin.getCommand("spawngiant")
        ).setExecutor(
                new SpawnGiantCommand(
                        giantZombie,
                        dayManager
                )
        );

    }
}
