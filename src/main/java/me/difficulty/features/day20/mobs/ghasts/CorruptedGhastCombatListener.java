
package me.difficulty.features.day20.mobs.ghasts;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class CorruptedGhastCombatListener
        implements Listener {

    /*
     * ========================================
     * CONFIGURACIÓN - GHAST CORRUPTO
     * ========================================
     *
     * Explosión de combate x9.
     *
     * Destrucción limitada a un radio
     * máximo de 5 bloques.
     */
    private static final float RADIUS_MULTIPLIER = 9.0f;

    private static final double BLOCK_DESTRUCTION_RADIUS = 5.0;

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
     *
     * Conservamos la potencia anterior.
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

        if (!(event.getEntity()
                instanceof LargeFireball fireball)) {
            return;
        }

        if (!isCorruptedGhastFireball(fireball)) {
            return;
        }

        event.setRadius(
                event.getRadius() * RADIUS_MULTIPLIER
        );
    }

    /*
     * ========================================
     * LIMITAR DESTRUCCIÓN DEL TERRENO
     * ========================================
     *
     * La explosión mantiene su potencia
     * de combate.
     *
     * Únicamente limitamos los bloques
     * que Minecraft puede destruir.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onEntityExplode(
            EntityExplodeEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        if (!(event.getEntity()
                instanceof LargeFireball fireball)) {
            return;
        }

        if (!isCorruptedGhastFireball(fireball)) {
            return;
        }

        Location center = event.getLocation();

        double maxDistanceSquared =
                BLOCK_DESTRUCTION_RADIUS
                        * BLOCK_DESTRUCTION_RADIUS;

        /*
         * Eliminar de la lista los bloques
         * situados fuera del radio permitido.
         *
         * No rompemos ni colocamos bloques
         * manualmente.
         */
        event.blockList().removeIf(block -> {

            double dx = block.getX() + 0.5
                    - center.getX();

            double dy = block.getY() + 0.5
                    - center.getY();

            double dz = block.getZ() + 0.5
                    - center.getZ();

            double distanceSquared =
                    dx * dx + dy * dy + dz * dz;

            return distanceSquared > maxDistanceSquared;
        });
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

        if (!(event.getEntity()
                instanceof Player player)) {
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
         * SOLO GHAST CORRUPTO
         */
        if (!isCorruptedGhastFireball(fireball)) {
            return;
        }

        /*
         * Respetar la Absorción.
         *
         * No usamos getFinalDamage().
         */
        if (event.getDamage() <= 0.0) {
            return;
        }

        /*
         * ESCUDO FRONTAL
         *
         * Si está bloqueando correctamente,
         * el jugador conserva Fuerza.
         */
        if (isShieldFacingExplosion(
                player,
                fireball
        )) {
            return;
        }

        /*
         * ELIMINAR FUERZA
         */
        player.removePotionEffect(
                PotionEffectType.STRENGTH
        );
    }

    /*
     * ========================================
     * COMPROBAR ESCUDO
     * ========================================
     */
    private boolean isShieldFacingExplosion(
            Player player,
            LargeFireball fireball
    ) {

        if (!player.isBlocking()) {
            return false;
        }

        /*
         * Dirección hacia el centro
         * de la explosión.
         */
        Vector sourceDirection = fireball
                .getLocation()
                .toVector()
                .subtract(
                        player.getEyeLocation()
                                .toVector()
                );

        sourceDirection.setY(0.0);

        /*
         * Respaldo para una explosión
         * exactamente sobre el jugador.
         */
        if (sourceDirection.lengthSquared() < 0.0001) {

            sourceDirection = fireball
                    .getVelocity()
                    .clone();

            if (sourceDirection.lengthSquared() < 0.0001) {

                sourceDirection = fireball
                        .getAcceleration()
                        .clone();
            }

            sourceDirection.multiply(-1.0);
            sourceDirection.setY(0.0);
        }

        if (sourceDirection.lengthSquared() < 0.0001) {
            return false;
        }

        Vector facing = player
                .getEyeLocation()
                .getDirection();

        facing.setY(0.0);

        if (facing.lengthSquared() < 0.0001) {
            return false;
        }

        facing.normalize();
        sourceDirection.normalize();

        return facing.dot(sourceDirection) > 0.0;
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