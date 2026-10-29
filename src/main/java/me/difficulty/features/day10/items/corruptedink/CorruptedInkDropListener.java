package me.difficulty.features.day10.items.corruptedink;

import me.difficulty.core.day.DayManager;

import org.bukkit.entity.GlowSquid;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;


public class CorruptedInkDropListener
        implements Listener {


    private final CorruptedInk corruptedInk;

    private final DayManager dayManager;


    /*
     * ========================================
     * PROBABILIDAD
     * ========================================
     *
     * 80 = 80%
     */

    private static final int DROP_CHANCE =
            80;


    /*
     * ========================================
     * CANTIDAD
     * ========================================
     */

    private static final int MIN_AMOUNT =
            2;

    private static final int MAX_AMOUNT =
            4;


    public CorruptedInkDropListener(
            CorruptedInk corruptedInk,
            DayManager dayManager
    ) {

        this.corruptedInk =
                corruptedInk;

        this.dayManager =
                dayManager;
    }


    /*
     * ========================================
     * DROP DE TINTA CORRUPTA
     * ========================================
     */

    @EventHandler
    public void onGlowSquidDeath(
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
         * SOLO GLOW SQUID
         * ========================================
         */

        if (!(event.getEntity()
                instanceof GlowSquid)) {

            return;
        }


        /*
         * ========================================
         * 80%
         * ========================================
         */

        int roll =
                ThreadLocalRandom
                        .current()
                        .nextInt(100);


        if (roll >= DROP_CHANCE) {
            return;
        }


        /*
         * ========================================
         * 2 - 4 UNIDADES
         * ========================================
         */

        int amount =
                ThreadLocalRandom
                        .current()
                        .nextInt(
                                MIN_AMOUNT,
                                MAX_AMOUNT + 1
                        );


        /*
         * Crear nuestro item custom.
         */
        ItemStack drop =
                corruptedInk.createItem();


        /*
         * Cambiar cantidad:
         *
         * 2
         * 3
         * o
         * 4
         */
        drop.setAmount(
                amount
        );


        /*
         * Añadirlo sin eliminar
         * los drops vanilla.
         */
        event.getDrops()
                .add(
                        drop
                );
    }
}