
package me.difficulty.features.day20.mobs.ghasts;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import org.bukkit.potion.PotionEffectType;

public final class CorruptedGhastCombatListener
        implements Listener {

    private static final float RADIUS_MULTIPLIER = 9.0f;

    private final DayManager dayManager;
    private final CustomMobRegistry registry;

    public CorruptedGhastCombatListener(
            DayManager dayManager,
            CustomMobRegistry registry
    ) {
        this.dayManager = dayManager;
        this.registry = registry;
    }

    /*
     * ========================================
     * EXPLOSIÓN DEL GHAST CORRUPTO
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onExplosionPrime(
            ExplosionPrimeEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        /*
         * SOLO BOLAS GRANDES DE FUEGO
         */
        if (!(event.getEntity()
                instanceof LargeFireball fireball)) {
            return;
        }

        /*
         * SOLO GHAST CORRUPTO
         */
        if (!isCorruptedGhastFireball(fireball)) {
            return;
        }

        /*
         * ========================================
         * POTENCIA DE EXPLOSIÓN x8
         * ========================================
         *
         * Minecraft calcula el daño
         * y el alcance de manera natural.
         *
         * No creamos explosiones adicionales.
         */
        event.setRadius(
                event.getRadius() * RADIUS_MULTIPLIER
        );
    }

    /*
     * ========================================
     * ELIMINAR FUERZA AL IMPACTAR
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onDamage(
            EntityDamageByEntityEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        /*
         * IDENTIFICAR PROYECTIL
         */
        Entity direct = event.getDamageSource()
                .getDirectEntity();

        LargeFireball fireball;

        if (direct instanceof LargeFireball source) {
            fireball = source;

        } else if (event.getDamager()
                instanceof LargeFireball source) {
            fireball = source;

        } else {
            return;
        }

        /*
         * SOLO PROYECTIL DEL GHAST CORRUPTO
         */
        if (!isCorruptedGhastFireball(fireball)) {
            return;
        }

        /*
         * ========================================
         * ELIMINAR FUERZA
         * ========================================
         *
         * Funciona aunque el jugador
         * tenga corazones de Absorción.
         */
        if (event.getEntity() instanceof Player player
                && event.getDamage() > 0.0) {

            player.removePotionEffect(
                    PotionEffectType.STRENGTH
            );
        }
    }

    /*
     * ========================================
     * IDENTIFICAR GHAST CORRUPTO
     * ========================================
     */
    private boolean isCorruptedGhastFireball(
            LargeFireball fireball
    ) {

        if (!(fireball.getShooter()
                instanceof Ghast ghast)) {
            return false;
        }

        return registry.is(
                ghast,
                MobIds.CORRUPTED_GHAST
        );
    }
}