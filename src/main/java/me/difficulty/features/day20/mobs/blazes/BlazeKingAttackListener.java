
package me.difficulty.features.day20.mobs.blazes;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

import org.bukkit.entity.Blaze;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.SmallFireball;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public final class BlazeKingAttackListener
        implements Listener {

    private final JavaPlugin plugin;
    private final DayManager dayManager;
    private final CustomMobRegistry mobRegistry;

    /*
     * Datos individuales de cada Rey Blaze.
     */
    private final NamespacedKey attackCounterKey;
    private final NamespacedKey lastShotTickKey;
    private final NamespacedKey specialProjectileKey;

    /*
     * ========================================
     * CONFIGURACIÓN
     * ========================================
     */

    // Una ráfaga especial cada tres.
    private static final int SPECIAL_EVERY = 3;

    private static final int PROJECTILE_COUNT = 20;

    private static final int SPECIAL_WAVES = 3;

    private static final long WAVE_DELAY_TICKS = 6L;

    /*
     * Los tres disparos vanilla de una
     * ráfaga ocurren aproximadamente
     * cada 6 ticks.
     *
     * Una separación mayor a 20 ticks
     * se considera una nueva ráfaga.
     */
    private static final int BURST_GAP_TICKS = 20;

    /*
     * ========================================
     * CONSTRUCTOR
     * ========================================
     */
    public BlazeKingAttackListener(
            JavaPlugin plugin,
            DayManager dayManager,
            CustomMobRegistry mobRegistry
    ) {

        this.plugin = plugin;
        this.dayManager = dayManager;
        this.mobRegistry = mobRegistry;

        attackCounterKey = new NamespacedKey(
                plugin,
                "blaze_king_attack_counter"
        );

        lastShotTickKey = new NamespacedKey(
                plugin,
                "blaze_king_last_shot_tick"
        );

        specialProjectileKey = new NamespacedKey(
                plugin,
                "blaze_king_special_projectile"
        );
    }

    /*
     * ========================================
     * DETECTAR DISPAROS DEL REY BLAZE
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
         * Solo bolas pequeñas de fuego.
         */
        if (!(event.getEntity()
                instanceof SmallFireball original)) {
            return;
        }

        /*
         * No procesar los proyectiles
         * creados por nuestro ataque especial.
         */
        if (original.getPersistentDataContainer()
                .has(
                        specialProjectileKey,
                        PersistentDataType.BYTE
                )) {
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
         * Solo Rey Blaze.
         */
        if (!mobRegistry.is(
                blaze,
                MobIds.BLAZE_KING
        )) {
            return;
        }

        /*
         * ========================================
         * CONTADOR POR ENTIDAD
         * ========================================
         */
        PersistentDataContainer data =
                blaze.getPersistentDataContainer();

        int currentTick =
                Bukkit.getCurrentTick();

        int lastShotTick =
                data.getOrDefault(
                        lastShotTickKey,
                        PersistentDataType.INTEGER,
                        Integer.MIN_VALUE
                );

        /*
         * ¿Es una nueva ráfaga?
         *
         * Los disparos cercanos en el
         * tiempo pertenecen a la misma.
         */
        boolean newBurst =
                lastShotTick == Integer.MIN_VALUE
                        || currentTick < lastShotTick
                        || (long) currentTick - lastShotTick
                        > BURST_GAP_TICKS;

        /*
         * Guardar el momento del disparo.
         */
        data.set(
                lastShotTickKey,
                PersistentDataType.INTEGER,
                currentTick
        );

        int attackCounter =
                data.getOrDefault(
                        attackCounterKey,
                        PersistentDataType.INTEGER,
                        0
                );

        /*
         * Contar una sola vez por ráfaga,
         * no una vez por proyectil.
         */
        if (newBurst) {

            attackCounter =
                    (attackCounter % SPECIAL_EVERY) + 1;

            data.set(
                    attackCounterKey,
                    PersistentDataType.INTEGER,
                    attackCounter
            );
        }

        /*
         * ========================================
         * RÁFAGAS NORMALES
         * ========================================
         *
         * Primera y segunda ráfaga:
         * dejar disparar normalmente.
         */
        if (attackCounter != SPECIAL_EVERY) {
            return;
        }

        /*
         * ========================================
         * TERCERA RÁFAGA: ESPECIAL
         * ========================================
         *
         * Cancelamos los proyectiles
         * originales de esta ráfaga.
         */
        event.setCancelled(true);

        /*
         * Solo el primer proyectil
         * de la ráfaga especial crea
         * las cinco bolas nuevas.
         *
         * Los siguientes dos disparos
         * vanilla también se cancelan,
         * pero no crean más proyectiles.
         */
        if (!newBurst) {
            return;
        }

        /*
         * Obtener la dirección original.
         */
        Vector direction =
                getFireballDirection(
                        original,
                        blaze
                );

        /*
         * Guardar los datos necesarios.
         */
        Location spawnLocation =
                original.getLocation().clone();

        boolean incendiary =
                original.isIncendiary();

        double originalSpeed =
                original.getVelocity().length();

        double speed =
                originalSpeed > 0.05
                        ? originalSpeed
                        : 0.6;


        /*
         * ========================================
         * TERCERA RÁFAGA
         *
         * 3 OLEADAS DE 15 BOLAS
         * ========================================
         *
         * Oleada 1: tick +1
         * Oleada 2: tick +7
         * Oleada 3: tick +13
         *
         * Total: 45 bolas de fuego.
         */

        for (int wave = 0; wave < SPECIAL_WAVES; wave++) {
            long delay =
                    1L + (wave * WAVE_DELAY_TICKS);

            plugin.getServer()
                    .getScheduler()
                    .runTaskLater(plugin, () -> {

                        /*
                         * Si el Rey Blaze murió
                         * o desapareció, no
                         * continuar disparando.
                         */
                        if (!blaze.isValid()
                                || blaze.isDead()) {
                            return;
                        }

                        launchSpecialBurst(
                                blaze,
                                spawnLocation,
                                direction,
                                speed,
                                incendiary
                        );

                    }, delay);
        }
    }

    /*
     * ========================================
     * OBTENER DIRECCIÓN DEL DISPARO
     * ========================================
     */
    private Vector getFireballDirection(
            SmallFireball original,
            Blaze blaze
    ) {

        Vector direction =
                original.getAcceleration().clone();

        /*
         * Si todavía no tiene
         * aceleración válida,
         * utilizar la velocidad.
         */
        if (direction.lengthSquared() < 0.0001) {

            direction =
                    original.getVelocity().clone();
        }

        /*
         * Utilizar el objetivo del Blaze
         * como respaldo.
         */
        if (direction.lengthSquared() < 0.0001) {

            LivingEntity target =
                    blaze.getTarget();

            if (target != null) {

                direction = target.getEyeLocation()
                        .toVector()
                        .subtract(
                                original.getLocation()
                                        .toVector()
                        );

            } else {

                direction =
                        blaze.getEyeLocation()
                                .getDirection();
            }
        }

        /*
         * Seguridad ante vectores inválidos.
         */
        if (direction.lengthSquared() < 0.0001) {

            direction = new Vector(
                    0.0,
                    0.0,
                    1.0
            );
        }

        return direction.normalize();
    }

    /*
     * ========================================
     * LANZAR RÁFAGA DE 90 GRADOS
     * ========================================
     */

    /*
     * ========================================
     * LANZAR UNA OLEADA DE 15 BOLAS
     * ========================================
     */
    private void launchSpecialBurst(
            Blaze blaze,
            Location location,
            Vector originalDirection,
            double speed,
            boolean incendiary
    ) {

        /*
         * Distribución:
         *
         * 15 bolas en 90 grados.
         *
         * Primera: -45 grados.
         * Última:   +45 grados.
         *
         * Las demás se distribuyen
         * uniformemente entre ambas.
         */

        for (int i = 0; i < PROJECTILE_COUNT; i++) {

            /*
             * Calcular el ángulo individual.
             *
             * 90 / 14 = aproximadamente
             * 6.43 grados de separación.
             */
            double angle =
                    -45.0
                            + (90.0 * i)
                            / (PROJECTILE_COUNT - 1);

            /*
             * Girar la dirección original
             * horizontalmente.
             */
            Vector direction =
                    originalDirection
                            .clone()
                            .rotateAroundY(
                                    Math.toRadians(angle)
                            )
                            .normalize();

            /*
             * Crear bola de fuego pequeña.
             */
            blaze.getWorld().spawn(
                    location,
                    SmallFireball.class,
                    fireball -> {

                        /*
                         * Identificar la bola
                         * como proyectil especial
                         * antes de incorporarla
                         * al mundo.
                         */
                        fireball.getPersistentDataContainer()
                                .set(
                                        specialProjectileKey,
                                        PersistentDataType.BYTE,
                                        (byte) 1
                                );

                        /*
                         * Mantener al Rey Blaze
                         * como responsable.
                         */
                        fireball.setShooter(blaze);

                        /*
                         * Conservar comportamiento
                         * incendiario original.
                         */
                        fireball.setIsIncendiary(
                                incendiary
                        );

                        /*
                         * Dirección de disparo.
                         */
                        fireball.setDirection(
                                direction
                        );

                        /*
                         * Velocidad del proyectil.
                         */
                        fireball.setVelocity(
                                direction.clone()
                                        .multiply(speed)
                        );
                    }
            );
        }
    }
}