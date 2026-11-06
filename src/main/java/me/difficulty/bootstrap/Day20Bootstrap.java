
package me.difficulty.bootstrap;

import me.difficulty.DifficultyPlugin;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.items.CustomItemRegistry;
import me.difficulty.core.mobs.CustomMobRegistry;

import me.difficulty.features.day20.items.ElementalHead;
import me.difficulty.features.day20.items.ElementalHeadPlaceListener;
import me.difficulty.features.day20.items.ElementalRod;
import me.difficulty.features.day20.items.ElementalRodProtectionListener;
import me.difficulty.features.day20.mobs.blazes.*;
import me.difficulty.features.day20.mobs.withers.*;
import org.bukkit.plugin.PluginManager;

import java.util.Objects;

public final class Day20Bootstrap {

    private final DifficultyPlugin plugin;

    /*
     * ========================================
     * CONSTRUCTOR
     * ========================================
     */
    public Day20Bootstrap(
            DifficultyPlugin plugin
    ) {

        this.plugin = Objects.requireNonNull(plugin);
    }

    /*
     * ========================================
     * CONTENEDOR DE BLAZES PERSONALIZADOS
     * ========================================
     *
     * Compartiremos estas mismas instancias
     * con sus listeners de combate y drops.
     */
    public record Blazes(
            CorruptedBlaze corruptedBlaze,
            PhantomBlaze phantomBlaze,
            BlazeKing blazeKing
    ) {
    }

    /*
     * ========================================
     * REGISTRAR BLAZES
     * ========================================
     */
    public Blazes registerBlazes(
            CustomMobRegistry mobRegistry
    ) {

        /*
         * Blaze Corrupto.
         */
        CorruptedBlaze corruptedBlaze =
                new CorruptedBlaze(plugin);

        mobRegistry.register(corruptedBlaze);

        /*
         * Blaze Fantasmal.
         */
        PhantomBlaze phantomBlaze =
                new PhantomBlaze(plugin);

        mobRegistry.register(phantomBlaze);

        /*
         * Rey Blaze.
         */
        BlazeKing blazeKing =
                new BlazeKing(plugin);

        mobRegistry.register(blazeKing);

        /*
         * Devolver las instancias registradas.
         */
        return new Blazes(
                corruptedBlaze,
                phantomBlaze,
                blazeKing
        );
    }

    /*
     * ========================================
     * LISTENER DE GENERACIÓN
     * ========================================
     */
    public void registerBlazeSpawnListener(
            DayManager dayManager,
            CustomMobRegistry mobRegistry,
            Blazes blazes
    ) {

        plugin.getServer()
                .getPluginManager()
                .registerEvents(
                        new BlazeSpawnListener(
                                dayManager,
                                mobRegistry,
                                blazes.corruptedBlaze(),
                                blazes.phantomBlaze(),
                                blazes.blazeKing()
                        ),
                        plugin
                );
    }

    /*
     * ========================================
     * LISTENER DE COMBATE DE BLAZES
     * ========================================
     */
    public void registerBlazeCombatListener(
            DayManager dayManager,
            CustomMobRegistry mobRegistry
    ) {

        plugin.getServer()
                .getPluginManager()
                .registerEvents(
                        new BlazeCombatListener(
                                dayManager,
                                mobRegistry
                        ),
                        plugin
                );
    }

    /*
     * ========================================
     * HABILIDAD DEL BLAZE FANTASMAL
     * ========================================
     */
    public void registerPhantomBlazeAttackListener(
            DayManager dayManager,
            CustomMobRegistry mobRegistry
    ) {

        plugin.getServer()
                .getPluginManager()
                .registerEvents(
                        new PhantomBlazeAttackListener(
                                plugin,
                                dayManager,
                                mobRegistry
                        ),
                        plugin
                );
    }


    /*
     * ========================================
     * ATAQUE ESPECIAL DEL REY BLAZE
     * ========================================
     */
    public void registerBlazeKingAttackListener(
            DayManager dayManager,
            CustomMobRegistry mobRegistry
    ) {

        plugin.getServer()
                .getPluginManager()
                .registerEvents(
                        new BlazeKingAttackListener(
                                plugin,
                                dayManager,
                                mobRegistry
                        ),
                        plugin
                );
    }

    /*
     * ========================================
     * EMPUJE DEL REY BLAZE
     * ========================================
     */
    public void registerBlazeKingKnockbackListener(
            DayManager dayManager,
            CustomMobRegistry mobRegistry
    ) {

        plugin.getServer()
                .getPluginManager()
                .registerEvents(
                        new BlazeKingKnockbackListener(
                                plugin,
                                dayManager,
                                mobRegistry
                        ),
                        plugin
                );
    }

