
package me.difficulty.features.day20.worldgen;

import me.difficulty.core.day.DayManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.BlockPopulator;

public final class AncientDebrisWorldListener
        implements Listener {

    /*
     * ========================================
     * DIA 20 - REGISTRO DE GENERACION
     * ========================================
     */

    private final DayManager dayManager;

    public AncientDebrisWorldListener(
            DayManager dayManager
    ) {
        this.dayManager = dayManager;
    }

    /*
     * ========================================
     * MUNDOS YA CARGADOS
     * ========================================
     */

    public void registerLoadedWorlds() {

        for (World world : Bukkit.getWorlds()) {
            registerWorld(world);
        }
    }

    /*
     * ========================================
     * MUNDOS NUEVOS
     * ========================================
     */

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {

        registerWorld(event.getWorld());
    }

    /*
     * ========================================
     * REGISTRAR POPULATOR
     * ========================================
     */

    private void registerWorld(World world) {

        /*
         * Solo mundos del Nether.
         */
        if (world.getEnvironment()
                != World.Environment.NETHER) {
            return;
        }

        /*
         * Evitar registros duplicados.
         */
        for (BlockPopulator populator
                : world.getPopulators()) {

            if (populator instanceof AncientDebrisPopulator) {
                return;
            }
        }

        /*
         * El populator estara registrado
         * incluso antes del dia 20,
         * pero no actuara hasta ese dia.
         */
        world.getPopulators().add(
                new AncientDebrisPopulator(dayManager)
        );
    }
}