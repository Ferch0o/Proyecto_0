package me.difficulty.features.storm.day10.tasks;

import me.difficulty.features.storm.core.StormManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;


public class StormPlayerEffectTask
        extends BukkitRunnable {


    private final StormManager stormManager;


    /*
     * ========================================
     * DURACIÓN DEL EFECTO
     * ========================================
     *
     * 10 segundos
     *
     * 20 ticks = 1 segundo
     * 200 ticks = 10 segundos
     */
    private static final int EFFECT_DURATION =
            20 * 10;


    public StormPlayerEffectTask(
            StormManager stormManager
    ) {

        this.stormManager =
                stormManager;
    }


    /*
     * ========================================
     * EJECUCIÓN
     * ========================================
     *
     * Esta tarea se ejecutará
     * cada 30 segundos.
     */

    @Override
    public void run() {


        /*
         * Solo funciona durante
         * nuestra Tormenta.
         */
        if (!stormManager.hasDay10Effects()) {
            return;
        }


        /*
         * Cada jugador hace
         * su propia tirada.
         */
        for (Player player :
                Bukkit.getOnlinePlayers()) {


            applyRandomStormEffect(
                    player
            );
        }
    }


    /*
     * ========================================
     * EFECTO ALEATORIO
     * ========================================
     */

    private void applyRandomStormEffect(
            Player player
    ) {


        /*
         * Tirada:
         *
         * 0  = Wither I
         * 1  = Lentitud I
         * 2  = Ceguera I
         * 3  = Debilidad I
         * 4  = Veneno I
         * 5  = Hambre I
         *
         * 6 - 99
         * = ningún efecto
         *
         * Por lo tanto:
         *
         * cada efecto = 1%
         * total de recibir algo = 6%
         */
        int roll =
                ThreadLocalRandom
                        .current()
                        .nextInt(100);


        PotionEffectType effectType;


        switch (roll) {

            case 0 ->
                    effectType =
                            PotionEffectType.WITHER;


            case 1 ->
                    effectType =
                            PotionEffectType.SLOWNESS;


            case 2 ->
                    effectType =
                            PotionEffectType.BLINDNESS;


            case 3 ->
                    effectType =
                            PotionEffectType.WEAKNESS;


            case 4 ->
                    effectType =
                            PotionEffectType.POISON;


            case 5 ->
                    effectType =
                            PotionEffectType.HUNGER;


            /*
             * 94%:
             *
             * no ocurre nada.
             */
            default -> {
                return;
            }
        }


        /*
         * ========================================
         * APLICAR EFECTO
         * ========================================
         *
         * amplifier = 0
         *
         * significa nivel I.
         */

        player.addPotionEffect(
                new PotionEffect(
                        effectType,
                        EFFECT_DURATION,
                        0,
                        false,
                        true,
                        true
                )
        );
    }
}