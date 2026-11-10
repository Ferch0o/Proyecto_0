
package me.difficulty.bootstrap;

import me.difficulty.DifficultyPlugin;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.items.CustomItemRegistry;
import me.difficulty.core.items.ItemIds;
import me.difficulty.core.mobs.CustomMobRegistry;

import me.difficulty.features.day10.items.explosivecrossbow.ExplosiveCrossbow;
import me.difficulty.features.day17.items.Day17Material;
import me.difficulty.features.day20.items.*;
import me.difficulty.features.day20.mobs.Day20BiomeNaturalSpawnListener;
import me.difficulty.features.day20.mobs.blazes.*;
import me.difficulty.features.day20.mobs.endermen.*;
import me.difficulty.features.day20.mobs.ghasts.*;
import me.difficulty.features.day20.mobs.piglins.*;
import me.difficulty.features.day20.mobs.pigmen.*;
import me.difficulty.features.day20.mobs.striders.StriderRideListener;
import me.difficulty.features.day20.mobs.withers.*;
import me.difficulty.features.day20.worldgen.*;
import org.bukkit.NamespacedKey;
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
     * DÍA 20 - APARICIONES NATURALES
     * POR BIOMA
     * ========================================
     */
    public void registerBiomeNaturalSpawnListener(
            DayManager dayManager,
            CustomMobRegistry mobRegistry
    ) {

        plugin.getServer()
                .getPluginManager()
                .registerEvents(
                        new Day20BiomeNaturalSpawnListener(
                                plugin,
                                dayManager,
                                mobRegistry
                        ),
                        plugin
                );
    }

    /*
     * ========================================
     * DÍA 20 - GHASTS PERSONALIZADOS
     * ========================================
     */
    public void registerGhastContent(
            DayManager dayManager,
            CustomMobRegistry mobRegistry,
            CustomItemRegistry itemRegistry
    ) {

        /*
         * ========================================
         * REGISTRAR GHASTS
         * ========================================
         */
        CorruptedGhast corrupted =
                new CorruptedGhast(plugin);

        DemonicGhast demonic =
                new DemonicGhast(plugin);

        mobRegistry.register(corrupted);
        mobRegistry.register(demonic);

        /*
         * ========================================
         * REGISTRAR LÁGRIMA ELEMENTAL
         * ========================================
         */
        ElementalTear elementalTear =
                new ElementalTear(plugin);

        itemRegistry.register(elementalTear);

        /*
         * ========================================
         * REGISTRAR LISTENERS
         * ========================================
         */
        PluginManager pm = plugin.getServer()
                .getPluginManager();

        pm.registerEvents(
                new GhastSpawnListener(
                        dayManager,
                        mobRegistry,
                        corrupted,
                        demonic
                ),
                plugin
        );

        pm.registerEvents(
                new CorruptedGhastCombatListener(
                        dayManager,
                        mobRegistry
                ),
                plugin
        );

        pm.registerEvents(
                new DemonicGhastAttackListener(
                        plugin,
                        dayManager,
                        mobRegistry
                ),
                plugin
        );

        pm.registerEvents(
                new GhastSelfProjectileProtectionListener(),
                plugin
        );

        pm.registerEvents(
                new GhastDropListener(
                        dayManager,
                        mobRegistry,
                        elementalTear
                ),
                plugin
        );

        pm.registerEvents(
                new ElementalTearProtectionListener(
                        plugin,
                        elementalTear
                ),
                plugin
        );

        /*
         * ========================================
         * COMANDOS DE PRUEBA
         * ========================================
         */
        Objects.requireNonNull(
                plugin.getCommand("spawncorruptedghast"),
                "Falta spawncorruptedghast en plugin.yml"
        ).setExecutor(
                new SpawnGhastCommand(
                        dayManager,
                        corrupted,
                        "Ghast Corrupto"
                )
        );

        Objects.requireNonNull(
                plugin.getCommand("spawndemonicghast"),
                "Falta spawndemonicghast en plugin.yml"
        ).setExecutor(
                new SpawnGhastCommand(
                        dayManager,
                        demonic,
                        "Ghast Demoníaco"
                )
        );
    }

    /*
     * ========================================
     * DÍA 20 - PIGMAN CORRUPTO Y NINJA
     * ========================================
     */
    public void registerPigmanContent(
            DayManager dayManager,
            CustomMobRegistry mobRegistry,
            CustomItemRegistry itemRegistry
    ) {

        /*
         * ========================================
         * REGISTRAR MOBS
         * ========================================
         */
        CorruptedPigman corrupted =
                new CorruptedPigman(plugin);

        NinjaPigman ninja =
                new NinjaPigman(plugin);

        mobRegistry.register(corrupted);
        mobRegistry.register(ninja);

        /*
         * ========================================
         * REGISTRAR CARNE ELEMENTAL
         * ========================================
         */
        ElementalMeat elementalMeat =
                new ElementalMeat(plugin);

        itemRegistry.register(elementalMeat);

        /*
         * ========================================
         * REGISTRAR LISTENERS
         * ========================================
         */
        PluginManager pm = plugin.getServer()
                .getPluginManager();

        pm.registerEvents(
                new PigmanSpawnListener(
                        dayManager,
                        mobRegistry,
                        corrupted,
                        ninja
                ),
                plugin
        );

        pm.registerEvents(
                new PigmanCombatListener(
                        dayManager,
                        mobRegistry
                ),
                plugin
        );

        pm.registerEvents(
                new PigmanDropListener(
                        dayManager,
                        mobRegistry,
                        elementalMeat
                ),
                plugin
        );

        pm.registerEvents(
                new ElementalMeatConsumeListener(
                        plugin,
                        elementalMeat
                ),
                plugin
        );

        /*
         * ========================================
         * COMANDOS DE PRUEBA
         * ========================================
         */
        Objects.requireNonNull(
                plugin.getCommand("spawncorruptedpigman"),
                "Falta spawncorruptedpigman en plugin.yml"
        ).setExecutor(
                new SpawnPigmanCommand(
                        dayManager,
                        corrupted,
                        "Pigman Corrupto"
                )
        );

        Objects.requireNonNull(
                plugin.getCommand("spawnninja"),
                "Falta spawnninja en plugin.yml"
        ).setExecutor(
                new SpawnPigmanCommand(
                        dayManager,
                        ninja,
                        "Ninja"
                )
        );
    }

    /*
     * ========================================
     * DÍA 20 - PIGLINS PERSONALIZADOS
     * ========================================
     */
    public void registerPiglinContent(
            DayManager dayManager,
            CustomMobRegistry mobRegistry,
            CustomItemRegistry itemRegistry
    ) {

        /*
         * REUTILIZAR BALLESTA EXPLOSIVA.
         */
        ExplosiveCrossbow crossbow =
                new ExplosiveCrossbow(plugin);

        /*
         * ========================================
         * REGISTRAR LAS TRES VARIANTES
         * ========================================
         */
        CorruptedPiglin corrupted =
                new CorruptedPiglin(plugin);

        ExplosivePiglin explosive =
                new ExplosivePiglin(
                        plugin,
                        crossbow
                );

        KingPiglin king =
                new KingPiglin(plugin);

        mobRegistry.register(corrupted);
        mobRegistry.register(explosive);
        mobRegistry.register(king);

        /*
         * ========================================
         * REGISTRAR ORO ELEMENTAL
         * ========================================
         */
        ElementalGold elementalGold =
                new ElementalGold(plugin);

        itemRegistry.register(elementalGold);

        PluginManager pm = plugin.getServer()
                .getPluginManager();

        /*
         * ========================================
         * PROTECCIÓN DEL ORO ELEMENTAL
         * ========================================
         */
        pm.registerEvents(
                new ElementalGoldProtectionListener(plugin),
                plugin
        );

        /*
         * ========================================
         * APARICIONES
         * ========================================
         */
        pm.registerEvents(
                new PiglinSpawnListener(
                        dayManager,
                        mobRegistry,
                        corrupted,
                        explosive,
                        king
                ),
                plugin
        );

        /*
         * ========================================
         * COMBATE
         * ========================================
         */
        pm.registerEvents(
                new PiglinCombatListener(
                        dayManager,
                        mobRegistry
                ),
                plugin
        );

        /*
         * ========================================
         * DROPS
         * ========================================
         */
        pm.registerEvents(
                new PiglinDropListener(
                        dayManager,
                        mobRegistry,
                        elementalGold
                ),
                plugin
        );

        /*
         * ========================================
         * ORO Y TRUEQUES
         * ========================================
         */
        pm.registerEvents(
                new PiglinGoldListener(
                        plugin,
                        dayManager,
                        mobRegistry
                ),
                plugin
        );

        /*
         * ========================================
         * AGRESIVIDAD GENERAL
         * ========================================
         *
         * Cada 10 ticks.
         */
        new PiglinAggressionTask(
                plugin,
                dayManager,
                mobRegistry
        ).runTaskTimer(
                plugin,
                20L,
                10L
        );

        /*
         * ========================================
         * COMANDOS DE PRUEBA
         * ========================================
         */

        /*
         * PIGLIN CORRUPTO
         */
        Objects.requireNonNull(
                plugin.getCommand("spawncorruptedpiglin"),
                "Falta spawncorruptedpiglin en plugin.yml"
        ).setExecutor(
                new SpawnPiglinCommand(
                        dayManager,
                        corrupted
                )
        );

        /*
         * PIGLIN EXPLOSIVO
         */
        Objects.requireNonNull(
                plugin.getCommand("spawnexplosivepiglin"),
                "Falta spawnexplosivepiglin en plugin.yml"
        ).setExecutor(
                new SpawnExplosivePiglinCommand(
                        dayManager,
                        explosive
                )
        );

        /*
         * REY PIGLIN
         */
        Objects.requireNonNull(
                plugin.getCommand("spawnkingpiglin"),
                "Falta spawnkingpiglin en plugin.yml"
        ).setExecutor(
                new SpawnKingPiglinCommand(
                        dayManager,
                        king
                )
        );
    }

    /*
     * ========================================
     * DÍA 20 - ENDERMEN PERSONALIZADOS
     * ========================================
     */
    public void registerEndermanContent(
            DayManager dayManager,
            CustomMobRegistry mobRegistry,
            CustomItemRegistry itemRegistry
    ) {

        /*
         * ========================================
         * REGISTRAR VARIANTES
         * ========================================
         */
        CorruptedEnderman corrupted =
                new CorruptedEnderman(plugin);

        AncestralEnderman ancestral =
                new AncestralEnderman(plugin);

        mobRegistry.register(corrupted);
        mobRegistry.register(ancestral);

        /*
         * ========================================
         * REGISTRAR PERLA ELEMENTAL
         * ========================================
         */
        ElementalPearl elementalPearl =
                new ElementalPearl(plugin);

        itemRegistry.register(elementalPearl);

        PluginManager pm = plugin.getServer()
                .getPluginManager();

        /*
         * ========================================
         * APARICIONES: 70% / 30%
         * ========================================
         */
        pm.registerEvents(
                new EndermanSpawnListener(
                        dayManager,
                        mobRegistry,
                        corrupted,
                        ancestral
                ),
                plugin
        );

        /*
         * ========================================
         * COMBATE
         * ========================================
         */
        pm.registerEvents(
                new EndermanCombatListener(
                        dayManager,
                        mobRegistry
                ),
                plugin
        );

        /*
         * ========================================
         * DROPS
         * ========================================
         */
        pm.registerEvents(
                new EndermanDropListener(
                        dayManager,
                        mobRegistry,
                        elementalPearl
                ),
                plugin
        );

        /*
         * ========================================
         * VELOCIDAD DEL ENDERMAN CORRUPTO
         * ========================================
         */
        new CorruptedEndermanSpeedTask(
                dayManager,
                mobRegistry
        ).runTaskTimer(
                plugin,
                20L,
                10L
        );

        /*
         * ========================================
         * PROTECCIÓN DE PERLAS ELEMENTALES
         * ========================================
         */
        pm.registerEvents(
                new ElementalPearlProtectionListener(
                        plugin,
                        elementalPearl
                ),
                plugin
        );

        /*
         * ========================================
         * TELETRANSPORTE DE PERLAS ELEMENTALES
         * ========================================
         */
        pm.registerEvents(
                new ElementalPearlTeleportListener(
                        plugin,
                        elementalPearl
                ),
                plugin
        );

        /*
         * ========================================
         * COMANDO - ENDERMAN CORRUPTO
         * ========================================
         */
        Objects.requireNonNull(
                plugin.getCommand("spawncorruptedenderman"),
                "Falta spawncorruptedenderman en plugin.yml"
        ).setExecutor(
                new SpawnEndermanCommand(
                        dayManager,
                        corrupted,
                        "Enderman Corrupto"
                )
        );

        /*
         * ========================================
         * COMANDO - ENDERMAN ANCESTRAL
         * ========================================
         */
        Objects.requireNonNull(
                plugin.getCommand("spawnancestralenderman"),
                "Falta spawnancestralenderman en plugin.yml"
        ).setExecutor(
                new SpawnEndermanCommand(
                        dayManager,
                        ancestral,
                        "Enderman Ancestral"
                )
        );

        /*
         * ========================================
         * COMANDO - PERLAS ELEMENTALES
         * ========================================
         */
        Objects.requireNonNull(
                plugin.getCommand("giveelementalpearl"),
                "Falta giveelementalpearl en plugin.yml"
        ).setExecutor(
                new GiveElementalPearlCommand(
                        dayManager,
                        elementalPearl
                )
        );
    }

    /*
     * ========================================
     * DÍA 20 - STRIDERS
     * ========================================
     *
     * Efectos mientras el jugador monta:
     * - Jugador: Resistencia III y Fuego.
     * - Strider: Resistencia III.
     * - Strider inmune a explosiones.
     *
     * Al desmontar:
     * - Resistencia al Fuego por 3 minutos.
     */

    public void registerStriderContent(
            DayManager dayManager
    ) {

        StriderRideListener striderRideListener =
                new StriderRideListener(
                        plugin,
                        dayManager
                );

        /*
         * Registrar eventos de montura,
         * desmontura y explosiones.
         */
        plugin.getServer()
                .getPluginManager()
                .registerEvents(
                        striderRideListener,
                        plugin
                );

        /*
         * Revisar los efectos
         * cada 10 ticks.
         */
        striderRideListener.runTaskTimer(
                plugin,
                0L,
                10L
        );
    }

    /*
     * ========================================
     * DIA 20 - ANCIENT DEBRIS
     * ========================================
     *
     * - Sin generacion natural en chunks nuevos.
     * - 80% de no obtener drops al romperlo.
     * - Los chunks antiguos se conservan.
     */

    public void registerAncientDebrisContent(
            DayManager dayManager
    ) {

        PluginManager pm = plugin.getServer()
                .getPluginManager();

        /*
         * GENERACION EN CHUNKS NUEVOS
         */
        AncientDebrisWorldListener worldListener =
                new AncientDebrisWorldListener(
                        dayManager
                );

        pm.registerEvents(
                worldListener,
                plugin
        );

        /*
         * Registrar tambien en mundos
         * que ya estan cargados.
         */
        worldListener.registerLoadedWorlds();

        /*
         * CONTROL DEL DROP
         */
        pm.registerEvents(
                new AncientDebrisDropListener(
                        dayManager
                ),
                plugin
        );
    }

    /*
     * ========================================
     * DIA 20 - MINERALES DE ORO
     * ========================================
     *
     * - Eliminar generacion natural.
     * - Bloquear drops al picar.
     * - Bloquear drops por explosiones.
     */

    public void registerGoldOreContent(
            DayManager dayManager
    ) {

        PluginManager pm = plugin.getServer()
                .getPluginManager();

        /*
         * ========================================
         * GENERACION DE CHUNKS NUEVOS
         * ========================================
         */
        GoldOreWorldListener worldListener =
                new GoldOreWorldListener(
                        dayManager
                );

        pm.registerEvents(
                worldListener,
                plugin
        );

        worldListener.registerLoadedWorlds();

        /*
         * ========================================
         * BLOQUEAR DROPS AL PICAR
         * ========================================
         */
        pm.registerEvents(
                new GoldOreDropListener(
                        dayManager
                ),
                plugin
        );

        /*
         * ========================================
         * BLOQUEAR DROPS POR EXPLOSIONES
         * ========================================
         */
        pm.registerEvents(
                new GoldOreExplosionListener(
                        plugin,
                        dayManager
                ),
                plugin
        );
    }

    /*
     * ========================================
     * DIA 20 - MULTI PEARLS
     * ========================================
     *
     * - Registrar nuevo item.
     * - Registrar receta.
     * - Proteger de recetas vanilla.
     * - Activar desde el dia 20.
     */
    public NamespacedKey registerMultiPearlsContent(
            DayManager dayManager,
            CustomItemRegistry itemRegistry
    ) {

        /*
         * Recuperar la Perla Elemental
         * ya registrada por los Endermen.
         */
        ElementalPearl elementalPearl =
                (ElementalPearl) itemRegistry.get(
                        ItemIds.ELEMENTAL_PEARL
                ).orElseThrow(
                        () -> new IllegalStateException(
                                "La Perla Elemental debe "
                                        + "registrarse antes de Multi Pearls."
                        )
                );

        /*
         * ========================================
         * REGISTRAR MULTI PEARLS
         * ========================================
         */
        MultiPearls multiPearls =
                new MultiPearls(plugin);

        itemRegistry.register(multiPearls);

        /*
         * ========================================
         * REGISTRAR RECETA
         * ========================================
         */
        MultiPearlsRecipe recipe =
                new MultiPearlsRecipe(
                        plugin,
                        dayManager,
                        elementalPearl,
                        multiPearls
                );

        recipe.register();

        /*
         * ========================================
         * MULTI PEARLS - VALIDACION Y CRAFTEO
         * ========================================
         */
        plugin.getServer()
                .getPluginManager()
                .registerEvents(
                        recipe,
                        plugin
                );

        /*
         * ========================================
         * MULTI PEARLS - LIBRO DE RECETAS
         * ========================================
         *
         * Misma metodologia que la
         * Estrella Corrupta del dia 7.
         */
        plugin.getServer()
                .getPluginManager()
                .registerEvents(
                        new MultiPearlsRecipeBookListener(
                                plugin,
                                dayManager,
                                elementalPearl,
                                multiPearls,
                                recipe.getRecipeKey()
                        ),
                        plugin
                );

        return recipe.getRecipeKey();
    }

    /*
     * ========================================
     * DIA 20 - GUADAÑA DEL PANICO
     * ========================================
     */
    public NamespacedKey registerPanicScytheContent(
            DayManager dayManager,
            CustomItemRegistry itemRegistry
    ) {

        ElementalGold elementalGold =
                (ElementalGold) itemRegistry.get(
                        ItemIds.ELEMENTAL_GOLD
                ).orElseThrow(
                        () -> new IllegalStateException(
                                "El Oro Elemental debe "
                                        + "registrarse primero."
                        )
                );

        MultiPearls multiPearls =
                (MultiPearls) itemRegistry.get(
                        ItemIds.MULTI_PEARLS
                ).orElseThrow(
                        () -> new IllegalStateException(
                                "Multi Pearls debe "
                                        + "registrarse primero."
                        )
                );

        /*
         * Registrar el objeto.
         */
        PanicScythe panicScythe =
                new PanicScythe(plugin);

        itemRegistry.register(panicScythe);

        /*
         * Registrar receta.
         */
        PanicScytheRecipe recipe =
                new PanicScytheRecipe(
                        plugin,
                        dayManager,
                        elementalGold,
                        multiPearls,
                        panicScythe
                );

        recipe.register();

        PluginManager pm =
                plugin.getServer().getPluginManager();

        /*
         * Validacion y consumo.
         */
        pm.registerEvents(
                recipe,
                plugin
        );

        /*
         * Libro de recetas.
         */
        pm.registerEvents(
                new PanicScytheRecipeBookListener(
                        plugin,
                        dayManager,
                        elementalGold,
                        multiPearls,
                        panicScythe,
                        recipe.getRecipeKey()
                ),
                plugin
        );

        /*
         * Habilidad de teletransporte.
         */
        pm.registerEvents(
                new PanicScytheAbilityListener(
                        dayManager,
                        panicScythe
                ),
                plugin
        );

        return recipe.getRecipeKey();
    }

    /*
     * ========================================
     * DIA 20 - LINGOTE ELEMENTAL
     * ========================================
     */
    public NamespacedKey registerElementalIngotContent(
            DayManager dayManager,
            CustomItemRegistry itemRegistry
    ) {

        /*
         * Recuperar Bloque de Titanio
         * registrado desde el día 17.
         */
        Day17Material titanium =
                (Day17Material) itemRegistry.get(
                        ItemIds.TITANIUM_BLOCK
                ).orElseThrow(
                        () -> new IllegalStateException(
                                "El Bloque de Titanio debe "
                                        + "registrarse primero."
                        )
                );

        /*
         * ========================================
         * REGISTRAR OBJETO
         * ========================================
         */
        ElementalIngot elementalIngot =
                new ElementalIngot(plugin);

        itemRegistry.register(elementalIngot);

        /*
         * ========================================
         * REGISTRAR RECETA
         * ========================================
         */
        ElementalIngotRecipe recipe =
                new ElementalIngotRecipe(
                        plugin,
                        dayManager,
                        titanium,
                        elementalIngot
                );

        recipe.register();

        PluginManager pm =
                plugin.getServer().getPluginManager();

        /*
         * Controlar cantidades y fabricación.
         */
        pm.registerEvents(
                recipe,
                plugin
        );

        /*
         * Libro de recetas.
         */
        pm.registerEvents(
                new ElementalIngotRecipeBookListener(
                        dayManager,
                        titanium,
                        elementalIngot,
                        recipe.getRecipeKey()
                ),
                plugin
        );

        /*
         * Proteger contra usos vanilla.
         */
        pm.registerEvents(
                new ElementalIngotProtectionListener(
                        elementalIngot
                ),
                plugin
        );

        return recipe.getRecipeKey();
    }

    /*
     * ========================================
     * DIA 20 - PLANTILLA ELEMENTAL
     * ========================================
     */
    public record ElementalTemplateKeys(
            NamespacedKey original,
            NamespacedKey copy
    ) {
    }

    public ElementalTemplateKeys registerElementalTemplateContent(
            DayManager dayManager,
            CustomItemRegistry itemRegistry
    ) {

        ElementalRod rod =
                (ElementalRod) itemRegistry.get(
                        ItemIds.ELEMENTAL_ROD
                ).orElseThrow();

        ElementalHead head =
                (ElementalHead) itemRegistry.get(
                        ItemIds.ELEMENTAL_HEAD
                ).orElseThrow();

        ElementalTear tear =
                (ElementalTear) itemRegistry.get(
                        ItemIds.ELEMENTAL_TEAR
                ).orElseThrow();

        ElementalMeat meat =
                (ElementalMeat) itemRegistry.get(
                        ItemIds.ELEMENTAL_MEAT
                ).orElseThrow();

        MultiPearls multiPearls =
                (MultiPearls) itemRegistry.get(
                        ItemIds.MULTI_PEARLS
                ).orElseThrow();

        /*
         * Registrar objeto personalizado.
         */
        ElementalTemplate template =
                new ElementalTemplate(plugin);

        itemRegistry.register(template);

        /*
         * Registrar las dos recetas.
         */
        ElementalTemplateRecipe recipes =
                new ElementalTemplateRecipe(
                        plugin,
                        dayManager,
                        rod,
                        head,
                        tear,
                        meat,
                        multiPearls,
                        template
                );

        recipes.register();

        PluginManager pm =
                plugin.getServer().getPluginManager();

        /*
         * Control de cantidades y fabricación.
         */
        pm.registerEvents(
                recipes,
                plugin
        );

        /*
         * Libro de recetas.
         */
        pm.registerEvents(
                new ElementalTemplateRecipeBookListener(
                        dayManager,
                        recipes
                ),
                plugin
        );

        /*
         * Protección contra usos vanilla.
         */
        pm.registerEvents(
                new ElementalTemplateProtectionListener(
                        template,
                        recipes.getCopyKey()
                ),
                plugin
        );

        return new ElementalTemplateKeys(
                recipes.getOriginalKey(),
                recipes.getCopyKey()
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
