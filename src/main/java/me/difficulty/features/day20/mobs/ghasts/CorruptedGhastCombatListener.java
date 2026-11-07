
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
import org.bukkit.util.Vector;

public final class CorruptedGhastCombatListener
        implements Listener {

    /*
     * ========================================
     * CONFIGURACIÓN - GHAST CORRUPTO
     * ========================================
     *
     * Potencia de explosión x9.
     *
     * Minecraft calcula el daño
     * y el alcance naturalmente.
     */
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

        if (!(event.getEntity()
                instanceof LargeFireball fireball)) {
            return;
        }

        if (!isCorruptedGhastFireball(fireball)) {
            return;
        }

        /*
         * ========================================
         * POTENCIA DE EXPLOSIÓN x9
         * ========================================
         *
         * No aplicamos multiplicadores
         * adicionales de daño.
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

        /*
         * SOLO DESDE EL DÍA 20
         */
        if (!dayManager.isAtLeast(20)) {
            return;
        }

        /*
         * SOLO JUGADORES
         */
        if (!(event.getEntity() instanceof Player player)) {
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
         * SOLO BOLA DE FUEGO
         * DEL GHAST CORRUPTO
         */
        if (!isCorruptedGhastFireball(fireball)) {
            return;
        }

        /*
         * IGNORAR DAÑO NULO
         *
         * No usamos getFinalDamage(),
         * para respetar el funcionamiento
         * de la Absorción.
         */
        if (event.getDamage() <= 0.0) {
            return;
        }

        /*
         * ========================================
         * PROTECCIÓN CON ESCUDO
         * ========================================
         *
         * Si el jugador bloquea frontalmente,
         * la explosión NO elimina Fuerza.
         *
         * El daño de la explosión se
         * procesa normalmente por Minecraft.
         */
        if (isShieldFacingExplosion(player, fireball)) {
            return;
        }

        /*
         * ========================================
         * ELIMINAR FUERZA
         * ========================================
         *
         * Se ejecuta si el jugador no
         * está protegido frontalmente.
         *
         * Funciona incluso con Absorción.
         */
        player.removePotionEffect(
                PotionEffectType.STRENGTH
        );
    }

    /*
     * ========================================
     * COMPROBAR ESCUDO FRENTE A EXPLOSIÓN
     * ========================================
     */
    private boolean isShieldFacingExplosion(
            Player player,
            LargeFireball fireball
    ) {

        /*
         * El jugador debe estar
         * utilizando el escudo.
         */
        if (!player.isBlocking()) {
            return false;
        }

        /*
         * Dirección desde el jugador
         * hacia el centro de la explosión.
         */
        Vector sourceDirection = fireball
                .getLocation()
                .toVector()
                .subtract(
                        player.getEyeLocation().toVector()
                );

        /*
         * Dirección horizontal.
         */
        sourceDirection.setY(0.0);

        /*
         * ========================================
         * RESPALDO
         * ========================================
         *
         * Si la explosión ocurre exactamente
         * sobre el jugador, utilizar la
         * trayectoria inversa del proyectil.
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

        /*
         * Si no existe dirección válida,
         * no consideramos el ataque bloqueado.
         */
        if (sourceDirection.lengthSquared() < 0.0001) {
            return false;
        }

        /*
         * Dirección hacia donde mira
         * el jugador.
         */
        Vector facing = player
                .getEyeLocation()
                .getDirection();

        facing.setY(0.0);

        if (facing.lengthSquared() < 0.0001) {
            return false;
        }

        facing.normalize();
        sourceDirection.normalize();

        /*
         * ========================================
         * PRODUCTO PUNTO
         * ========================================
         *
         * Positivo: explosión desde delante.
         * Negativo: explosión desde detrás.
         *
         * Misma lógica direccional usada
         * en la protección del Blaze.
         */
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