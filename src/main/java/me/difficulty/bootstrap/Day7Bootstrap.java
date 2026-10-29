
package me.difficulty.bootstrap;

import me.difficulty.DifficultyPlugin;
import me.difficulty.core.day.DayManager;
import me.difficulty.core.items.CustomItemRegistry;
import me.difficulty.core.mobs.CustomMobRegistry;

import me.difficulty.features.day7.corruptedstar.*;
import me.difficulty.features.day7.gigante.*;

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

    // ========================================
    // MOBS DEL DIA 7
    // ========================================

    public void registerMob(CustomMobRegistry mobRegistry) {

        giantZombie = new GiantZombie(plugin);
        mobRegistry.register(giantZombie);
    }

    // ========================================
    // TAREAS DEL ZOMBIE GIGANTE
    // ========================================

    public void registerGiantTasks(
            CustomMobRegistry mobRegistry
    ) {

        // Persecucion
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

        // Combate
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

    // ========================================
    // ESTRELLA CORRUPTA
    // ========================================

    public void registerItem(
            CustomItemRegistry itemRegistry
    ) {

        corruptedStar = new CorruptedStar(plugin);
        itemRegistry.register(corruptedStar);
    }

    public void createRecipe() {

        corruptedStarRecipe = new CorruptedStarRecipe(
                plugin,
                corruptedStar
        );
    }

    public void registerRecipe() {
        corruptedStarRecipe.register();
    }

    public CorruptedStarRecipe getRecipe() {
        return corruptedStarRecipe;
    }

    // ========================================
    // LISTENERS
    // ========================================

    public void registerListeners() {

        PluginManager pm =
                plugin.getServer().getPluginManager();

        // Estrella Corrupta
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
                new CorruptedStarCooldownPersistenceListener(
                        plugin
                ),
                plugin
        );

        // Generacion natural del Gigante
        pm.registerEvents(
                new GiantSpawnListener(
                        plugin,
                        dayManager,
                        giantZombie
                ),
                plugin
        );

        // Recompensa del Gigante
        pm.registerEvents(
                new GiantDropListener(
                        dayManager,
                        giantZombie
                ),
                plugin
        );
    }

    // ========================================
    // COMANDOS
    // ========================================

    public void registerCommand() {

        Objects.requireNonNull(
                plugin.getCommand("givecorruptedstar")
        ).setExecutor(
                new GiveCorruptedStarCommand(
                        corruptedStar
                )
        );

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