package me.difficulty.features.storm.day10.listeners;

import me.difficulty.features.storm.core.StormManager;
import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.potion.PotionEffectType;

public class StormCreeperListener implements Listener {

    private final StormManager stormManager;


    public StormCreeperListener(
            StormManager stormManager
    ) {

        this.stormManager =
                stormManager;
    }


    /*
     * ========================================
     * EVITAR NUBE DE POCIONES DEL CREEPER
     * ========================================
     */

    @EventHandler(ignoreCancelled = true)
    public void onCreeperPrime(
            ExplosionPrimeEvent event
    ) {

        /*
         * Solo durante nuestra Tormenta.
         */
        if (!stormManager.hasDay10Effects()) {
            return;
        }


        /*
         * Solo Creepers.
         *
         * Esto también incluye nuestro
         * Creeper Futurista, porque sigue
         * siendo una entidad Creeper.
         */
        if (!(event.getEntity()
                instanceof Creeper creeper)) {

            return;
        }


        /*
         * ========================================
         * QUITAR EFECTOS ANTES DE EXPLOTAR
         * ========================================
         *
         * Si el Creeper explotara teniendo
         * estos efectos, Minecraft podría
         * generar una nube persistente.
         */

        creeper.removePotionEffect(
                PotionEffectType.STRENGTH
        );


        creeper.removePotionEffect(
                PotionEffectType.RESISTANCE
        );
    }
}