    /*
     * ========================================
     * DÍA 20 - REGISTRAR VARA ELEMENTAL
     * ========================================
     */
    public ElementalRod registerElementalRod(
            CustomItemRegistry itemRegistry
    ) {

        ElementalRod elementalRod =
                new ElementalRod(plugin);

        itemRegistry.register(
                elementalRod
        );

        return elementalRod;
    }


    /*
     * ========================================
     * DÍA 20 - PROTECCIÓN VARA ELEMENTAL
     * ========================================
     */
    public void registerElementalRodProtection(
            ElementalRod elementalRod
    ) {

        plugin.getServer()
                .getPluginManager()
                .registerEvents(
                        new ElementalRodProtectionListener(
                                plugin,
                                elementalRod
                        ),
                        plugin
                );
    }

    /*
     * ========================================
     * DÍA 20 - DROPS DE LOS BLAZES
     * ========================================
     */
    public void registerBlazeDropListener(
            DayManager dayManager,
            CustomMobRegistry mobRegistry,
            ElementalRod elementalRod
    ) {

        plugin.getServer()
                .getPluginManager()
                .registerEvents(
                        new BlazeDropListener(
                                dayManager,
                                mobRegistry,
                                elementalRod
                        ),
                        plugin
                );
    }


    /*
     * ========================================
     * DÍA 20 - WITHER SKELETONS
     * ========================================
     */
    public void registerWitherSkeletonContent(
            DayManager dayManager,
            CustomMobRegistry mobRegistry,
            CustomItemRegistry itemRegistry
    ) {

        /*
         * ========================================
         * REGISTRAR MOBS
         * ========================================
         */
        CorruptedWitherSkeleton corrupted =
                new CorruptedWitherSkeleton(plugin);

        FortressGuardian guardian =
                new FortressGuardian(plugin);

        mobRegistry.register(corrupted);
        mobRegistry.register(guardian);

        /*
         * ========================================
         * REGISTRAR CABEZA ELEMENTAL
         * ========================================
         */
        ElementalHead head =
                new ElementalHead(plugin);

        itemRegistry.register(head);

        /*
         * ========================================
         * REGISTRAR LISTENERS
         * ========================================
         */
        PluginManager pm = plugin.getServer()
                .getPluginManager();

        pm.registerEvents(
                new WitherSkeletonSpawnListener(
                        dayManager,
                        mobRegistry,
                        corrupted,
                        guardian
                ),
                plugin
        );

        pm.registerEvents(
                new WitherSkeletonCombatListener(
                        dayManager,
                        mobRegistry
                ),
                plugin
        );

        pm.registerEvents(
                new WitherSkeletonDropListener(
                        dayManager,
                        mobRegistry,
                        head
                ),
                plugin
        );

        pm.registerEvents(
                new ElementalHeadPlaceListener(head),
                plugin
        );

        /*
         * ========================================
         * COMANDOS DE PRUEBA
         * ========================================
         */
        Objects.requireNonNull(
                plugin.getCommand("spawncorruptedwither"),
                "Falta spawncorruptedwither en plugin.yml"
        ).setExecutor(
                new SpawnWitherSkeletonCommand(
                        dayManager,
                        corrupted,
                        "Wither Corrupto"
                )
        );

        Objects.requireNonNull(
                plugin.getCommand("spawnfortressguardian"),
                "Falta spawnfortressguardian en plugin.yml"
        ).setExecutor(
                new SpawnWitherSkeletonCommand(
                        dayManager,
                        guardian,
                        "Guardián de la Fortaleza"
                )
        );
    }

    /*
     * ========================================
     * COMANDOS DE PRUEBA - BLAZES DÍA 20
     * ========================================
     */
    public void registerBlazeSpawnCommands(
            DayManager dayManager,
            Blazes blazes
    ) {

        /*
         * Blaze Corrupto.
         */
        Objects.requireNonNull(
                plugin.getCommand("spawncorruptedblaze"),
                "Falta spawncorruptedblaze en plugin.yml"
        ).setExecutor(
                new SpawnCorruptedBlazeCommand(
                        blazes.corruptedBlaze(),
                        dayManager
                )
        );

        /*
         * Blaze Fantasmal.
         */
        Objects.requireNonNull(
                plugin.getCommand("spawnphantomblaze"),
                "Falta spawnphantomblaze en plugin.yml"
        ).setExecutor(
                new SpawnPhantomBlazeCommand(
                        blazes.phantomBlaze(),
                        dayManager
                )
        );

        /*
         * Rey Blaze.
         */
        Objects.requireNonNull(
                plugin.getCommand("spawnblazeking"),
                "Falta spawnblazeking en plugin.yml"
        ).setExecutor(
                new SpawnBlazeKingCommand(
                        blazes.blazeKing(),
                        dayManager
                )
        );
    }
}
