
package me.difficulty.listeners;

import me.difficulty.storm.StormManager;

import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;

public class CreeperPotionCloudListener
        implements Listener {

    private final StormManager stormManager;

    public CreeperPotionCloudListener(
            StormManager stormManager
    ) {
        this.stormManager = stormManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreeperPrime(
            ExplosionPrimeEvent event
    ) {

        /*
         * SOLO TORMENTAS DEL DÍA 10+
         */
        if (!stormManager.hasDay10Effects()) {
            return;
        }

        if (!(event.getEntity()
                instanceof Creeper creeper)) {
            return;
        }

        /*
         * Evitar nubes de efectos
         * provocadas por creepers.
         */
        for (PotionEffect effect :
                new ArrayList<>(
                        creeper.getActivePotionEffects()
                )) {

            creeper.removePotionEffect(
                    effect.getType()
            );
        }
    }
}
