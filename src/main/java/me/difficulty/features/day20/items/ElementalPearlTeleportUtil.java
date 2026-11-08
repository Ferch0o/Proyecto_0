
package me.difficulty.features.day20.items;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EnderPearl;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.persistence.PersistentDataType;

public final class ElementalPearlTeleportUtil {

    /*
     * Distancia máxima entre la perla
     * y el destino del teletransporte.
     *
     * Se compara la distancia al cuadrado.
     */
    private static final double MAX_DISTANCE_SQUARED = 16.0;

    private ElementalPearlTeleportUtil() {
    }

    public static boolean isElementalTeleport(
            PlayerTeleportEvent event,
            NamespacedKey projectileKey
    ) {

        if (event.getCause()
                != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return false;
        }

        Location destination = event.getTo();

        if (destination == null) {
            return false;
        }

        /*
         * Buscar la perla asociada al jugador
         * que esté más cerca del destino.
         *
         * Esto evita decidir en función
         * del objeto que ahora tiene
         * el jugador en la mano.
         */
        EnderPearl closest = null;
        double closestDistance = MAX_DISTANCE_SQUARED;

        for (EnderPearl pearl :
                event.getPlayer().getEnderPearls()) {

            if (!pearl.getWorld().equals(
                    destination.getWorld()
            )) {
                continue;
            }

            double distance = pearl
                    .getLocation()
                    .distanceSquared(destination);

            if (distance < closestDistance) {

                closest = pearl;
                closestDistance = distance;
            }
        }

        /*
         * Solo aceptar la marca que se
         * asignó al lanzar nuestra perla.
         */
        return closest != null
                && closest.getPersistentDataContainer().has(
                projectileKey,
                PersistentDataType.BYTE
        );
    }
}