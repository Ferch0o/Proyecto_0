
package me.difficulty.features.storm.day7;

import me.difficulty.core.day.DayManager;
import me.difficulty.features.storm.core.StormManager;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;


public class StormNaturalTask extends BukkitRunnable {

    private final DayManager dayManager;
    private final StormManager stormManager;


    /*
     * ========================================
     * PROBABILIDAD
     * ========================================
     *
     * 1 entre 10.000 cada segundo
     * = 0,01%
     */

    private static final int CHANCE_DENOMINATOR =
            10_000;


    /*
     * ========================================
     * DURACIONES
     * ========================================
     */

    private static final long THIRTY_MINUTES =
            30L * 60L;

    private static final long ONE_HOUR =
            60L * 60L;


    public StormNaturalTask(
            DayManager dayManager,
            StormManager stormManager
    ) {

        this.dayManager = dayManager;
        this.stormManager = stormManager;
    }


    /*
     * ========================================
     * TIRADA CADA SEGUNDO
     * ========================================
     */

    @Override
    public void run() {

        /*
         * Solo desde el Día 7.
         */

        if (!dayManager.isAtLeast(7)) {
            return;
        }

        /*
         * IMPORTANTE:
         *
         * No revisamos stormManager.isActive().
         *
         * Una tormenta natural puede aparecer
         * mientras otra ya está activa.
         */

        if (ThreadLocalRandom.current()
                .nextInt(CHANCE_DENOMINATOR) != 0) {

            return;
        }

        /*
         * ========================================
         * DURACIÓN ALEATORIA
         * ========================================
         *
         * 20% = 30 minutos
         * 80% = 1 hora
         */

        int durationRoll =
                ThreadLocalRandom.current()
                        .nextInt(100);

        long addedSeconds =
                durationRoll < 20
                        ? THIRTY_MINUTES
                        : ONE_HOUR;

        /*
         * Acumular duración.
         */

        stormManager.addStormTime(
                addedSeconds,
                "Tormenta natural"
        );
    }
}
