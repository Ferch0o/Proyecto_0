
package me.difficulty.features.day20.worldgen;

import me.difficulty.core.day.DayManager;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.concurrent.ThreadLocalRandom;

public final class AncientDebrisDropListener
        implements Listener {

    /*
     * ========================================
     * DIA 20 - DROPS DE ANCIENT DEBRIS
     * ========================================
     */

    private static final int REQUIRED_DAY = 20;

    // 80% de no obtener nada.
    private static final int NO_DROP_CHANCE = 80;

    private final DayManager dayManager;

    public AncientDebrisDropListener(
            DayManager dayManager
    ) {
        this.dayManager = dayManager;
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onAncientDebrisBreak(
            BlockBreakEvent event
    ) {

        /*
         * Antes del dia 20:
         * comportamiento vanilla.
         */
        if (!dayManager.isAtLeast(REQUIRED_DAY)) {
            return;
        }

        /*
         * Solo Ancient Debris.
         */
        if (event.getBlock().getType()
                != Material.ANCIENT_DEBRIS) {
            return;
        }

        /*
         * Sorteo independiente por bloque.
         *
         * 0-79: no suelta nada.
         * 80-99: drops normales.
         */
        boolean noDrop =
                ThreadLocalRandom.current()
                        .nextInt(100) < NO_DROP_CHANCE;

        if (!noDrop) {
            return;
        }

        /*
         * El bloque se rompe normalmente,
         * pero no genera objetos.
         *
         * Tambien funciona con Toque de Seda.
         */
        event.setDropItems(false);
        event.setExpToDrop(0);
    }
}