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
import me.difficulty.features.day20.items.ElementalRod;
import org.bukkit.NamespacedKey;
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
         * DÍA 17 - MALDICIÓN DEL INFRAMUNDO
         * Y PATATA ELEMENTAL
         * ========================================
         */

        Day17Bootstrap day17Bootstrap =
                new Day17Bootstrap(
                        this,
                        dayManager
                );

        day17Bootstrap.register(
                itemRegistry,
                mobRegistry
        );


        /*
         * ========================================
         * DÍA 20 - NUEVOS MOBS DEL NETHER
         * ========================================
         */

        Day20Bootstrap day20Bootstrap =
                new Day20Bootstrap(this);

        /*
         * Registrar los tres Blazes.
         *
         * El registro no significa que aparecerán
         * antes del día 20: eso lo controla
         * BlazeSpawnListener.
         */
        Day20Bootstrap.Blazes day20Blazes =
                day20Bootstrap.registerBlazes(mobRegistry);

        /*
         * ========================================
         * DÍA 20 - VARA ELEMENTAL
         * ========================================
         */
        ElementalRod elementalRod =
                day20Bootstrap.registerElementalRod(
                        itemRegistry
                );

        day20Bootstrap.registerElementalRodProtection(
                elementalRod
        );

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

        /*
         * ========================================
         * DÍA 20 - APARICIÓN DE BLAZES
         * ========================================
         *
         * Natural y Spawner.
         */
        day20Bootstrap.registerBlazeSpawnListener(
                dayManager,
                mobRegistry,
                day20Blazes
        );

        day20Bootstrap.registerBlazeCombatListener(
                dayManager,
                mobRegistry
        );

        day20Bootstrap.registerPhantomBlazeAttackListener(
                dayManager,
                mobRegistry
        );

        day20Bootstrap.registerBlazeKingAttackListener(
                dayManager,
                mobRegistry
        );

        day20Bootstrap.registerBlazeKingKnockbackListener(
                dayManager,
                mobRegistry
        );

        day20Bootstrap.registerBlazeDropListener(
                dayManager,
                mobRegistry,
                elementalRod
        );

        /*
         * ========================================
         * DÍA 20 - WITHER SKELETONS
         * ========================================
         */
        day20Bootstrap.registerWitherSkeletonContent(
                dayManager,
                mobRegistry,
                itemRegistry
        );

        /*
         * ========================================
         * DÍA 20 - GENERACIÓN NATURAL POR BIOMAS
         * ========================================
         */
        day20Bootstrap.registerBiomeNaturalSpawnListener(
                dayManager,
                mobRegistry
        );

        /*
         * ========================================
         * DÍA 20 - GHASTS
         * ========================================
         */
        day20Bootstrap.registerGhastContent(
                dayManager,
                mobRegistry,
                itemRegistry
        );

        /*
         * ========================================
         * DÍA 20 - PIGMAN CORRUPTO Y NINJA
         * ========================================
         */
        day20Bootstrap.registerPigmanContent(
                dayManager,
                mobRegistry,
                itemRegistry
        );

        /*
         * ========================================
         * DÍA 20 - PIGLINS
         * ========================================
         */
        day20Bootstrap.registerPiglinContent(
                dayManager,
                mobRegistry,
                itemRegistry
        );

        /*
         * ========================================
         * DÍA 20 - ENDERMEN
         * ========================================
         */
        day20Bootstrap.registerEndermanContent(
                dayManager,
                mobRegistry,
                itemRegistry
        );

        /*
         * ========================================
         * DIA 20 - MULTI PEARLS
         * ========================================
         *
         * Se registra despues de los Endermen
         * porque necesita la Perla Elemental.
         */
        NamespacedKey multiPearlsRecipeKey =
                day20Bootstrap.registerMultiPearlsContent(
                        dayManager,
                        itemRegistry
                );

        /*
         * Registrar la receta para controlar
         * su desbloqueo desde el dia 20.
         */
        recipeUnlockManager.registerMultiPearlsRecipe(
                multiPearlsRecipeKey
        );

        /*
         * ========================================
         * DÍA 20 - STRIDERS
         * ========================================
         */
        day20Bootstrap.registerStriderContent(
                dayManager
        );

        /*
         * ========================================
         * DÍA 20 - GENERACION DE ANCIENT DEBRIS
         * ========================================
         */
        day20Bootstrap.registerAncientDebrisContent(
                dayManager
        );

        /*
         * ========================================
         * DIA 20 - MINERALES DE ORO
         * ========================================
         */
        day20Bootstrap.registerGoldOreContent(
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

        day20Bootstrap.registerBlazeSpawnCommands(
                dayManager,
                day20Blazes
        );

        day10Bootstrap.registerItemCommands(
                day10Items
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