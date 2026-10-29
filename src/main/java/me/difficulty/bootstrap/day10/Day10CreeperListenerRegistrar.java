
package me.difficulty.bootstrap.day10;

import me.difficulty.DifficultyPlugin;
import me.difficulty.bootstrap.Day10Bootstrap;
import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;

import me.difficulty.features.day10.mobs.creepers.CreeperSpawnListener;
import me.difficulty.features.day10.mobs.creepers.FuturisticCreeperExplosionListener;

import org.bukkit.plugin.PluginManager;
import java.util.Objects;

public final class Day10CreeperListenerRegistrar {

    private final DifficultyPlugin plugin;

    public Day10CreeperListenerRegistrar(DifficultyPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    public void register(
            DayManager dayManager,
            CustomMobRegistry mobRegistry,
            Day10Bootstrap.Mobs mobs
    ) {

        PluginManager pm = plugin.getServer().getPluginManager();

        // CREEPER FUTURISTA - EXPLOSION
        pm.registerEvents(
                new FuturisticCreeperExplosionListener(
                        dayManager,
                        mobRegistry
                ),
                plugin
        );

        // CREEPER FUTURISTA - GENERACION
        pm.registerEvents(
                new CreeperSpawnListener(
                        dayManager,
                        mobs.futuristicCreeper()
                ),
                plugin
        );
    }
}