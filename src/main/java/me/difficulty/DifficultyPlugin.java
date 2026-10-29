package me.difficulty;

import me.difficulty.bootstrap.*;
import me.difficulty.commands.*;
import me.difficulty.core.day.DayManager;
import me.difficulty.core.recipes.RecipeUnlockListener;
import me.difficulty.features.day10.mobs.creepers.*;
import me.difficulty.features.day10.mobs.skeletons.*;
import me.difficulty.features.day10.mobs.spiders.*;
import me.difficulty.features.day10.mobs.zombies.*;
import me.difficulty.features.storm.core.StormTimerTask;
import me.difficulty.features.storm.core.commands.StormCommand;
import me.difficulty.features.storm.core.listeners.StormBossBarListener;
import me.difficulty.features.storm.core.listeners.StormDeathListener;
import me.difficulty.features.storm.day10.listeners.StormDiamondDropListener;
import me.difficulty.features.storm.day10.listeners.StormEnderPearlListener;
import me.difficulty.features.storm.day10.listeners.StormEvokerDropListener;
import me.difficulty.features.storm.day7.StormNaturalTask;
import me.difficulty.features.storm.day10.listeners.StormCreeperListener;
import me.difficulty.core.items.CustomItemRegistry;
import me.difficulty.features.day10.items.corruptedink.CorruptedInk;
import me.difficulty.features.day10.items.explosivecrossbow.ExplosiveCrossbow;
import me.difficulty.features.day10.items.minotaurhorn.MinotaurHorn;
import me.difficulty.features.day7.corruptedstar.CorruptedStar;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.features.day10.items.explosivecrossbow.ExplosiveCrossbowRecipe;
import me.difficulty.core.recipes.RecipeUnlockManager;
import me.difficulty.features.storm.core.StormManager;
import me.difficulty.features.storm.day10.tasks.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class DifficultyPlugin extends JavaPlugin {


    private DayManager dayManager;
    private CustomItemRegistry itemRegistry;
    private RecipeUnlockManager recipeUnlockManager;
    private CustomMobRegistry mobRegistry;

    private StormManager stormManager;
    private StormMobCapTask stormMobCapTask;
    private StormExtraSpawnTask stormExtraSpawnTask;
    private StormPlayerEffectTask stormPlayerEffectTask;

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
        stormManager = new StormManager(this, dayManager);

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


        Day10Bootstrap day10Bootstrap = new Day10Bootstrap(this);

        // Crear y registrar los tres items del dia 10
        Day10Bootstrap.Items day10Items =
                day10Bootstrap.registerItems(itemRegistry);

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

        getServer()
                .getPluginManager()
                .registerEvents(
                        new StormBossBarListener(
                                this,
                                stormManager
                        ),
                        this
                );

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

        getServer()
                .getPluginManager()
                .registerEvents(
                        new StormDeathListener(
                                dayManager,
                                stormManager
                        ),
                        this
                );

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

        getServer()
                .getPluginManager()
                .registerEvents(
                        new StormCreeperListener(
                                stormManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new CreeperPotionCloudListener(stormManager),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new StormEvokerDropListener(
                                stormManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new StormEnderPearlListener(
                                stormManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new StormDiamondDropListener(
                                this,
                                stormManager
                        ),
                        this
                );

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

        day10Bootstrap.registerCreatureTasks(
                dayManager,
                mobRegistry,
                day10Mobs
        );

        new StormTimerTask(
                stormManager
        ).runTaskTimer(
                this,
                20L,
                20L
        );

        new StormNaturalTask(
                dayManager,
                stormManager
        ).runTaskTimer(
                this,
                20L,
                20L
        );


        new StormMobEffectTask(
                this,
                stormManager
        ).runTaskTimer(
                this,
                0L,
                5L
        );

        stormMobCapTask =
                new StormMobCapTask(
                        stormManager
                );


        stormMobCapTask.runTaskTimer(
                this,
                20L,
                20L
        );

        stormExtraSpawnTask =
                new StormExtraSpawnTask(
                        this,
                        stormManager
                );


        stormExtraSpawnTask.runTaskTimer(
                this,
                20L,
                20L
        );

        stormPlayerEffectTask =
                new StormPlayerEffectTask(
                        stormManager
                );


        stormPlayerEffectTask.runTaskTimer(
                this,
                200L,
                200L
        );

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

        StormCommand stormCommand =
                new StormCommand(
                        stormManager
                );


        Objects.requireNonNull(
                getCommand("storm")
        ).setExecutor(
                stormCommand
        );


        Objects.requireNonNull(
                getCommand("storm")
        ).setTabCompleter(
                stormCommand
        );


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

        if (stormMobCapTask != null) {
            stormMobCapTask.shutdown();
        }

        if (stormManager != null) {
            stormManager.saveState();
        }

        getLogger().info(
                "DifficultyPlugin apagado"
        );
    }
}