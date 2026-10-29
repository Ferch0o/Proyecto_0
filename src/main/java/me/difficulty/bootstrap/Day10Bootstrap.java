package me.difficulty.bootstrap;

import me.difficulty.DifficultyPlugin;
import me.difficulty.bootstrap.day10.*;
import me.difficulty.core.items.CustomItemRegistry;
import me.difficulty.features.day10.items.corruptedink.*;
import me.difficulty.features.day10.items.minotaurhorn.*;
import me.difficulty.features.day10.items.explosivecrossbow.*;
import me.difficulty.features.day10.mobs.zombies.*;
import me.difficulty.features.day10.mobs.skeletons.*;
import me.difficulty.features.day10.mobs.creepers.*;
import me.difficulty.features.day10.mobs.spiders.*;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;

import java.util.Objects;

public final class Day10Bootstrap {

    private final DifficultyPlugin plugin;

    public Day10Bootstrap(DifficultyPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    // ========================================
    // ITEMS PERSONALIZADOS DEL DIA 10
    // ========================================

    public Items registerItems(
            CustomItemRegistry itemRegistry
    ) {

        return new Day10ItemRegistrar(plugin)
                .register(itemRegistry);
    }

    // Contenedor de los tres items registrados
    public record Items(
            CorruptedInk corruptedInk,
            MinotaurHorn minotaurHorn,
            ExplosiveCrossbow explosiveCrossbow
    ) {
    }

    // ========================================
    // MOBS PERSONALIZADOS DEL DIA 10
    // ========================================

    public Mobs registerMobs(
            CustomMobRegistry mobRegistry
    ) {

        return new Day10MobRegistrar(plugin)
                .register(mobRegistry);
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

        new Day10ZombieSkeletonListenerRegistrar(plugin)
                .register(
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

        new Day10CreeperListenerRegistrar(plugin)
                .register(
                        dayManager,
                        mobRegistry,
                        mobs
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

        new Day10SpiderListenerRegistrar(plugin)
                .register(
                        dayManager,
                        mobRegistry,
                        mobs
                );
    }

    // ========================================
    // LISTENERS - GOLEMS, CABALLOS Y MASCOTAS
    // ========================================

    public void registerOtherCreatureListeners(
            DayManager dayManager
    ) {

        new Day10OtherCreatureListenerRegistrar(plugin)
                .register(
                        dayManager
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
        new Day10ItemListenerRegistrar(plugin)
                .register(
                        dayManager,
                        items,
                        explosiveCrossbowRecipe
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
