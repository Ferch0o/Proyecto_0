package me.difficulty.features.storm.day10.listeners;

import me.difficulty.features.storm.core.StormManager;

import org.bukkit.Material;
import org.bukkit.entity.Evoker;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class StormEvokerDropListener
        implements Listener {

    private final StormManager stormManager;


    public StormEvokerDropListener(
            StormManager stormManager
    ) {

        this.stormManager =
                stormManager;
    }


    /*
     * ========================================
     * EVOKERS SIN TOTEM DURANTE TORMENTA
     * ========================================
     */

    @EventHandler
    public void onEvokerDeath(
            EntityDeathEvent event
    ) {

        /*
         * Solo durante nuestra Tormenta.
         */
        if (!stormManager.hasDay10Effects()) {
            return;
        }


        /*
         * Solo Evokers.
         */
        if (!(event.getEntity()
                instanceof Evoker)) {

            return;
        }


        /*
         * ========================================
         * ELIMINAR TOTEMS
         * ========================================
         *
         * No limpiamos todos los drops.
         *
         * Únicamente quitamos cualquier
         * Totem of Undying.
         */

        event.getDrops()
                .removeIf(
                        item ->
                                item.getType()
                                        == Material.TOTEM_OF_UNDYING
                );
    }
}