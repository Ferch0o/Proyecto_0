package me.difficulty.features.day10.items.minotaurhorn;

import me.difficulty.core.day.DayManager;

import org.bukkit.entity.Ravager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import org.bukkit.inventory.ItemStack;


public class MinotaurHornDropListener
        implements Listener {


    private final MinotaurHorn minotaurHorn;

    private final DayManager dayManager;


    public MinotaurHornDropListener(
            MinotaurHorn minotaurHorn,
            DayManager dayManager
    ) {

        this.minotaurHorn =
                minotaurHorn;

        this.dayManager =
                dayManager;
    }


    /*
     * ========================================
     * DROP DEL CUERNO DE MINOTAURO
     * ========================================
     */

    @EventHandler
    public void onRavagerDeath(
            EntityDeathEvent event
    ) {


        /*
         * ========================================
         * SOLO DESDE EL DÍA 10
         * ========================================
         */

        if (!dayManager.isAtLeast(10)) {
            return;
        }


        /*
         * ========================================
         * SOLO RAVAGER
         * ========================================
         */

        if (!(event.getEntity()
                instanceof Ravager)) {

            return;
        }


        /*
         * ========================================
         * 100% DE PROBABILIDAD
         * ========================================
         *
         * No hacemos ninguna tirada.
         *
         * Siempre suelta exactamente:
         *
         * 1 Cuerno de Minotauro
         */

        ItemStack drop =
                minotaurHorn.createItem();


        drop.setAmount(
                1
        );


        /*
         * Conservamos los drops
         * normales del Ravager.
         */

        event.getDrops()
                .add(
                        drop
                );
    }
}