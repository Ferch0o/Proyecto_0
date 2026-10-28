
package me.difficulty.bootstrap;

import me.difficulty.DifficultyPlugin;
import me.difficulty.core.items.CustomItemRegistry;
import me.difficulty.features.day10.items.corruptedink.CorruptedInk;
import me.difficulty.features.day10.items.minotaurhorn.MinotaurHorn;
import me.difficulty.features.day10.items.explosivecrossbow.ExplosiveCrossbow;

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
}
