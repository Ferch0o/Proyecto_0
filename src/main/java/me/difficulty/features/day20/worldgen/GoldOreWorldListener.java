
package me.difficulty.features.day20.worldgen;

import me.difficulty.core.day.DayManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.BlockPopulator;

public final class GoldOreWorldListener
        implements Listener {

    /*
     * ========================================
     * DIA 20 - REGISTRO DE GENERACION DE ORO
     * ========================================
     */

    private final DayManager dayManager;

    public GoldOreWorldListener(
            DayManager dayManager
    ) {
        this.dayManager = dayManager;
    }

    /*
     * ========================================
     * REGISTRAR MUNDOS YA CARGADOS
     * ========================================
     */

    public void registerLoadedWorlds() {

        for (World world : Bukkit.getWorlds()) {
            registerWorld(world);
        }
    }

    /*
     * ========================================
     * REGISTRAR MUNDOS NUEVOS
     * ========================================
     */

    @EventHandler
    public void onWorldInit(
            WorldInitEvent event
    ) {

        registerWorld(event.getWorld());
    }

    /*
     * ========================================
     * REGISTRAR GENERADOR
     * ========================================
     */

    private void registerWorld(
            World world
    ) {

        World.Environment environment =
                world.getEnvironment();

        /*
         * Solo registrar en:
         * - Overworld
         * - Nether
         */
        if (environment != World.Environment.NORMAL
                && environment != World.Environment.NETHER) {
            return;
        }

        /*
         * Evitar registros duplicados.
         */
        for (BlockPopulator populator
                : world.getPopulators()) {

            if (populator instanceof GoldOrePopulator) {
                return;
            }
        }

        /*
         * Registrar el sistema.
         *
         * Se registra desde el inicio,
         * pero solo actua desde el dia 20.
         */
        world.getPopulators().add(
                new GoldOrePopulator(dayManager)
        );
    }
}