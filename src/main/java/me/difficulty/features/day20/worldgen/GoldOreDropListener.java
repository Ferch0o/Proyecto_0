
package me.difficulty.features.day20.worldgen;

import me.difficulty.core.day.DayManager;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public final class GoldOreDropListener
        implements Listener {

    /*
     * ========================================
     * DIA 20 - DROPS DEL MINERAL DE ORO
     * ========================================
     *
     * Desde el dia 20:
     *
     * - GOLD_ORE: 0 drops.
     * - DEEPSLATE_GOLD_ORE: 0 drops.
     * - NETHER_GOLD_ORE: 0 drops.
     *
     * Probabilidad de no obtener nada: 100%.
     */

    private static final int REQUIRED_DAY = 20;

    private final DayManager dayManager;

    public GoldOreDropListener(
            DayManager dayManager
    ) {
        this.dayManager = dayManager;
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onGoldOreBreak(
            BlockBreakEvent event
    ) {

        /*
         * Antes del dia 20:
         * comportamiento vanilla.
         */
        if (!dayManager.isAtLeast(REQUIRED_DAY)) {
            return;
        }

        Material material =
                event.getBlock().getType();

        /*
         * ========================================
         * IDENTIFICAR MINERALES DE ORO
         * ========================================
         */
        boolean isGoldOre =
                material == Material.GOLD_ORE
                        || material == Material.DEEPSLATE_GOLD_ORE
                        || material == Material.NETHER_GOLD_ORE;

        if (!isGoldOre) {
            return;
        }

        /*
         * ========================================
         * ELIMINAR DROPS
         * ========================================
         *
         * El bloque se rompe normalmente,
         * pero no entrega ningun objeto.
         *
         * Tambien bloquea:
         * - Fortuna
         * - Toque de Seda
         *
         * No entrega experiencia.
         */
        event.setDropItems(false);
        event.setExpToDrop(0);
    }
}