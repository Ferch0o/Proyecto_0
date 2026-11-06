
package me.difficulty.features.day20.mobs.blazes;

import org.bukkit.Location;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class BlazeShieldUtil {

    private BlazeShieldUtil() {
    }

    /*
     * ========================================
     * BLOQUEAR ATAQUE CUERPO A CUERPO
     * ========================================
     */
    public static boolean blocksMelee(
            Player player,
            Blaze blaze
    ) {

        if (!player.isBlocking()) {
            return false;
        }

        /*
         * Dirección desde el jugador
         * hacia el Blaze atacante.
         */
        Vector sourceDirection =
                blaze.getEyeLocation()
                        .toVector()
                        .subtract(
                                player.getEyeLocation()
                                        .toVector()
                        );

        return isFacingAttack(
                player,
                sourceDirection
        );
    }

    /*
     * ========================================
     * BLOQUEAR PROYECTIL DE BLAZE
     * ========================================
     */
    public static boolean blocksProjectile(
            Player player,
            Fireball fireball,
            Blaze blaze
    ) {

        if (!player.isBlocking()) {
            return false;
        }

        /*
         * Obtener la trayectoria real
         * de la bola de fuego.
         */
        Vector incoming =
                fireball.getVelocity().clone();

        /*
         * Si no hay velocidad suficiente,
         * utilizar la aceleración.
         */
        if (incoming.lengthSquared() < 0.0001) {

            incoming =
                    fireball.getAcceleration().clone();
        }

        /*
         * Invertimos la dirección
         * para apuntar hacia el origen
         * del ataque.
         */
        if (incoming.lengthSquared() >= 0.0001) {

            incoming.multiply(-1.0);

            return isFacingAttack(
                    player,
                    incoming
            );
        }

        /*
         * Respaldo:
         * utilizar la ubicación del Blaze.
         */
        return blocksMelee(player, blaze);
    }

    /*
     * ========================================
     * COMPROBAR DIRECCIÓN DEL ESCUDO
     * ========================================
     */
    private static boolean isFacingAttack(
            Player player,
            Vector sourceDirection
    ) {

        Vector facing =
                player.getEyeLocation()
                        .getDirection();

        /*
         * El escudo se evalúa
         * horizontalmente.
         */
        facing.setY(0.0);
        sourceDirection.setY(0.0);

        if (facing.lengthSquared() < 0.0001
                || sourceDirection.lengthSquared() < 0.0001) {

            return false;
        }

        facing.normalize();
        sourceDirection.normalize();

        /*
         * Producto punto:
         *
         * Positivo: ataque desde delante.
         * Negativo: ataque desde detrás.
         */
        return facing.dot(sourceDirection) > 0.0;
    }
}