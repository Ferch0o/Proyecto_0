package me.difficulty.features.day10.mobs.caballos;

import me.difficulty.day.DayManager;
import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class HorseSpawnListener implements Listener {

    private final DayManager dayManager;


    public HorseSpawnListener(
            DayManager dayManager
    ) {

        this.dayManager = dayManager;
    }


    /*
     * ========================================
     * 5% → VELOCIDAD III
     * ========================================
     */

    @EventHandler
    public void onHorseSpawn(
            CreatureSpawnEvent event
    ) {

        /*
         * Solo desde el Día 10.
         */
        if (!dayManager.isAtLeast(10)) {
            return;
        }


        /*
         * Solo caballos normales.
         *
         * No afecta:
         * - burros
         * - mulas
         * - caballos esqueleto
         * - caballos zombie
         */
        if (!(event.getEntity()
                instanceof Horse horse)) {

            return;
        }


        /*
         * ========================================
         * 5% DE PROBABILIDAD
         * ========================================
         */

        if (ThreadLocalRandom
                .current()
                .nextInt(100) < 5) {

            horse.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.SPEED,
                            PotionEffect.INFINITE_DURATION,
                            2,
                            false,
                            true
                    )
            );
        }
    }
}