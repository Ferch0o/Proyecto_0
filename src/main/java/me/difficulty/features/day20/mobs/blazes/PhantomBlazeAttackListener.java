
package me.difficulty.features.day20.mobs.blazes;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

import org.bukkit.entity.Blaze;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public final class PhantomBlazeAttackListener
        implements Listener {

    private final DayManager dayManager;
    private final CustomMobRegistry mobRegistry;

    /*
     * Identifica las bolas de fuego
     * especiales de nuestro plugin.
     */
    private final NamespacedKey specialFireballKey;

    /*
     * ========================================
     * CONFIGURACIÓN
     * ========================================
     */

    // 15% por proyectil lanzado.
    private static final int SPECIAL_CHANCE = 15;

    // Duración provisional de los efectos.
    private static final int EFFECT_DURATION = 20 * 20;

    // Radio de aplicación de efectos.
    private static final double EFFECT_RADIUS = 5.0;

    private static final double EFFECT_RADIUS_SQUARED =
            EFFECT_RADIUS * EFFECT_RADIUS;

    /*
     * ========================================
     * CONSTRUCTOR
     * ========================================
     */
    public PhantomBlazeAttackListener(
            JavaPlugin plugin,
            DayManager dayManager,
            CustomMobRegistry mobRegistry
    ) {

        this.dayManager = dayManager;
        this.mobRegistry = mobRegistry;

        this.specialFireballKey = new NamespacedKey(
                plugin,
                "phantom_blaze_special_fireball"
        );
    }

    /*
     * ========================================
     * DISPARO DEL BLAZE FANTASMAL
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onProjectileLaunch(
            ProjectileLaunchEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        /*
         * Solo las bolas pequeñas
         * que normalmente dispara un Blaze.
         */
        if (!(event.getEntity()
                instanceof SmallFireball original)) {
            return;
        }

        /*
         * Identificar al tirador.
         */
        if (!(original.getShooter()
                instanceof Blaze blaze)) {
            return;
        }

        /*
         * Solo nuestro Blaze Fantasmal.
         */
        if (!mobRegistry.is(
                blaze,
                MobIds.PHANTOM_BLAZE
        )) {
            return;
        }

        /*
         * ========================================
         * PROBABILIDAD DEL 15%
         * ========================================
         */
        if (ThreadLocalRandom.current()
                .nextInt(100) >= SPECIAL_CHANCE) {

            // El Blaze dispara normalmente.
            return;
        }

        /*
         * ========================================
         * DIRECCIÓN DEL PROYECTIL
         * ========================================
         */
        Vector direction =
                original.getAcceleration().clone();

        if (direction.lengthSquared() < 0.0001) {

            direction =
                    original.getVelocity().clone();
        }

        /*
         * Respaldo si el proyectil
         * todavía no tiene dirección.
         */
        if (direction.lengthSquared() < 0.0001) {

            LivingEntity target = blaze.getTarget();

            if (target != null) {

                direction = target
                        .getEyeLocation()
                        .toVector()
                        .subtract(
                                original.getLocation()
                                        .toVector()
                        );

            } else {

                direction = blaze
                        .getEyeLocation()
                        .getDirection();
            }
        }

        if (direction.lengthSquared() < 0.0001) {
            return;
        }

        direction.normalize();

        /*
         * Guardar los datos antes
         * de cancelar el proyectil.
         */
        final Vector shotDirection =
                direction.clone();

        final Location spawnLocation =
                original.getLocation().clone();

        /*
         * ========================================
         * REEMPLAZAR PROYECTIL VANILLA
         * ========================================
         *
         * Cancelamos la bola pequeña
         * para no disparar dos proyectiles.
         */
        event.setCancelled(true);

        /*
         * Crear bola grande tipo Ghast.
         */
        blaze.getWorld().spawn(
                spawnLocation,
                LargeFireball.class,
                fireball -> {

                    /*
                     * Conservar al tirador.
                     */
                    fireball.setShooter(blaze);

                    /*
                     * Dirección y velocidad.
                     */
                    fireball.setDirection(
                            shotDirection
                    );

                    fireball.setVelocity(
                            shotDirection.clone()
                                    .multiply(0.6)
                    );

                    /*
                     * Potencia de explosión
                     * similar a la del Ghast.
                     */
                    fireball.setYield(1.0f);

                    /*
                     * La explosión no
                     * iniciará incendios.
                     */
                    fireball.setIsIncendiary(false);

                    /*
                     * Marcar el proyectil
                     * como habilidad especial.
                     */
                    fireball
                            .getPersistentDataContainer()
                            .set(
                                    specialFireballKey,
                                    PersistentDataType.BYTE,
                                    (byte) 1
                            );
                }
        );
    }

    /*
     * ========================================
     * DETECTAR IMPACTO ESPECIAL
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onSpecialFireballHit(
            ProjectileHitEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        if (!(event.getEntity()
                instanceof LargeFireball fireball)) {
            return;
        }

        /*
         * Solo nuestras bolas especiales.
         */
        if (!isSpecialFireball(fireball)) {
            return;
        }

        /*
         * Punto de impacto.
         */
        Location impact =
                fireball.getLocation();

        /*
         * ========================================
         * EFECTOS EN ÁREA
         * ========================================
         *
         * Detectar jugadores dentro
         * de cuatro bloques.
         */
        for (Player player :
                impact.getWorld().getPlayers()) {

            if (!player.isOnline()
                    || player.isDead()) {
                continue;
            }

            if (player.getGameMode()
                    == GameMode.CREATIVE
                    || player.getGameMode()
                    == GameMode.SPECTATOR) {
                continue;
            }

            Location playerCenter =
                    player.getLocation()
                            .clone()
                            .add(0.0, 1.0, 0.0);

            if (playerCenter.distanceSquared(impact)
                    > EFFECT_RADIUS_SQUARED) {
                continue;
            }

            /*
             * Cada jugador afectado
             * recibe tiradas independientes.
             */
            applyRandomEffects(player);
        }
    }

    /*
     * ========================================
     * NO DESTRUIR BLOQUES
     * ========================================
     *
     * Conservamos la explosión
     * y su daño contra entidades,
     * evitando daños al terreno.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onSpecialExplosion(
            EntityExplodeEvent event
    ) {

        if (!(event.getEntity()
                instanceof LargeFireball fireball)) {
            return;
        }

        if (!isSpecialFireball(fireball)) {
            return;
        }

        event.blockList().clear();
    }

    /*
     * ========================================
     * IDENTIFICAR BOLA ESPECIAL
     * ========================================
     */
    private boolean isSpecialFireball(
            LargeFireball fireball
    ) {

        return fireball
                .getPersistentDataContainer()
                .has(
                        specialFireballKey,
                        PersistentDataType.BYTE
                );
    }

    /*
     * ========================================
     * EFECTOS INDEPENDIENTES
     * ========================================
     */
    private void applyRandomEffects(
            Player player
    ) {

        /*
         * Debilidad III - 40%
         */
        tryEffect(
                player,
                PotionEffectType.WEAKNESS,
                2,
                40
        );

        /*
         * Ceguera I - 35%
         */
        tryEffect(
                player,
                PotionEffectType.BLINDNESS,
                0,
                35
        );

        /*
         * Lentitud III - 55%
         */
        tryEffect(
                player,
                PotionEffectType.SLOWNESS,
                2,
                55
        );

        /*
         * Fatiga Minera II - 20%
         */
        tryEffect(
                player,
                PotionEffectType.MINING_FATIGUE,
                1,
                20
        );

        /*
         * Hambre III - 60%
         */
        tryEffect(
                player,
                PotionEffectType.HUNGER,
                2,
                60
        );

        /*
         * Veneno V - 30%
         */
        tryEffect(
                player,
                PotionEffectType.POISON,
                4,
                30
        );

        /*
         * Oscuridad I - 15%
         */
        tryEffect(
                player,
                PotionEffectType.DARKNESS,
                0,
                15
        );
    }

    /*
     * ========================================
     * INTENTAR APLICAR UN EFECTO
     * ========================================
     */
    private void tryEffect(
            Player player,
            PotionEffectType type,
            int amplifier,
            int chance
    ) {

        /*
         * Una tirada independiente
         * para cada efecto.
         */
        if (ThreadLocalRandom.current()
                .nextInt(100) >= chance) {
            return;
        }

        /*
         * Aplicar el efecto.
         */
        player.addPotionEffect(
                new PotionEffect(
                        type,
                        EFFECT_DURATION,
                        amplifier,
                        false,
                        true,
                        true
                )
        );
    }
}