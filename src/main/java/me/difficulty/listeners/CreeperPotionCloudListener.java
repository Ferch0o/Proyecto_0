package me.difficulty.listeners;

import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;

public class CreeperPotionCloudListener implements Listener {


    /*
     * ========================================
     * EVITAR NUBES DE POCIONES DE CREEPERS
     * ========================================
     *
     * Antes de que un Creeper explote,
     * eliminamos TODOS sus efectos de poción.
     *
     * De esta manera no importa si tiene:
     *
     * - Fuerza
     * - Resistencia
     * - Velocidad
     * - Veneno
     * - Invisibilidad
     * - efectos futuros
     * - etc.
     *
     * Nunca podrá dejar una nube persistente
     * al explotar.
     */

    @EventHandler(ignoreCancelled = true)
    public void onCreeperPrime(
            ExplosionPrimeEvent event
    ) {

        /*
         * Solo Creepers.
         *
         * El Creeper Futurista también entra,
         * porque sigue siendo una entidad
         * Creeper.
         */
        if (!(event.getEntity()
                instanceof Creeper creeper)) {

            return;
        }


        /*
         * ========================================
         * ELIMINAR TODOS LOS EFECTOS
         * ========================================
         *
         * Creamos una copia de la colección
         * antes de eliminar para evitar
         * modificarla mientras la recorremos.
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