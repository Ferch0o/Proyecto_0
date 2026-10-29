
package me.difficulty.bootstrap.day10;

import me.difficulty.DifficultyPlugin;
import me.difficulty.bootstrap.Day10Bootstrap;
import me.difficulty.core.day.DayManager;

import me.difficulty.features.day10.mobs.zombies.*;
import me.difficulty.features.day10.mobs.skeletons.*;
import me.difficulty.features.day10.mobs.creepers.*;
import me.difficulty.features.day10.mobs.spiders.*;

import java.util.Objects;

public final class Day10CommandRegistrar {

    private final DifficultyPlugin plugin;

    public Day10CommandRegistrar(DifficultyPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    // ========================================
    // COMANDOS DE LOS MOBS DEL DIA 10
    // ========================================

    public void register(
            DayManager dayManager,
            Day10Bootstrap.Mobs mobs
    ) {

        // ========================================
        // ZOMBIS
        // ========================================

        Objects.requireNonNull(
                plugin.getCommand("spawninfrazombie")
        ).setExecutor(
                new SpawnInfrazombieCommand(
                        mobs.infrazombie(),
                        dayManager
                )
        );

        Objects.requireNonNull(
                plugin.getCommand("spawncorruptedzombie")
        ).setExecutor(
                new SpawnCorruptedZombieCommand(
                        mobs.corruptedZombie(),
                        dayManager
                )
        );

        Objects.requireNonNull(
                plugin.getCommand("spawnscavenger")
        ).setExecutor(
                new SpawnScavengerCommand(
                        mobs.scavenger(),
                        dayManager
                )
        );

        // ========================================
        // ESQUELETOS
        // ========================================

        Objects.requireNonNull(
                plugin.getCommand("spawnelementalskeleton")
        ).setExecutor(
                new SpawnElementalSkeletonCommand(
                        mobs.elementalSkeleton(),
                        dayManager
                )
        );

        Objects.requireNonNull(
                plugin.getCommand("spawngladiator")
        ).setExecutor(
                new SpawnGladiatorCommand(
                        mobs.gladiator(),
                        dayManager
                )
        );

        // ========================================
        // CREEPER FUTURISTA
        // ========================================

        Objects.requireNonNull(
                plugin.getCommand("spawnfuturisticcreeper")
        ).setExecutor(
                new SpawnFuturisticCreeperCommand(
                        mobs.futuristicCreeper(),
                        dayManager
                )
        );

        // ========================================
        // ARANAS
        // ========================================

        Objects.requireNonNull(
                plugin.getCommand("spawnspeedspider")
        ).setExecutor(
                new SpawnSpeedSpiderCommand(
                        mobs.speedSpider(),
                        dayManager
                )
        );

        Objects.requireNonNull(
                plugin.getCommand("spawnabyssspider")
        ).setExecutor(
                new SpawnAbyssSpiderCommand(
                        mobs.abyssSpider(),
                        dayManager
                )
        );

        Objects.requireNonNull(
                plugin.getCommand("spawnweaver")
        ).setExecutor(
                new SpawnWeaverCommand(
                        mobs.weaver(),
                        dayManager
                )
        );

        Objects.requireNonNull(
                plugin.getCommand("spawngoliath")
        ).setExecutor(
                new SpawnGoliathCommand(
                        mobs.goliath(),
                        dayManager
                )
        );
    }
}