
package me.difficulty.bootstrap;

import me.difficulty.DifficultyPlugin;
import me.difficulty.bootstrap.day10.Day10CommandRegistrar;
import me.difficulty.bootstrap.day10.Day10TaskRegistrar;
import me.difficulty.bootstrap.day10.Day10ZombieSkeletonListenerRegistrar;
import me.difficulty.core.items.CustomItemRegistry;
import me.difficulty.features.day10.items.Day10MaterialProtectionListener;
import me.difficulty.features.day10.items.corruptedink.*;
import me.difficulty.features.day10.items.minotaurhorn.*;
import me.difficulty.features.day10.items.explosivecrossbow.*;
import me.difficulty.features.day10.mobs.zombies.*;
import me.difficulty.features.day10.mobs.skeletons.*;
import me.difficulty.features.day10.mobs.creepers.*;
import me.difficulty.features.day10.mobs.spiders.*;
import me.difficulty.features.day10.mobs.golems.IronGolemListener;
import me.difficulty.features.day10.mobs.caballos.*;
import me.difficulty.features.pets.*;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;

import org.bukkit.plugin.PluginManager;

import java.util.Objects;

public final class Day10Bootstrap {

    private final DifficultyPlugin plugin;

    public Day10Bootstrap(DifficultyPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    // CREAR Y REGISTRAR ITEMS DEL DIA 10
    public Items registerItems(CustomItemRegistry itemRegistry) {

        // 1. Tinta Corrupta
        CorruptedInk corruptedInk =
                new CorruptedInk(plugin);

        itemRegistry.register(corruptedInk);

        // 2. Cuerno de Minotauro
        MinotaurHorn minotaurHorn =
                new MinotaurHorn(plugin);

        itemRegistry.register(minotaurHorn);

        // 3. Ballesta Explosiva
        ExplosiveCrossbow explosiveCrossbow =
                new ExplosiveCrossbow(plugin);

        itemRegistry.register(explosiveCrossbow);

        return new Items(
                corruptedInk,
                minotaurHorn,
                explosiveCrossbow
        );
    }

    // Contenedor de los tres items registrados
    public record Items(
            CorruptedInk corruptedInk,
            MinotaurHorn minotaurHorn,
            ExplosiveCrossbow explosiveCrossbow
    ) {
    }

    // CREAR Y REGISTRAR MOBS DEL DIA 10
    public Mobs registerMobs(CustomMobRegistry mobRegistry) {

        // ZOMBIS
        Infrazombie infrazombie =
                new Infrazombie(plugin);
        mobRegistry.register(infrazombie);

        CorruptedZombie corruptedZombie =
                new CorruptedZombie(plugin);
        mobRegistry.register(corruptedZombie);

        Scavenger scavenger =
                new Scavenger(plugin);
        mobRegistry.register(scavenger);

        // ESQUELETOS
        ElementalSkeleton elementalSkeleton =
                new ElementalSkeleton(plugin);
        mobRegistry.register(elementalSkeleton);

        Gladiator gladiator =
                new Gladiator(plugin);
        mobRegistry.register(gladiator);

        // CREEPER
        FuturisticCreeper futuristicCreeper =
                new FuturisticCreeper(plugin);
        mobRegistry.register(futuristicCreeper);

        // ARAÑAS
        SpeedSpider speedSpider =
                new SpeedSpider(plugin);
        mobRegistry.register(speedSpider);

        AbyssSpider abyssSpider =
                new AbyssSpider(plugin);
        mobRegistry.register(abyssSpider);

        // La Tejedora depende de los dos esqueletos.
        Weaver weaver = new Weaver(
                plugin,
                elementalSkeleton,
                gladiator
        );
        mobRegistry.register(weaver);

        Goliath goliath =
                new Goliath(plugin);
        mobRegistry.register(goliath);

        return new Mobs(
                infrazombie,
                corruptedZombie,
                scavenger,
                elementalSkeleton,
                gladiator,
                futuristicCreeper,
                speedSpider,
                abyssSpider,
                weaver,
                goliath
        );
    }

    // Instancias compartidas con listeners, tasks y comandos
    public record Mobs(
            Infrazombie infrazombie,
            CorruptedZombie corruptedZombie,
            Scavenger scavenger,
            ElementalSkeleton elementalSkeleton,
            Gladiator gladiator,
            FuturisticCreeper futuristicCreeper,
            SpeedSpider speedSpider,
            AbyssSpider abyssSpider,
            Weaver weaver,
            Goliath goliath
    ) {
    }

    // CREAR RECETA DE LA BALLESTA EXPLOSIVA
    public ExplosiveCrossbowRecipe createRecipe(Items items) {
        return new ExplosiveCrossbowRecipe(
                plugin,
                items.explosiveCrossbow(),
                items.corruptedInk(),
                items.minotaurHorn()
        );
    }

    // REGISTRAR LA RECETA EN MINECRAFT
    public void registerRecipe(ExplosiveCrossbowRecipe recipe) {
        recipe.register();
    }

    // ========================================
    // LISTENERS DIA 10 - ZOMBIS Y ESQUELETOS
    // ========================================

    public void registerZombieAndSkeletonListeners(
            DayManager dayManager,
            CustomMobRegistry mobRegistry,
            Mobs mobs
    ) {

        new Day10ZombieSkeletonListenerRegistrar(
                plugin
        ).register(
                dayManager,
                mobRegistry,
                mobs
        );
    }

    // ========================================
    // LISTENERS DIA 10 - CREEPERS
    // ========================================

    public void registerCreeperListeners(
            DayManager dayManager,
            CustomMobRegistry mobRegistry,
            Mobs mobs
    ) {

        PluginManager pm =
                plugin.getServer().getPluginManager();

        // Explosion del Creeper Futurista
        pm.registerEvents(
                new FuturisticCreeperExplosionListener(
                        dayManager,
                        mobRegistry
                ),
                plugin
        );

        // Generacion del Creeper Futurista
        pm.registerEvents(
                new CreeperSpawnListener(
                        dayManager,
                        mobs.futuristicCreeper()
                ),
                plugin
        );
    }


    // ========================================
    // LISTENERS DIA 10 - ARANAS
    // ========================================

    public void registerSpiderListeners(
            DayManager dayManager,
            CustomMobRegistry mobRegistry,
            Mobs mobs
    ) {

        PluginManager pm =
                plugin.getServer().getPluginManager();

        // Combate de la Tejedora y su jinete
        pm.registerEvents(
                new WeaverRiderCombatListener(
                        mobs.weaver(),
                        dayManager
                ),
                plugin
        );

        // Habilidades del Goliat
        pm.registerEvents(
                new GoliathListener(
                        mobRegistry,
                        dayManager
                ),
                plugin
        );

        // Comportamiento al morir la Arana Abisal
        pm.registerEvents(
                new AbyssSpiderDeathListener(
                        mobRegistry,
                        dayManager,
                        mobs.goliath()
                ),
                plugin
        );

        // Generacion de las aranas personalizadas
        pm.registerEvents(
                new SpiderSpawnListener(
                        dayManager,
                        mobs.speedSpider(),
                        mobs.abyssSpider(),
                        mobs.weaver(),
                        mobs.goliath()
                ),
                plugin
        );
    }


    // ========================================
    // LISTENERS - GOLEMS, CABALLOS Y MASCOTAS
    // ========================================

    public void registerOtherCreatureListeners(
            DayManager dayManager
    ) {

        PluginManager pm =
                plugin.getServer().getPluginManager();

        // GOLEMS DE HIERRO
        pm.registerEvents(
                new IronGolemListener(
                        dayManager
                ),
                plugin
        );

        // CABALLOS
        pm.registerEvents(
                new HorseSpawnListener(
                        dayManager
                ),
                plugin
        );

        // COMBATE DE MASCOTAS
        pm.registerEvents(
                new PetCombatListener(
                        dayManager
                ),
                plugin
        );

        // CONTROL DE LOBOS
        pm.registerEvents(
                new WolfControlListener(
                        dayManager
                ),
                plugin
        );
    }


    // ========================================
    // LISTENERS DIA 10 - ITEMS Y RECETAS
    // ========================================

    public void registerItemListeners(
            DayManager dayManager,
            Items items,
            ExplosiveCrossbowRecipe explosiveCrossbowRecipe
    ) {

        PluginManager pm =
                plugin.getServer().getPluginManager();

        // ========================================
        // TINTA CORRUPTA
        // ========================================

        pm.registerEvents(
                new CorruptedInkDropListener(
                        items.corruptedInk(),
                        dayManager
                ),
                plugin
        );

        // ========================================
        // CUERNO DE MINOTAURO
        // ========================================

        pm.registerEvents(
                new MinotaurHornDropListener(
                        items.minotaurHorn(),
                        dayManager
                ),
                plugin
        );

        // ========================================
        // PROTECCION DE MATERIALES
        // ========================================

        pm.registerEvents(
                new Day10MaterialProtectionListener(
                        explosiveCrossbowRecipe.getRecipeKey(),
                        items.corruptedInk(),
                        items.minotaurHorn()
                ),
                plugin
        );

        // ========================================
        // BALLESTA EXPLOSIVA - CRAFTEO
        // ========================================

        pm.registerEvents(
                new ExplosiveCrossbowRecipeListener(
                        plugin,
                        explosiveCrossbowRecipe.getRecipeKey(),
                        items.corruptedInk(),
                        items.minotaurHorn(),
                        items.explosiveCrossbow(),
                        dayManager
                ),
                plugin
        );

        // ========================================
        // BALLESTA EXPLOSIVA - LIBRO DE RECETAS
        // ========================================

        pm.registerEvents(
                new ExplosiveCrossbowRecipeBookListener(
                        plugin,
                        explosiveCrossbowRecipe.getRecipeKey(),
                        items.corruptedInk(),
                        items.minotaurHorn(),
                        items.explosiveCrossbow(),
                        dayManager
                ),
                plugin
        );

        // ========================================
        // BALLESTA EXPLOSIVA - DISPAROS
        // ========================================

        pm.registerEvents(
                new ExplosiveCrossbowShootListener(
                        plugin,
                        items.explosiveCrossbow(),
                        dayManager
                ),
                plugin
        );
    }

    // ========================================
    // TASKS - CRIATURAS DEL DIA 10 Y MASCOTAS
    // ========================================

    public void registerCreatureTasks(
            DayManager dayManager,
            CustomMobRegistry mobRegistry,
            Mobs mobs
    ) {

        new Day10TaskRegistrar(plugin).register(
                dayManager,
                mobRegistry,
                mobs
        );
    }

    // ========================================
    // COMANDOS - MOBS DEL DIA 10
    // ========================================

    public void registerSpawnCommands(
            DayManager dayManager,
            Mobs mobs
    ) {

        new Day10CommandRegistrar(plugin).register(
                dayManager,
                mobs
        );
    }
}
