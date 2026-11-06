
package me.difficulty.features.day20.mobs.blazes;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;

import org.bukkit.GameMode;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public final class BlazeKingKnockbackListener
        implements Listener {

    private final JavaPlugin plugin;
    private final DayManager dayManager;
    private final CustomMobRegistry mobRegistry;

    /*
     * ========================================
     * CONFIGURACIÓN DEL EMPUJE
     * ========================================
     */

    private static final double HORIZONTAL_KNOCKBACK = 0.70;
    private static final double VERTICAL_KNOCKBACK = 0.22;

    /*
     * ========================================
     * CONSTRUCTOR
     * ========================================
     */
    public BlazeKingKnockbackListener(
            JavaPlugin plugin,
            DayManager dayManager,
            CustomMobRegistry mobRegistry
    ) {

        this.plugin = plugin;
        this.dayManager = dayManager;
        this.mobRegistry = mobRegistry;
    }

    /*
     * ========================================
     * IMPACTO DE PROYECTILES
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void onProjectileHit(
            ProjectileHitEvent event
    ) {

        /*
         * Solo desde el Día 20.
         */
        if (!dayManager.isAtLeast(20)) {
            return;
        }

        /*
         * Solo bolas pequeñas de fuego.
         *
         * Incluye:
         * - Proyectiles normales.
         * - Los 75 proyectiles especiales.
         */
        if (!(event.getEntity()
                instanceof SmallFireball fireball)) {
            return;
        }

        /*
         * El objetivo debe ser un jugador.
         */
        if (!(event.getHitEntity()
                instanceof Player player)) {
            return;
        }

        /*
         * Identificar al Blaze que
         * lanzó el proyectil.
         */
        if (!(fireball.getShooter()
                instanceof Blaze blaze)) {
            return;
        }

        /*
         * Solo proyectiles del Rey Blaze.
         */
        if (!mobRegistry.is(
                blaze,
                MobIds.BLAZE_KING
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
         * No afectar jugadores en
         * creativo o espectador.
         */
        if (player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        /*
         * ========================================
         * DIRECCIÓN DEL EMPUJE
         * ========================================
         *
         * Usamos la dirección de vuelo
         * de la bola de fuego.
         */
        Vector direction =
                fireball.getVelocity().clone();

        /*
         * Solo necesitamos la dirección
         * horizontal.
         */
        direction.setY(0.0);

        /*
         * Si no hay velocidad suficiente,
         * utilizamos la aceleración.
         */
        if (direction.lengthSquared() < 0.0001) {

            direction =
                    fireball.getAcceleration().clone();

            direction.setY(0.0);
        }

        /*
         * Último respaldo:
         * alejar al jugador del Rey Blaze.
         */
        if (direction.lengthSquared() < 0.0001) {

            direction =
                    player.getLocation()
                            .toVector()
                            .subtract(
                                    blaze.getLocation()
                                            .toVector()
                            );

            direction.setY(0.0);
        }

        /*
         * Evitar normalizar
         * un vector vacío.
         */
        if (direction.lengthSquared() < 0.0001) {
            return;
        }

        /*
         * ========================================
         * CALCULAR EMPUJE FUERTE
         * ========================================
         */
        Vector knockback =
                direction.normalize()
                        .multiply(HORIZONTAL_KNOCKBACK);

        /*
         * Añadir elevación.
         */
        knockback.setY(
                VERTICAL_KNOCKBACK
        );

        /*
         * ========================================
         * APLICAR EN EL SIGUIENTE TICK
         * ========================================
         *
         * Evita que el procesamiento
         * vanilla del impacto sobrescriba
         * nuestro empuje personalizado.
         *
         * No altera el daño del evento.
         */
        plugin.getServer()
                .getScheduler()
                .runTask(plugin, () -> {

                    if (!player.isOnline()
                            || !player.isValid()
                            || player.isDead()) {
                        return;
                    }

                    if (player.getGameMode()
                            == GameMode.CREATIVE
                            || player.getGameMode()
                            == GameMode.SPECTATOR) {
                        return;
                    }

                    player.setVelocity(
                            knockback
                    );
                });
    }
}
