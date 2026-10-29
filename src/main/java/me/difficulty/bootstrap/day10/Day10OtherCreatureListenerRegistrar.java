
package me.difficulty.bootstrap.day10;

import me.difficulty.DifficultyPlugin;
import me.difficulty.core.day.DayManager;

import me.difficulty.features.day10.mobs.golems.IronGolemListener;
import me.difficulty.features.day10.mobs.caballos.HorseSpawnListener;

import me.difficulty.features.pets.PetCombatListener;
import me.difficulty.features.pets.WolfControlListener;

import org.bukkit.plugin.PluginManager;
import java.util.Objects;

public final class Day10OtherCreatureListenerRegistrar {

    private final DifficultyPlugin plugin;

    public Day10OtherCreatureListenerRegistrar(DifficultyPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    public void register(DayManager dayManager) {

        PluginManager pm = plugin.getServer().getPluginManager();

        // GOLEMS DE HIERRO
        pm.registerEvents(
                new IronGolemListener(
                        dayManager
                ),
                plugin
        );

        // CABALLOS
        pm.registerEvents(
                new HorseSpawnListener(
                        dayManager
                ),
                plugin
        );

        // MASCOTAS - COMBATE
        pm.registerEvents(
                new PetCombatListener(
                        dayManager
                ),
                plugin
        );

        // LOBOS - CONTROL
        pm.registerEvents(
                new WolfControlListener(
                        dayManager
                ),
                plugin
        );
    }
}