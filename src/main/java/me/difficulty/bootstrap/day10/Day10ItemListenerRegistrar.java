
package me.difficulty.bootstrap.day10;

import me.difficulty.DifficultyPlugin;
import me.difficulty.bootstrap.Day10Bootstrap;
import me.difficulty.core.day.DayManager;

import me.difficulty.features.day10.items.Day10MaterialProtectionListener;

import me.difficulty.features.day10.items.corruptedink.CorruptedInkDropListener;
import me.difficulty.features.day10.items.minotaurhorn.MinotaurHornDropListener;

import me.difficulty.features.day10.items.explosivecrossbow.ExplosiveCrossbowRecipe;
import me.difficulty.features.day10.items.explosivecrossbow.ExplosiveCrossbowRecipeListener;
import me.difficulty.features.day10.items.explosivecrossbow.ExplosiveCrossbowRecipeBookListener;
import me.difficulty.features.day10.items.explosivecrossbow.ExplosiveCrossbowShootListener;

import org.bukkit.plugin.PluginManager;
import java.util.Objects;

public final class Day10ItemListenerRegistrar {

    private final DifficultyPlugin plugin;

    public Day10ItemListenerRegistrar(DifficultyPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    public void register(
            DayManager dayManager,
            Day10Bootstrap.Items items,
            ExplosiveCrossbowRecipe explosiveCrossbowRecipe
    ) {

        PluginManager pm = plugin.getServer().getPluginManager();

        // ========================================
        // TINTA CORRUPTA
        // ========================================

        pm.registerEvents(
                new CorruptedInkDropListener(
                        items.corruptedInk(),
                        dayManager
                ),
                plugin
        );

        // ========================================
        // CUERNO DE MINOTAURO
        // ========================================

        pm.registerEvents(
                new MinotaurHornDropListener(
                        items.minotaurHorn(),
                        dayManager
                ),
                plugin
        );

        // ========================================
        // PROTECCION DE MATERIALES
        // ========================================

        pm.registerEvents(
                new Day10MaterialProtectionListener(
                        explosiveCrossbowRecipe.getRecipeKey(),
                        items.corruptedInk(),
                        items.minotaurHorn()
                ),
                plugin
        );

        // ========================================
        // BALLESTA EXPLOSIVA - CRAFTEO
        // ========================================

        pm.registerEvents(
                new ExplosiveCrossbowRecipeListener(
                        plugin,
                        explosiveCrossbowRecipe.getRecipeKey(),
                        items.corruptedInk(),
                        items.minotaurHorn(),
                        items.explosiveCrossbow(),
                        dayManager
                ),
                plugin
        );

        // ========================================
        // BALLESTA EXPLOSIVA - LIBRO DE RECETAS
        // ========================================

        pm.registerEvents(
                new ExplosiveCrossbowRecipeBookListener(
                        plugin,
                        explosiveCrossbowRecipe.getRecipeKey(),
                        items.corruptedInk(),
                        items.minotaurHorn(),
                        items.explosiveCrossbow(),
                        dayManager
                ),
                plugin
        );

        // ========================================
        // BALLESTA EXPLOSIVA - DISPAROS
        // ========================================

        pm.registerEvents(
                new ExplosiveCrossbowShootListener(
                        plugin,
                        items.explosiveCrossbow(),
                        dayManager
                ),
                plugin
        );
    }
}