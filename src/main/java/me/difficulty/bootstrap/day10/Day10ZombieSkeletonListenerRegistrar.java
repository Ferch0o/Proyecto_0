
package me.difficulty.bootstrap.day10;

import me.difficulty.DifficultyPlugin;
import me.difficulty.bootstrap.Day10Bootstrap;
import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;

import me.difficulty.features.day10.mobs.zombies.CorruptedZombieCombatListener;
import me.difficulty.features.day10.mobs.zombies.ZombieSpawnListener;
import me.difficulty.features.day10.mobs.skeletons.ElementalSkeletonCombatListener;
import me.difficulty.features.day10.mobs.skeletons.SkeletonSpawnListener;

import org.bukkit.plugin.PluginManager;
import java.util.Objects;

public final class Day10ZombieSkeletonListenerRegistrar {

    private final DifficultyPlugin plugin;

    public Day10ZombieSkeletonListenerRegistrar(
            DifficultyPlugin plugin
    ) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    // ========================================
    // LISTENERS - ZOMBIS Y ESQUELETOS
    // ========================================

    public void register(
            DayManager dayManager,
            CustomMobRegistry mobRegistry,
            Day10Bootstrap.Mobs mobs
    ) {

        PluginManager pm =
                plugin.getServer().getPluginManager();

        // ========================================
        // ZOMBIS
        // ========================================

        // Combate del Zombie Corrupto
        pm.registerEvents(
                new CorruptedZombieCombatListener(
                        mobRegistry,
                        dayManager
                ),
                plugin
        );

        // Generacion de los tres zombies
        pm.registerEvents(
                new ZombieSpawnListener(
                        dayManager,
                        mobs.infrazombie(),
                        mobs.corruptedZombie(),
                        mobs.scavenger()
                ),
                plugin
        );

        // ========================================
        // ESQUELETOS
        // ========================================

        // Combate del Esqueleto Elemental
        pm.registerEvents(
                new ElementalSkeletonCombatListener(
                        mobRegistry,
                        dayManager
                ),
                plugin
        );

        // Generacion de los dos esqueletos
        pm.registerEvents(
                new SkeletonSpawnListener(
                        dayManager,
                        mobs.elementalSkeleton(),
                        mobs.gladiator()
                ),
                plugin
        );
    }
}