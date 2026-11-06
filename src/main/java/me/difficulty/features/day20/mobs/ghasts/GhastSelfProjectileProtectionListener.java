
package me.difficulty.features.day20.mobs.ghasts;

import org.bukkit.damage.DamageSource;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LargeFireball;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public final class GhastSelfProjectileProtectionListener
        implements Listener {

    /*
     * ========================================
     * INMUNIDAD A PROYECTILES PROPIOS
     * ========================================
     *
     * Afecta a todos los Ghasts:
     *
     * - Ghast vanilla.
     * - Ghast Corrupto.
     * - Ghast Demoníaco.
     *
     * Solo bloquea daño provocado
     * por sus propios proyectiles.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onGhastDamage(
            EntityDamageEvent event
    ) {

        /*
         * ========================================
         * SOLO GHASTS
         * ========================================
         */
        if (!(event.getEntity() instanceof Ghast ghast)) {
            return;
        }

        /*
         * ========================================
         * IDENTIFICAR ORIGEN DEL DAÑO
         * ========================================
         */
        DamageSource source = event.getDamageSource();

        Entity direct = source.getDirectEntity();

        /*
         * ========================================
         * DAÑO DIRECTO DE BOLA DE FUEGO
         * ========================================
         */
        if (direct instanceof LargeFireball fireball) {

            if (isOwnProjectile(ghast, fireball)) {
                event.setCancelled(true);
            }

            return;
        }

        /*
         * ========================================
         * RESPALDO: ENTIDAD ATACANTE
         * ========================================
         */
        if (event instanceof EntityDamageByEntityEvent damageEvent
                && damageEvent.getDamager()
                instanceof LargeFireball fireball) {

            if (isOwnProjectile(ghast, fireball)) {
                event.setCancelled(true);
            }

            return;
        }

        /*
         * ========================================
         * RESPALDO: DAÑO ATRIBUIDO AL GHAST
         * ========================================
         *
         * En algunos eventos de explosión,
         * Paper puede identificar al tirador
         * como entidad causante del daño.
         */
        boolean projectileDamage =
                event.getCause()
                        == EntityDamageEvent.DamageCause.PROJECTILE;

        boolean explosionDamage =
                event.getCause()
                        == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION;

        if (!projectileDamage && !explosionDamage) {
            return;
        }

        Entity causing = source.getCausingEntity();

        if (causing instanceof Ghast shooter
                && shooter.getUniqueId().equals(
                ghast.getUniqueId()
        )) {

            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * COMPROBAR PROYECTIL PROPIO
     * ========================================
     */
    private boolean isOwnProjectile(
            Ghast ghast,
            LargeFireball fireball
    ) {

        if (!(fireball.getShooter()
                instanceof Ghast shooter)) {
            return false;
        }

        /*
         * Comparar UUID para comprobar
         * que el proyectil pertenece
         * exactamente al mismo Ghast.
         */
        return shooter.getUniqueId().equals(
                ghast.getUniqueId()
        );
    }
}