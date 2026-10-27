package me.difficulty.features.day10.mobs.skeletons;

import me.difficulty.day.DayManager;
import me.difficulty.mobs.CustomMobRegistry;
import me.difficulty.mobs.MobIds;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class ElementalSkeletonCombatListener
        implements Listener {

    private final CustomMobRegistry mobRegistry;
    private final DayManager dayManager;

    public ElementalSkeletonCombatListener(
            CustomMobRegistry mobRegistry,
            DayManager dayManager
    ) {

        this.mobRegistry = mobRegistry;
        this.dayManager = dayManager;
    }


    /*
     * ========================================
     * EFECTOS DE LAS FLECHAS
     * ========================================
     */

    @EventHandler(ignoreCancelled = true)
    public void onElementalArrowHit(
            EntityDamageByEntityEvent event
    ) {

        /*
         * La entidad golpeada debe ser
         * un jugador.
         */
        if (!(event.getEntity()
                instanceof Player player)) {

            return;
        }


        /*
         * El daño debe venir de una flecha.
         */
        if (!(event.getDamager()
                instanceof Arrow arrow)) {

            return;
        }


        /*
         * El tirador debe ser un esqueleto.
         */
        if (!(arrow.getShooter()
                instanceof Skeleton skeleton)) {

            return;
        }


        /*
         * Solo desde el Día 10.
         */
        if (!dayManager.isAtLeast(10)) {
            return;
        }


        /*
         * Comprobamos que sea nuestro
         * Esqueleto Elemental.
         */
        if (!mobRegistry.is(
                skeleton,
                MobIds.ELEMENTAL_SKELETON
        )) {

            return;
        }


        /*
         * ========================================
         * TIRADA 0 - 99
         * ========================================
         */

        int roll =
                ThreadLocalRandom
                        .current()
                        .nextInt(100);


        /*
         * 0 - 4
         *
         * 5% Wither II
         */
        if (roll < 5) {

            applyEffect(
                    player,
                    PotionEffectType.WITHER,
                    1
            );

            return;
        }


        /*
         * 5 - 9
         *
         * 5% Lentitud II
         */
        if (roll < 10) {

            applyEffect(
                    player,
                    PotionEffectType.SLOWNESS,
                    1
            );

            return;
        }


        /*
         * 10 - 14
         *
         * 5% Veneno II
         */
        if (roll < 15) {

            applyEffect(
                    player,
                    PotionEffectType.POISON,
                    1
            );

            return;
        }


        /*
         * 15 - 19
         *
         * 5% Ceguera I
         */
        if (roll < 20) {

            applyEffect(
                    player,
                    PotionEffectType.BLINDNESS,
                    0
            );

            return;
        }


        /*
         * 20 - 24
         *
         * 5% Debilidad II
         */
        if (roll < 25) {

            applyEffect(
                    player,
                    PotionEffectType.WEAKNESS,
                    1
            );
        }


        /*
         * 25 - 99
         *
         * 75%
         *
         * No hacemos nada.
         * La flecha funciona normalmente.
         */
    }


    /*
     * ========================================
     * TRIPLE FLECHA
     * ========================================
     */

    @EventHandler(ignoreCancelled = true)
    public void onElementalSkeletonShoot(
            EntityShootBowEvent event
    ) {

        if (!(event.getEntity()
                instanceof Skeleton skeleton)) {

            return;
        }

        if (!dayManager.isAtLeast(10)) {
            return;
        }

        if (!mobRegistry.is(
                skeleton,
                MobIds.ELEMENTAL_SKELETON
        )) {

            return;
        }

        if (!(event.getProjectile()
                instanceof Arrow originalArrow)) {

            return;
        }


        /*
         * 5% de probabilidad de disparar
         * tres flechas.
         */
        if (ThreadLocalRandom
                .current()
                .nextInt(100) >= 20) {

            return;
        }

        /*
         * Tomamos la dirección en la que salió
         * la flecha original.
         */
        Vector direction =
                originalArrow
                        .getVelocity()
                        .clone();

        /*
         * Si por alguna razón Paper todavía nos da
         * una velocidad demasiado pequeña,
         * usamos la dirección hacia donde mira
         * el esqueleto como respaldo.
         */
        if (direction.lengthSquared() < 0.01) {

            direction =
                    skeleton.getEyeLocation()
                            .getDirection();
        }

        /*
         * Solo queremos la dirección.
         * La velocidad la estableceremos nosotros.
         */
        direction.normalize();


        Vector leftDirection =
                direction
                        .clone()
                        .rotateAroundY(
                                Math.toRadians(8)
                        );


        Vector rightDirection =
                direction
                        .clone()
                        .rotateAroundY(
                                Math.toRadians(-8)
                        );


        spawnExtraArrow(
                skeleton,
                originalArrow,
                leftDirection
        );

        spawnExtraArrow(
                skeleton,
                originalArrow,
                rightDirection
        );
    }


    /*
     * ========================================
     * APLICAR EFECTO
     * ========================================
     */

    private void applyEffect(
            Player player,
            PotionEffectType type,
            int amplifier
    ) {

        player.addPotionEffect(
                new PotionEffect(
                        type,

                        /*
                         * 10 segundos.
                         */
                        10 * 20,

                        amplifier
                )
        );
    }


    /*
     * ========================================
     * CREAR FLECHA EXTRA
     * ========================================
     */

    private void spawnExtraArrow(
            Skeleton skeleton,
            Arrow originalArrow,
            Vector direction
    ) {

        /*
         * Creamos la flecha desde la altura
         * de los ojos del esqueleto.
         *
         * La API de Paper permite indicar
         * dirección, velocidad y dispersión.
         */
        Arrow extraArrow =
                skeleton.getWorld()
                        .spawnArrow(
                                skeleton.getEyeLocation(),
                                direction,

                                /*
                                 * Velocidad de la flecha.
                                 */
                                2.0f,

                                /*
                                 * 0 = sin dispersión aleatoria.
                                 */
                                0.0f
                        );


        /*
         * Indicamos quién disparó la flecha.
         *
         * Esto es muy importante porque nuestro
         * listener de efectos luego comprueba
         * que el shooter sea un Esqueleto Elemental.
         */
        extraArrow.setShooter(
                skeleton
        );


        /*
         * Conservamos el daño.
         */
        extraArrow.setDamage(
                originalArrow.getDamage()
        );


        /*
         * Conservamos el fuego.
         */
        /*
         * Conservamos el arco asociado.
         */
        ItemStack weapon =
                originalArrow.getWeapon();

        if (weapon != null) {

            extraArrow.setWeapon(
                    weapon.clone()
            );


            /*
             * Si el arco tiene Fuego,
             * encendemos manualmente
             * la flecha adicional.
             */
            if (weapon.containsEnchantment(
                    Enchantment.FLAME
            )) {

                extraArrow.setFireTicks(
                        100
                );
            }
        }
    }
}