
package me.difficulty.bootstrap;

import me.difficulty.DifficultyPlugin;
import me.difficulty.core.items.CustomItemRegistry;
import me.difficulty.features.day10.items.corruptedink.CorruptedInk;
import me.difficulty.features.day10.items.explosivecrossbow.ExplosiveCrossbowRecipe;
import me.difficulty.features.day10.items.minotaurhorn.MinotaurHorn;
import me.difficulty.features.day10.items.explosivecrossbow.ExplosiveCrossbow;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.features.day10.mobs.zombies.*;
import me.difficulty.features.day10.mobs.skeletons.*;
import me.difficulty.features.day10.mobs.creepers.*;
import me.difficulty.features.day10.mobs.spiders.*;

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

}
