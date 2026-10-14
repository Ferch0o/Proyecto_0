package me.difficulty;

import me.difficulty.commands.*;
import me.difficulty.day.DayManager;
import me.difficulty.items.CustomItemRegistry;
import me.difficulty.items.day7.CorruptedStar;
import me.difficulty.listeners.*;
import me.difficulty.mobs.CustomMobRegistry;
import me.difficulty.mobs.day10.*;
import me.difficulty.recipes.CorruptedStarRecipe;
import me.difficulty.recipes.RecipeUnlockManager;
import me.difficulty.tasks.GladiatorWeaponTask;
import me.difficulty.tasks.SpeedSpiderClimbTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class DifficultyPlugin extends JavaPlugin {

    private DayManager dayManager;
    private CustomItemRegistry itemRegistry;
    private CorruptedStar corruptedStar;
    private RecipeUnlockManager recipeUnlockManager;
    private CustomMobRegistry mobRegistry;
    private Infrazombie infrazombie;
    private CorruptedZombie corruptedZombie;
    private Scavenger scavenger;
    private ElementalSkeleton elementalSkeleton;
    private Gladiator gladiator;
    private FuturisticCreeper futuristicCreeper;
    private SpeedSpider speedSpider;
    private AbyssSpider abyssSpider;

    @Override
    public void onEnable() {

        /*
         * ========================================
         * 1. CONFIGURACIÓN
         * ========================================
         */

        saveDefaultConfig();


        /*
         * ========================================
         * 2. SISTEMAS PRINCIPALES
         * ========================================
         */

        dayManager =
                new DayManager(this);

        itemRegistry =
                new CustomItemRegistry(this);

        mobRegistry =
                new CustomMobRegistry(this);

        /*
         * ========================================
         * 3. ITEMS PERSONALIZADOS
         * ========================================
         */

        corruptedStar =
                new CorruptedStar(this);

        itemRegistry.register(
                corruptedStar
        );

        /*
         * ========================================
         * 4. MOBS PERSONALIZADOS
         * ========================================
         */

        infrazombie =
                new Infrazombie(this);

        mobRegistry.register(
                infrazombie
        );

        corruptedZombie =
                new CorruptedZombie(this);

        mobRegistry.register(
                corruptedZombie
        );

        scavenger =
                new Scavenger(this);

        mobRegistry.register(
                scavenger
        );

        elementalSkeleton =
                new ElementalSkeleton(this);

        mobRegistry.register(
                elementalSkeleton
        );

        gladiator =
                new Gladiator(this);

        mobRegistry.register(
                gladiator
        );

        futuristicCreeper =
                new FuturisticCreeper(this);

        mobRegistry.register(
                futuristicCreeper
        );

        speedSpider =
                new SpeedSpider(this);

        mobRegistry.register(
                speedSpider
        );

        abyssSpider =
                new AbyssSpider(this);

        mobRegistry.register(
                abyssSpider
        );

        /*
         * ========================================
         * 4. RECETAS
         * ========================================
         */

        CorruptedStarRecipe corruptedStarRecipe =
                new CorruptedStarRecipe(
                        this,
                        corruptedStar
                );

        /*
         * IMPORTANTE:
         *
         * Primero registramos la receta
         * en Minecraft.
         */
        corruptedStarRecipe.register();


        /*
         * ========================================
         * 5. DESBLOQUEO DE RECETAS
         * ========================================
         */

        recipeUnlockManager =
                new RecipeUnlockManager(
                        this,
                        dayManager,
                        corruptedStarRecipe.getRecipeKey()
                );


        /*
         * ========================================
         * 6. LISTENERS
         * ========================================
         */

        getServer()
                .getPluginManager()
                .registerEvents(
                        new CorruptedStarListener(
                                corruptedStar,
                                dayManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new CorruptedStarRecipeListener(
                                corruptedStarRecipe.getRecipeKey(),
                                corruptedStar,
                                dayManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new CorruptedStarRecipeBookListener(
                                corruptedStarRecipe.getRecipeKey(),
                                corruptedStar,
                                dayManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new RecipeUnlockListener(
                                recipeUnlockManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new CorruptedZombieCombatListener(
                                mobRegistry,
                                dayManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new ZombieSpawnListener(
                                dayManager,
                                infrazombie,
                                corruptedZombie,
                                scavenger
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new ElementalSkeletonCombatListener(
                                mobRegistry,
                                dayManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new SkeletonSpawnListener(
                                dayManager,
                                elementalSkeleton,
                                gladiator
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new FuturisticCreeperExplosionListener(
                                dayManager,
                                mobRegistry
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new CreeperSpawnListener(
                                dayManager,
                                futuristicCreeper
                        ),
                        this
                );

        /*
         * ========================================
         * 8. TAREAS
         * ========================================
         */

        new GladiatorWeaponTask(
                this,
                dayManager,
                mobRegistry,
                gladiator
        ).runTaskTimer(
                this,
                0L,
                10L
        );

        new SpeedSpiderClimbTask(
                dayManager,
                mobRegistry
        ).runTaskTimer(
                this,
                0L,
                2L
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

        Objects.requireNonNull(
                getCommand("givecorruptedstar")
        ).setExecutor(
                new GiveCorruptedStarCommand(
                        corruptedStar
                )
        );

        Objects.requireNonNull(
                getCommand("spawninfrazombie")
        ).setExecutor(
                new SpawnInfrazombieCommand(
                        infrazombie,
                        dayManager
                )
        );

        Objects.requireNonNull(
                getCommand("spawncorruptedzombie")
        ).setExecutor(
                new SpawnCorruptedZombieCommand(
                        corruptedZombie,
                        dayManager
                )
        );

        Objects.requireNonNull(
                getCommand("spawnscavenger")
        ).setExecutor(
                new SpawnScavengerCommand(
                        scavenger,
                        dayManager
                )
        );

        Objects.requireNonNull(
                getCommand("spawnelementalskeleton")
        ).setExecutor(
                new SpawnElementalSkeletonCommand(
                        elementalSkeleton,
                        dayManager
                )
        );

        Objects.requireNonNull(
                getCommand("spawngladiator")
        ).setExecutor(
                new SpawnGladiatorCommand(
                        gladiator,
                        dayManager
                )
        );

        Objects.requireNonNull(
                getCommand("spawnfuturisticcreeper")
        ).setExecutor(
                new SpawnFuturisticCreeperCommand(
                        futuristicCreeper,
                        dayManager
                )
        );

        Objects.requireNonNull(
                getCommand("spawnspeedspider")
        ).setExecutor(
                new SpawnSpeedSpiderCommand(
                        speedSpider,
                        dayManager
                )
        );

        Objects.requireNonNull(
                getCommand("spawnabyssspider")
        ).setExecutor(
                new SpawnAbyssSpiderCommand(
                        abyssSpider,
                        dayManager
                )
        );

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

        getLogger().info(
                "DifficultyPlugin apagado"
        );
    }
}