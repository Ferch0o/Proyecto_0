
package me.difficulty.bootstrap.day10;

import me.difficulty.DifficultyPlugin;
import me.difficulty.bootstrap.Day10Bootstrap;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;

import me.difficulty.features.day10.mobs.skeletons.GladiatorWeaponTask;
import me.difficulty.features.day10.mobs.spiders.SpeedSpiderClimbTask;
import me.difficulty.features.day10.mobs.caballos.HorseEffectTask;
import me.difficulty.features.pets.PetBehaviorTask;

import java.util.Objects;

public final class Day10TaskRegistrar {

    private final DifficultyPlugin plugin;

    public Day10TaskRegistrar(DifficultyPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    // ========================================
    // TASKS DEL DIA 10 Y MASCOTAS
    // ========================================

    public void register(
            DayManager dayManager,
            CustomMobRegistry mobRegistry,
            Day10Bootstrap.Mobs mobs
    ) {

        // ========================================
        // GLADIADOR - CAMBIO DE ARMAS
        // ========================================

        new GladiatorWeaponTask(
                plugin,
                dayManager,
                mobRegistry,
                mobs.gladiator()
        ).runTaskTimer(
                plugin,
                0L,
                10L
        );

        // ========================================
        // ARANA VELOZ - ESCALADA
        // ========================================

        new SpeedSpiderClimbTask(
                dayManager,
                mobRegistry
        ).runTaskTimer(
                plugin,
                0L,
                2L
        );

        // ========================================
        // CABALLOS - EFECTOS
        // ========================================

        new HorseEffectTask(
                dayManager
        ).runTaskTimer(
                plugin,
                0L,
                10L
        );

        // ========================================
        // MASCOTAS - COMPORTAMIENTO
        // ========================================

        new PetBehaviorTask(
                dayManager
        ).runTaskTimer(
                plugin,
                0L,
                5L
        );
    }
}