package me.difficulty;

import me.difficulty.bootstrap.*;
import me.difficulty.commands.*;
import me.difficulty.core.day.DayManager;
import me.difficulty.core.recipes.RecipeUnlockListener;
import me.difficulty.features.day10.items.Day10MaterialProtectionListener;
import me.difficulty.features.day10.items.corruptedink.CorruptedInkDropListener;
import me.difficulty.features.day10.items.explosivecrossbow.ExplosiveCrossbowRecipeBookListener;
import me.difficulty.features.day10.items.explosivecrossbow.ExplosiveCrossbowRecipeListener;
import me.difficulty.features.day10.items.explosivecrossbow.ExplosiveCrossbowShootListener;
import me.difficulty.features.day10.items.minotaurhorn.MinotaurHornDropListener;
import me.difficulty.features.day10.mobs.caballos.HorseEffectTask;
import me.difficulty.features.day10.mobs.caballos.HorseSpawnListener;
import me.difficulty.features.day10.mobs.creepers.*;
import me.difficulty.features.day10.mobs.golems.IronGolemListener;
import me.difficulty.features.day10.mobs.skeletons.*;
import me.difficulty.features.day10.mobs.spiders.*;
import me.difficulty.features.day10.mobs.zombies.*;
import me.difficulty.features.pets.PetBehaviorTask;
import me.difficulty.features.pets.PetCombatListener;
import me.difficulty.features.pets.WolfControlListener;
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
    private Weaver weaver;
    private Goliath goliath;
    private StormManager stormManager;
    private StormMobCapTask stormMobCapTask;
    private StormExtraSpawnTask stormExtraSpawnTask;
    private StormPlayerEffectTask stormPlayerEffectTask;
    private CorruptedInk corruptedInk;
    private MinotaurHorn minotaurHorn;
    private ExplosiveCrossbow explosiveCrossbow;

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

        // Conservar las referencias utilizadas por el plugin
        corruptedInk = day10Items.corruptedInk();
        minotaurHorn = day10Items.minotaurHorn();
        explosiveCrossbow = day10Items.explosiveCrossbow();

        /*
         * ========================================
         * 4. MOBS PERSONALIZADOS
         * ========================================
         */

        Day10Bootstrap.Mobs day10Mobs = day10Bootstrap.registerMobs(mobRegistry);

        // ZOMBIS
        infrazombie = day10Mobs.infrazombie();
        corruptedZombie = day10Mobs.corruptedZombie();
        scavenger = day10Mobs.scavenger();

        // ESQUELETOS
        elementalSkeleton = day10Mobs.elementalSkeleton();
        gladiator = day10Mobs.gladiator();

        // CREEPER
        futuristicCreeper = day10Mobs.futuristicCreeper();

        // ARAÑAS
        speedSpider = day10Mobs.speedSpider();
        abyssSpider = day10Mobs.abyssSpider();
        weaver = day10Mobs.weaver();
        goliath = day10Mobs.goliath();

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

        getServer()
                .getPluginManager()
                .registerEvents(
                        new WeaverRiderCombatListener(
                                weaver,
                                dayManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new GoliathListener(
                                mobRegistry,
                                dayManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new AbyssSpiderDeathListener(
                                mobRegistry,
                                dayManager,
                                goliath
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new SpiderSpawnListener(
                                dayManager,
                                speedSpider,
                                abyssSpider,
                                weaver,
                                goliath
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new IronGolemListener(
                                dayManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new HorseSpawnListener(
                                dayManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new PetCombatListener(
                                dayManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new WolfControlListener(
                                dayManager
                        ),
                        this
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

        getServer()
                .getPluginManager()
                .registerEvents(
                        new CorruptedInkDropListener(
                                corruptedInk,
                                dayManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new MinotaurHornDropListener(
                                minotaurHorn,
                                dayManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new Day10MaterialProtectionListener(
                                explosiveCrossbowRecipe.getRecipeKey(),
                                corruptedInk,
                                minotaurHorn
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new ExplosiveCrossbowRecipeListener(
                                this,
                                explosiveCrossbowRecipe.getRecipeKey(),
                                corruptedInk,
                                minotaurHorn,
                                explosiveCrossbow,
                                dayManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new ExplosiveCrossbowRecipeBookListener(
                                this,
                                explosiveCrossbowRecipe.getRecipeKey(),
                                corruptedInk,
                                minotaurHorn,
                                explosiveCrossbow,
                                dayManager
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new ExplosiveCrossbowShootListener(
                                this,
                                explosiveCrossbow,
                                dayManager
                        ),
                        this
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

        new HorseEffectTask(
                dayManager
        ).runTaskTimer(
                this,
                0L,
                10L
        );

        new PetBehaviorTask(
                dayManager
        ).runTaskTimer(
                this,
                0L,
                5L
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

        Objects.requireNonNull(
                getCommand("spawnweaver")
        ).setExecutor(
                new SpawnWeaverCommand(
                        weaver,
                        dayManager
                )
        );

        Objects.requireNonNull(
                getCommand("spawngoliath")
        ).setExecutor(
                new SpawnGoliathCommand(
                        goliath,
                        dayManager
                )
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