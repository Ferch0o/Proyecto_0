
package me.difficulty.bootstrap;

import me.difficulty.DifficultyPlugin;
import me.difficulty.core.day.DayManager;
import me.difficulty.core.items.CustomItemRegistry;
import me.difficulty.core.mobs.CustomMobRegistry;

import java.util.Objects;

public final class CoreBootstrap {

    private final DifficultyPlugin plugin;

    public CoreBootstrap(DifficultyPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    // Inicializar sistema de días
    public DayManager createDayManager() {
        return new DayManager(plugin);
    }

    // Inicializar registros generales
    public Registries createRegistries() {

        CustomItemRegistry itemRegistry =
                new CustomItemRegistry(plugin);

        CustomMobRegistry mobRegistry =
                new CustomMobRegistry(plugin);

        return new Registries(
                itemRegistry,
                mobRegistry
        );
    }

    // Contenedor de los registros compartidos
    public record Registries(
            CustomItemRegistry itemRegistry,
            CustomMobRegistry mobRegistry
    ) {
    }
}
