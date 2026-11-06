
package me.difficulty.features.day20.mobs.blazes;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;

import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import org.bukkit.potion.PotionEffectType;

public final class BlazeCombatListener implements Listener {

    private final DayManager dayManager;
    private final CustomMobRegistry mobRegistry;

    /*
     * ========================================
     * CONFIGURACIÓN
     * ========================================
     *
     * Blaze Corrupto:
     *
     * Daño x4.
     */
    private static final double
            CORRUPTED_DAMAGE_MULTIPLIER = 5.0;

    /*
     * ========================================
     * CONSTRUCTOR
     * ========================================
     */
    public BlazeCombatListener(
            DayManager dayManager,
            CustomMobRegistry mobRegistry
    ) {

        this.dayManager = dayManager;
        this.mobRegistry = mobRegistry;
    }

    /*
     * ========================================
     * IMPACTO DE BOLA DE FUEGO
     * ========================================
     *
     * Esta comprobación funciona
     * independientemente de que el
     * proyectil produzca daño.
     *
     * Es importante cuando el jugador
     * tiene Resistencia al Fuego.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onBlazeFireballHit(
            ProjectileHitEvent event
    ) {

        /*
         * Solo desde el día 20.
         */
        if (!dayManager.isAtLeast(20)) {
            return;
        }

        /*
         * Solo bolas de fuego.
         */
        if (!(event.getEntity()
                instanceof Fireball fireball)) {
            return;
        }

        /*
         * Debe impactar directamente
         * contra un jugador.
         */
        if (!(event.getHitEntity()
                instanceof Player player)) {
            return;
        }

        /*
         * Comprobar quién lanzó
         * el proyectil.
         */
        if (!(fireball.getShooter()
                instanceof Blaze blaze)) {
            return;
        }

        /*
         * Solo Blaze Corrupto.
         */
        if (!mobRegistry.is(
                blaze,
                MobIds.CORRUPTED_BLAZE
        )) {
            return;
        }

        if (BlazeShieldUtil.blocksProjectile(
                player,
                fireball,
                blaze
        )) {
            return;
        }

        /*
         * ========================================
         * ELIMINAR RESISTENCIA AL FUEGO
         * ========================================
         *
         * Se ejecuta aunque el jugador
         * fuera inmune al daño del fuego.
         */
        removeFireResistance(player);
    }

    /*
     * ========================================
     * COMBATE DE BLAZES
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onBlazeCombat(
            EntityDamageByEntityEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        /*
         * ========================================
         * REY BLAZE: INMUNIDAD A FLECHAS
         * ========================================
         */
        if (event.getEntity()
                instanceof Blaze blaze) {

            if (mobRegistry.is(
                    blaze,
                    MobIds.BLAZE_KING
            )) {

                Entity damager =
                        event.getDamager();

                /*
                 * Flechas normales,
                 * espectrales y con efectos.
                 *
                 * Los tridentes no se bloquean.
                 */
                if (damager instanceof AbstractArrow
                        && !(damager instanceof Trident)) {

                    event.setCancelled(true);
                    return;
                }
            }
        }

        /*
         * ========================================
         * IDENTIFICAR ATACANTE
         * ========================================
         */
        Blaze attacker = null;

        Entity damager = event.getDamager();

        /*
         * Ataque cuerpo a cuerpo.
         */
        if (damager instanceof Blaze blaze) {

            attacker = blaze;

            /*
             * Ataque con proyectil.
             */
        } else if (damager instanceof Projectile projectile) {

            if (projectile.getShooter()
                    instanceof Blaze blaze) {

                attacker = blaze;
            }
        }

        /*
         * El ataque no proviene
         * de un Blaze.
         */
        if (attacker == null) {
            return;
        }

        /*
         * ========================================
         * SOLO BLAZE CORRUPTO
         * ========================================
         */
        if (!mobRegistry.is(
                attacker,
                MobIds.CORRUPTED_BLAZE
        )) {
            return;
        }

        /*
         * ========================================
         * DAÑO CUÁDRUPLE
         * ========================================
         *
         * Antes:
         * Daño x2.
         *
         * Ahora:
         * Daño x4.
         *
         * No creamos un segundo
         * evento de daño.
         */
        double originalDamage =
                event.getDamage();

        if (originalDamage > 0.0) {

            event.setDamage(
                    originalDamage
                            * CORRUPTED_DAMAGE_MULTIPLIER
            );
        }

        if (event.getEntity() instanceof Player player) {

            boolean shieldBlocked = false;

            /*
             * Ataque cuerpo a cuerpo.
             */
            if (damager instanceof Blaze blaze) {

                shieldBlocked =
                        BlazeShieldUtil.blocksMelee(
                                player,
                                blaze
                        );
            }

            /*
             * Ataque mediante proyectil.
             */
            else if (damager instanceof Fireball fireball) {

                shieldBlocked =
                        BlazeShieldUtil.blocksProjectile(
                                player,
                                fireball,
                                attacker
                        );
            }

            /*
             * Retirar Resistencia al Fuego
             * solamente si el escudo
             * no protegió al jugador.
             */
            if (!shieldBlocked) {

                removeFireResistance(player);
            }
        }
    }

    /*
     * ========================================
     * QUITAR RESISTENCIA AL FUEGO
     * ========================================
     */
    private void removeFireResistance(
            Player player
    ) {

        if (!player.hasPotionEffect(
                PotionEffectType.FIRE_RESISTANCE
        )) {
            return;
        }

        player.removePotionEffect(
                PotionEffectType.FIRE_RESISTANCE
        );
    }
}