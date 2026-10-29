package me.difficulty;

import me.difficulty.bootstrap.*;
import me.difficulty.bootstrap.storm.StormBootstrap;
import me.difficulty.commands.*;
import me.difficulty.core.day.DayManager;
import me.difficulty.core.recipes.RecipeUnlockListener;
import me.difficulty.core.items.CustomItemRegistry;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.features.day10.items.explosivecrossbow.ExplosiveCrossbowRecipe;
import me.difficulty.core.recipes.RecipeUnlockManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class DifficultyPlugin extends JavaPlugin {

    private DayManager dayManager;
    private CustomItemRegistry itemRegistry;
    private RecipeUnlockManager recipeUnlockManager;
    private CustomMobRegistry mobRegistry;

    private StormBootstrap stormBootstrap;

    @Override
    public void onEnable() {

        /*
         * ========================================
         * 1. CONFIGURACIÓN
         * ========================================
         */

        saveDefaultConfig();

        QualityOfLifeBootstrap qualityOfLifeBootstrap =
                new QualityOfLifeBootstrap(this);

        /*
         * ========================================
         * 2. SISTEMAS PRINCIPALES
         * ========================================
         */

        CoreBootstrap coreBootstrap = new CoreBootstrap(this);

        // 1. Sistema de días
        dayManager = coreBootstrap.createDayManager();

        // 2. Sistema de Tormenta
        stormBootstrap = new StormBootstrap(
                this,
                dayManager
        );

        // 3. Registros compartidos
        CoreBootstrap.Registries registries = coreBootstrap.createRegistries();

        itemRegistry = registries.itemRegistry();
        mobRegistry = registries.mobRegistry();

        /*
         * ========================================
         * 3. ITEMS PERSONALIZADOS
         * ========================================
         */

        Day7Bootstrap day7Bootstrap = new Day7Bootstrap(this, dayManager);
        day7Bootstrap.registerItem(itemRegistry);
        day7Bootstrap.registerMob(mobRegistry);

        Day10Bootstrap day10Bootstrap = new Day10Bootstrap(this);
        // Crear y registrar los tres items del dia 10
        Day10Bootstrap.Items day10Items = day10Bootstrap.registerItems(itemRegistry);

        /*
         * ========================================
         * 4. MOBS PERSONALIZADOS
         * ========================================
         */

        Day10Bootstrap.Mobs day10Mobs = day10Bootstrap.registerMobs(mobRegistry);

        /*
         * ========================================
         * 4. RECETAS
         * ========================================
         */

        day7Bootstrap.createRecipe();
        ExplosiveCrossbowRecipe explosiveCrossbowRecipe = day10Bootstrap.createRecipe(day10Items);

        /*
         * IMPORTANTE:
         *
         * Primero registramos la receta
         * en Minecraft.
         */

        day7Bootstrap.registerRecipe();
        day10Bootstrap.registerRecipe(explosiveCrossbowRecipe);

        /*
         * ========================================
         * 5. DESBLOQUEO DE RECETAS
         * ========================================
         */

        recipeUnlockManager =
                new RecipeUnlockManager(
                        this,
                        dayManager,
                        day7Bootstrap.getRecipe().getRecipeKey(),
                        explosiveCrossbowRecipe.getRecipeKey()
                );

        /*
         * ========================================
         * 6. LISTENERS
         * ========================================
         */

        /*
         * ========================================
         * BOSSBAR DE TORMENTA
         * RECONEXIÓN DE JUGADORES
         * ========================================
         */

        stormBootstrap.registerBossBarListener();

        /*
         * ========================================
         * CALIDAD DE VIDA
         * FUEGO AMIGO DESACTIVADO
         * ========================================
         *
         * Disponible desde el Día 0.
         * Afecta a todos los jugadores.
         */

        qualityOfLifeBootstrap.registerFriendlyFire();

        stormBootstrap.registerDeathListener();

        day7Bootstrap.registerListeners();

        getServer()
                .getPluginManager()
                .registerEvents(
                        new RecipeUnlockListener(
                                recipeUnlockManager
                        ),
                        this
                );

        day10Bootstrap.registerZombieAndSkeletonListeners(
                dayManager,
                mobRegistry,
                day10Mobs
        );


        day10Bootstrap.registerCreeperListeners(
                dayManager,
                mobRegistry,
                day10Mobs
        );


        day10Bootstrap.registerSpiderListeners(
                dayManager,
                mobRegistry,
                day10Mobs
        );

        day10Bootstrap.registerOtherCreatureListeners(
                dayManager
        );

        stormBootstrap.registerDay10Listeners();

        day10Bootstrap.registerItemListeners(
                dayManager,
                day10Items,
                explosiveCrossbowRecipe
        );

        /*
         * ========================================
         * CALIDAD DE VIDA
         * DESCUENTOS COMPARTIDOS DE ALDEANOS
         * ========================================
         */

        qualityOfLifeBootstrap.registerVillagerListener();

        /*
         * ========================================
         * 8. TAREAS
         * ========================================
         */

        // Inteligencia artificial del Zombie Gigante
        day7Bootstrap.registerGiantTasks(mobRegistry);

        day10Bootstrap.registerCreatureTasks(
                dayManager,
                mobRegistry,
                day10Mobs
        );

        stormBootstrap.registerTasks();

        /*
         * ========================================
         * 7. COMANDOS
         * ========================================
         */

        Objects.requireNonNull(
                getCommand("day")
        ).setExecutor(
                new DayCommand(
                        dayManager
                )
        );

        Objects.requireNonNull(
                getCommand("setday")
        ).setExecutor(
                new SetDayCommand(
                        dayManager,
                        recipeUnlockManager
                )
        );

        day7Bootstrap.registerCommand();

        day10Bootstrap.registerSpawnCommands(
                dayManager,
                day10Mobs
        );

        stormBootstrap.registerCommand();

        /*
         * ========================================
         * COMANDO SHARED CURE
         * ========================================
         */

        qualityOfLifeBootstrap.registerVillagerCommand();

        /*
         * ========================================
         * 8. ACTUALIZAR JUGADORES CONECTADOS
         * ========================================
         *
         * Esto ocurre DESPUÉS de registrar
         * la receta.
         */

        recipeUnlockManager.updateAllPlayers();

        /*
         * ========================================
         * 9. MENSAJE DE INICIO
         * ========================================
         */

        getLogger().info(
                "DifficultyPlugin iniciado correctamente. Día actual: "
                        + dayManager.getCurrentDay()
        );
    }

    @Override
    public void onDisable() {

        if (stormBootstrap != null) {
            stormBootstrap.shutdown();
        }

        getLogger().info(
                "DifficultyPlugin apagado"
        );
    }
}