
package me.difficulty.bootstrap.day10;

import me.difficulty.DifficultyPlugin;
import me.difficulty.bootstrap.Day10Bootstrap;

import me.difficulty.core.items.CustomItemRegistry;

import me.difficulty.features.day10.items.corruptedink.CorruptedInk;
import me.difficulty.features.day10.items.minotaurhorn.MinotaurHorn;
import me.difficulty.features.day10.items.explosivecrossbow.ExplosiveCrossbow;

import java.util.Objects;

public final class Day10ItemRegistrar {

    private final DifficultyPlugin plugin;

    public Day10ItemRegistrar(DifficultyPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    // ========================================
    // ITEMS PERSONALIZADOS DEL DIA 10
    // ========================================

    public Day10Bootstrap.Items register(
            CustomItemRegistry itemRegistry
    ) {

        // ========================================
        // TINTA CORRUPTA
        // ========================================

        CorruptedInk corruptedInk =
                new CorruptedInk(plugin);

        itemRegistry.register(corruptedInk);

        // ========================================
        // CUERNO DE MINOTAURO
        // ========================================

        MinotaurHorn minotaurHorn =
                new MinotaurHorn(plugin);

        itemRegistry.register(minotaurHorn);

        // ========================================
        // BALLESTA EXPLOSIVA
        // ========================================

        ExplosiveCrossbow explosiveCrossbow =
                new ExplosiveCrossbow(plugin);

        itemRegistry.register(explosiveCrossbow);

        // ========================================
        // DEVOLVER INSTANCIAS REGISTRADAS
        // ========================================

        return new Day10Bootstrap.Items(
                corruptedInk,
                minotaurHorn,
                explosiveCrossbow
        );
    }
}