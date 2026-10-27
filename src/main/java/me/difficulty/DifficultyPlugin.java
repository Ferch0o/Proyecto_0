package me.difficulty;

import me.difficulty.commands.*;
import me.difficulty.day.DayManager;
import me.difficulty.features.day7.corruptedstar.CorruptedStarListener;
import me.difficulty.features.day7.corruptedstar.CorruptedStarRecipeBookListener;
import me.difficulty.features.day7.corruptedstar.CorruptedStarRecipeListener;
import me.difficulty.features.day7.corruptedstar.GiveCorruptedStarCommand;
import me.difficulty.features.qualityoflife.friendlyfire.FriendlyFireProtectionListener;
import me.difficulty.items.CustomItemRegistry;
import me.difficulty.items.day10.CorruptedInk;
import me.difficulty.items.day10.ExplosiveCrossbow;
import me.difficulty.items.day10.MinotaurHorn;
import me.difficulty.features.day7.corruptedstar.CorruptedStar;
import me.difficulty.listeners.*;
import me.difficulty.mobs.CustomMobRegistry;
import me.difficulty.mobs.day10.*;
import me.difficulty.features.day7.corruptedstar.CorruptedStarRecipe;
import me.difficulty.recipes.ExplosiveCrossbowRecipe;
import me.difficulty.recipes.RecipeUnlockManager;
import me.difficulty.storm.StormManager;
import me.difficulty.tasks.*;
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


        /*
         * ========================================
         * 2. SISTEMAS PRINCIPALES
         * ========================================
         */

        dayManager =
                new DayManager(this);

        stormManager =
                new StormManager(
                        this,
                        dayManager
                );

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

        corruptedInk =
                new CorruptedInk(
                        this
                );

        itemRegistry.register(
                corruptedInk
        );

        minotaurHorn =
                new MinotaurHorn(
                        this
                );

        itemRegistry.register(
                minotaurHorn
        );

        explosiveCrossbow =
                new ExplosiveCrossbow(
                        this
                );

        itemRegistry.register(
                explosiveCrossbow
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

        weaver =
                new Weaver(
                        this,
                        elementalSkeleton,
                        gladiator
                );

        mobRegistry.register(
                weaver
        );

        goliath =
                new Goliath(this);

        mobRegistry.register(
                goliath
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

        ExplosiveCrossbowRecipe explosiveCrossbowRecipe =
                new ExplosiveCrossbowRecipe(
                        this,
                        explosiveCrossbow,
                        corruptedInk,
                        minotaurHorn
                );

        /*
         * IMPORTANTE:
         *
         * Primero registramos la receta
         * en Minecraft.
         */
        corruptedStarRecipe.register();

        explosiveCrossbowRecipe.register();
        /*
         * ========================================
         * 5. DESBLOQUEO DE RECETAS
         * ========================================
         */

        recipeUnlockManager =
                new RecipeUnlockManager(
                        this,
                        dayManager,
                        corruptedStarRecipe.getRecipeKey(),
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

        getServer()
                .getPluginManager()
                .registerEvents(
                        new FriendlyFireProtectionListener(this),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new StormDeathListener(
                                dayManager,
                                stormManager
                        ),
                        this
                );

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

        SharedVillagerCureListener sharedVillagerCureListener =
                new SharedVillagerCureListener(this);

        getServer()
                .getPluginManager()
                .registerEvents(
                        sharedVillagerCureListener,
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

        Objects.requireNonNull(
                getCommand("sharedcure")
        ).setExecutor(
                new SharedCureCommand(
                        sharedVillagerCureListener
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