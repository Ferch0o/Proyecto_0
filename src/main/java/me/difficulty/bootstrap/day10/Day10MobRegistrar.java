
package me.difficulty.bootstrap.day10;

import me.difficulty.DifficultyPlugin;
import me.difficulty.bootstrap.Day10Bootstrap;

import me.difficulty.core.mobs.CustomMobRegistry;

import me.difficulty.features.day10.mobs.zombies.*;
import me.difficulty.features.day10.mobs.skeletons.*;
import me.difficulty.features.day10.mobs.creepers.*;
import me.difficulty.features.day10.mobs.spiders.*;

import java.util.Objects;

public final class Day10MobRegistrar {

    private final DifficultyPlugin plugin;

    public Day10MobRegistrar(DifficultyPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    // ========================================
    // MOBS PERSONALIZADOS DEL DIA 10
    // ========================================

    public Day10Bootstrap.Mobs register(
            CustomMobRegistry mobRegistry
    ) {

        // ========================================
        // ZOMBIS
        // ========================================

        Infrazombie infrazombie =
                new Infrazombie(plugin);

        mobRegistry.register(infrazombie);

        CorruptedZombie corruptedZombie =
                new CorruptedZombie(plugin);

        mobRegistry.register(corruptedZombie);

        Scavenger scavenger =
                new Scavenger(plugin);

        mobRegistry.register(scavenger);

        // ========================================
        // ESQUELETOS
        // ========================================

        ElementalSkeleton elementalSkeleton =
                new ElementalSkeleton(plugin);

        mobRegistry.register(elementalSkeleton);

        Gladiator gladiator =
                new Gladiator(plugin);

        mobRegistry.register(gladiator);

        // ========================================
        // CREEPER FUTURISTA
        // ========================================

        FuturisticCreeper futuristicCreeper =
                new FuturisticCreeper(plugin);

        mobRegistry.register(futuristicCreeper);

        // ========================================
        // ARANAS
        // ========================================

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

        // ========================================
        // DEVOLVER INSTANCIAS REGISTRADAS
        // ========================================

        return new Day10Bootstrap.Mobs(
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
}