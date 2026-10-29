
package me.difficulty.bootstrap.storm;

import me.difficulty.DifficultyPlugin;
import me.difficulty.core.day.DayManager;

import me.difficulty.features.day10.mobs.creepers.CreeperPotionCloudListener;

import me.difficulty.features.storm.core.StormManager;
import me.difficulty.features.storm.core.StormTimerTask;
import me.difficulty.features.storm.core.commands.StormCommand;
import me.difficulty.features.storm.core.listeners.StormBossBarListener;
import me.difficulty.features.storm.core.listeners.StormDeathListener;

import me.difficulty.features.storm.day7.StormNaturalTask;

import me.difficulty.features.storm.day10.listeners.*;
import me.difficulty.features.storm.day10.tasks.*;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;

import java.util.Objects;

public final class StormBootstrap {

    private final DifficultyPlugin plugin;
    private final DayManager dayManager;
    private final StormManager stormManager;

    private StormMobCapTask stormMobCapTask;

    // ========================================
    // CONSTRUCTOR - SISTEMA DE TORMENTA
    // ========================================

    public StormBootstrap(
            DifficultyPlugin plugin,
            DayManager dayManager
    ) {
        this.plugin = Objects.requireNonNull(plugin);
        this.dayManager = Objects.requireNonNull(dayManager);

        this.stormManager = new StormManager(
                plugin,
                dayManager
        );
    }

    // ========================================
    // BOSSBAR Y RECONEXION
    // ========================================

    public void registerBossBarListener() {

        PluginManager pm =
                plugin.getServer().getPluginManager();

        pm.registerEvents(
                new StormBossBarListener(
                        plugin,
                        stormManager
                ),
                plugin
        );
    }

    // ========================================
    // MUERTE DE JUGADORES
    // ========================================

    public void registerDeathListener() {

        PluginManager pm =
                plugin.getServer().getPluginManager();

        pm.registerEvents(
                new StormDeathListener(
                        dayManager,
                        stormManager
                ),
                plugin
        );
    }

    // ========================================
    // LISTENERS ESPECIALES - DIA 10
    // ========================================

    public void registerDay10Listeners() {

        PluginManager pm =
                plugin.getServer().getPluginManager();

        // CREEPERS DURANTE TORMENTA
        pm.registerEvents(
                new StormCreeperListener(
                        stormManager
                ),
                plugin
        );

        // NUBES DE POCIONES DE CREEPERS
        pm.registerEvents(
                new CreeperPotionCloudListener(
                        stormManager
                ),
                plugin
        );

        // DROPS DE EVOKERS
        pm.registerEvents(
                new StormEvokerDropListener(
                        stormManager
                ),
                plugin
        );

        // ENDER PEARLS
        pm.registerEvents(
                new StormEnderPearlListener(
                        stormManager
                ),
                plugin
        );

        // DIAMANTES
        pm.registerEvents(
                new StormDiamondDropListener(
                        plugin,
                        stormManager
                ),
                plugin
        );
    }

    // ========================================
    // TAREAS PROGRAMADAS DE TORMENTA
    // ========================================

    public void registerTasks() {

        // CONTROL GENERAL DE DURACION
        new StormTimerTask(
                stormManager
        ).runTaskTimer(
                plugin,
                20L,
                20L
        );

        // TORMENTAS NATURALES DESDE DIA 7
        new StormNaturalTask(
                dayManager,
                stormManager
        ).runTaskTimer(
                plugin,
                20L,
                20L
        );

        // EFECTOS SOBRE MOBS
        new StormMobEffectTask(
                plugin,
                stormManager
        ).runTaskTimer(
                plugin,
                0L,
                5L
        );

        // CONTROL DE LIMITE DE MOBS
        stormMobCapTask =
                new StormMobCapTask(
                        stormManager
                );

        stormMobCapTask.runTaskTimer(
                plugin,
                20L,
                20L
        );

        // GENERACION EXTRA DE ENEMIGOS
        new StormExtraSpawnTask(
                plugin,
                stormManager
        ).runTaskTimer(
                plugin,
                20L,
                20L
        );

        // EFECTOS SOBRE JUGADORES
        new StormPlayerEffectTask(
                stormManager
        ).runTaskTimer(
                plugin,
                200L,
                200L
        );
    }

    // ========================================
    // COMANDO /STORM
    // ========================================

    public void registerCommand() {

        StormCommand stormCommand =
                new StormCommand(stormManager);

        PluginCommand command = Objects.requireNonNull(
                plugin.getCommand("storm"),
                "El comando /storm no esta registrado en plugin.yml"
        );

        command.setExecutor(stormCommand);
        command.setTabCompleter(stormCommand);
    }

    // ========================================
    // APAGADO Y PERSISTENCIA
    // ========================================

    public void shutdown() {

        // Restaurar el control especial de mobs
        if (stormMobCapTask != null) {
            stormMobCapTask.shutdown();
        }

        // Guardar duracion acumulada y estado
        stormManager.saveState();
    }

    // ========================================
    // ACCESO AL MANAGER
    // ========================================

    public StormManager getStormManager() {
        return stormManager;
    }
}