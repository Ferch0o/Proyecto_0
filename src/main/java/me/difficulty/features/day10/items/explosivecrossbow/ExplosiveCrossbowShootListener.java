package me.difficulty.features.day10.items.explosivecrossbow;

import me.difficulty.core.day.DayManager;

import org.bukkit.*;

import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import org.bukkit.persistence.PersistentDataType;

import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class ExplosiveCrossbowShootListener
        implements Listener {


    /*
     * ========================================
     * CONFIGURACIÓN
     * ========================================
     */


    /*
     * Duración de vuelo del cohete.
     *
     * Esto NO controla el daño.
     */
    private static final int FIREWORK_POWER =
            1;

    private static final float EXPLOSION_POWER =
            3F;

    /*
     * ========================================
     * RADIO PARA ELIMINAR EL EMPUJE
     * ========================================
     *
     * Guardamos la velocidad de todas
     * las entidades cercanas antes
     * de generar la explosión.
     */
    private static final double VELOCITY_CHECK_RADIUS =
            6.0;

    /*
     * ========================================
     * VARIABLES
     * ========================================
     */

    private final JavaPlugin plugin;

    private final ExplosiveCrossbow explosiveCrossbow;

    private final DayManager dayManager;


    /*
     * Identificador de los cohetes
     * creados por nuestra Ballesta Explosiva.
     */
    private final NamespacedKey explosiveProjectileKey;

    // Evita cancelar nuestros propios eventos de dano personalizado.
    private final Set<UUID> applyingCustomDamage = new HashSet<>();


    /*
     * ========================================
     * CONSTRUCTOR
     * ========================================
     */

    public ExplosiveCrossbowShootListener(
            JavaPlugin plugin,
            ExplosiveCrossbow explosiveCrossbow,
            DayManager dayManager
    ) {

        this.plugin =
                plugin;

        this.explosiveCrossbow =
                explosiveCrossbow;

        this.dayManager =
                dayManager;


        this.explosiveProjectileKey =
                new NamespacedKey(
                        plugin,
                        "explosive_crossbow_projectile"
                );
    }


    /*
     * ========================================
     * DISPARAR BALLESTA
     * ========================================
     */

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onShoot(
            EntityShootBowEvent event
    ) {


        /*
         * ========================================
         * OBTENER ARMA
         * ========================================
         */

        ItemStack weapon =
                event.getBow();


        if (weapon == null) {
            return;
        }


        /*
         * ========================================
         * COMPROBAR BALLESTA EXPLOSIVA
         * ========================================
         *
         * Las ballestas normales
         * continúan funcionando normalmente.
         */

        if (!explosiveCrossbow
                .isExplosiveCrossbow(
                        weapon
                )) {

            return;
        }


        /*
         * ========================================
         * RESTRICCIÓN DÍA 10
         * ========================================
         */

        if (!dayManager.isAtLeast(10)) {


            event.setCancelled(true);


            if (event.getEntity()
                    instanceof Player player) {


                player.sendMessage(
                        "§cLa Ballesta Explosiva no está disponible antes del Día 10."
                );
            }


            return;
        }


        /*
         * ========================================
         * PROYECTIL ORIGINAL
         * ========================================
         *
         * Puede ser:
         *
         * - Flecha
         * - Flecha espectral
         * - Flecha con poción
         * - Cohete
         *
         * No importa.
         *
         * Siempre será reemplazado
         * por nuestro cohete.
         */

        Entity originalProjectile =
                event.getProjectile();


        /*
         * Guardamos la velocidad
         * del proyectil original.
         */

        Vector velocity =
                originalProjectile
                        .getVelocity()
                        .clone();


        /*
         * ========================================
         * CREAR COHETE
         * ========================================
         */

        Firework firework =
                originalProjectile
                        .getWorld()
                        .spawn(
                                originalProjectile
                                        .getLocation(),

                                Firework.class,

                                spawnedFirework -> {


                                    /*
                                     * ========================================
                                     * CONFIGURAR FIREWORK
                                     * ========================================
                                     */

                                    FireworkMeta meta =
                                            spawnedFirework
                                                    .getFireworkMeta();


                                    /*
                                     * Flight Duration 1.
                                     *
                                     * Solo controla cuánto
                                     * tiempo vuela.
                                     */
                                    meta.setPower(
                                            FIREWORK_POWER
                                    );


                                    /*
                                     * ========================================
                                     * EFECTO VISUAL
                                     * ========================================
                                     *
                                     * Una explosión visual roja.
                                     *
                                     * El daño fuerte vendrá
                                     * después del cálculo de daño personalizado.
                                     */
                                    meta.addEffect(
                                            FireworkEffect
                                                    .builder()
                                                    .with(
                                                            FireworkEffect.Type.BALL
                                                    )
                                                    .withColor(
                                                            Color.RED
                                                    )
                                                    .build()
                                    );


                                    spawnedFirework
                                            .setFireworkMeta(
                                                    meta
                                            );


                                    /*
                                     * ========================================
                                     * DISPARO EN ÁNGULO
                                     * ========================================
                                     *
                                     * Hace que el cohete funcione
                                     * como uno disparado por
                                     * una ballesta.
                                     */
                                    spawnedFirework
                                            .setShotAtAngle(
                                                    true
                                            );


                                    /*
                                     * ========================================
                                     * SHOOTER
                                     * ========================================
                                     */

                                    spawnedFirework
                                            .setShooter(
                                                    event.getEntity()
                                            );


                                    /*
                                     * ========================================
                                     * MARCAR COHETE CUSTOM
                                     * ========================================
                                     */

                                    spawnedFirework
                                            .getPersistentDataContainer()
                                            .set(
                                                    explosiveProjectileKey,
                                                    PersistentDataType.BYTE,
                                                    (byte) 1
                                            );
                                }
                        );


        /*
         * ========================================
         * CONSERVAR VELOCIDAD DEL DISPARO
         * ========================================
         */

        firework.setVelocity(
                velocity
        );


        /*
         * ========================================
         * REEMPLAZAR PROYECTIL
         * ========================================
         */

        event.setProjectile(
                firework
        );


        /*
         * Eliminamos el proyectil original
         * para evitar que salgan:
         *
         * flecha + cohete
         *
         * simultáneamente.
         */

        originalProjectile.remove();
    }


    /*
     * ========================================
     * EXPLOSIÓN REAL SIN EMPUJE
     * ========================================
     */

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onFireworkExplode(
            FireworkExplodeEvent event
    ) {


        Firework firework =
                event.getEntity();


        /*
         * ========================================
         * COMPROBAR IDENTIDAD
         * ========================================
         *
         * Los cohetes normales de Minecraft
         * NO reciben esta explosión especial.
         */

        if (!firework
                .getPersistentDataContainer()
                .has(
                        explosiveProjectileKey,
                        PersistentDataType.BYTE
                )) {

            return;
        }


        /*
         * ========================================
         * OBTENER SHOOTER
         * ========================================
         */

        Entity source =
                null;


        if (firework.getShooter()
                instanceof Entity entity) {

            source =
                    entity;
        }


        /*
         * ========================================
         * GUARDAR VELOCIDADES
         * ========================================
         *
         * Las explosiones vanilla generan
         * knockback automáticamente.
         *
         * Por eso guardamos la velocidad
         * original de jugadores y mobs.
         */

        Map<LivingEntity, Vector> previousVelocities =
                new HashMap<>();


        for (Entity nearby :
                firework
                        .getWorld()
                        .getNearbyEntities(
                                firework.getLocation(),
                                VELOCITY_CHECK_RADIUS,
                                VELOCITY_CHECK_RADIUS,
                                VELOCITY_CHECK_RADIUS
                        )) {


            /*
             * Solo entidades vivas.
             *
             * Jugadores
             * Zombies
             * Esqueletos
             * Animales
             * etc.
             */

            if (!(nearby
                    instanceof LivingEntity living)) {

                continue;
            }


            /*
             * Guardamos exactamente
             * la velocidad que tenía
             * antes de la explosión.
             */

            previousVelocities.put(
                    living,
                    living
                            .getVelocity()
                            .clone()
            );
        }


        /*
         * ========================================
         * DAÑO EXPLOSIVO PERSONALIZADO, POTENCIA 3
         * ========================================
         *
         * No se crea una explosión de mundo:
         * los objetos tirados y los bloques
         * no reciben daño por esta mecánica.
         */

        // Explosión de daño personalizado.
        // No creamos una explosión de mundo:
        // así no dañamos objetos tirados ni bloques.

        Location explosionLocation = firework.getLocation();

        double radius = EXPLOSION_POWER * 2.0;

        // Paper 1.21.5 requiere entidad directa si hay entidad causante.
        // El cohete es el golpe directo; el tirador recibe el credito.
        DamageSource.Builder builder = DamageSource
                .builder(DamageType.EXPLOSION)
                .withDirectEntity(firework);

        // Conservar la atribución de daño al tirador.
        if (source != null) {
            builder.withCausingEntity(source);
        }

        DamageSource damageSource = builder.build();

        for (Entity nearby : firework.getWorld().getNearbyEntities(
                explosionLocation,
                radius,
                radius,
                radius
        )) {
            // Solamente dañar entidades vivas.
            if (!(nearby instanceof LivingEntity target)) {
                continue;
            }

            if (!target.isValid() || target.isDead()) {
                continue;
            }

            if (target instanceof Player player) {
                if (player.getGameMode() == GameMode.CREATIVE
                        || player.getGameMode() == GameMode.SPECTATOR) {
                    continue;
                }
            }

            Location targetCenter = target.getLocation().add(
                    0.0,
                    target.getHeight() / 2.0,
                    0.0
            );

            double distance =
                    explosionLocation.distance(targetCenter);

            if (distance > radius) {
                continue;
            }

            double impact = 1.0 - (distance / radius);

            if (impact <= 0.0) {
                continue;
            }

            // Fórmula de daño explosivo, potencia 3.
            // No calcula exposición a paredes.
            double damage = (
                    (impact * impact + impact) / 2.0
            ) * 7.0 * radius + 1.0;

            UUID targetId = target.getUniqueId();
            applyingCustomDamage.add(targetId);

            try {
                target.damage(damage, damageSource);
            } finally {
                applyingCustomDamage.remove(targetId);
            }
        }

        /*
         * ========================================
         * QUITAR EMPUJE INMEDIATAMENTE
         * ========================================
         */

        restoreVelocities(
                previousVelocities
        );


        /*
         * ========================================
         * QUITAR EMPUJE 1 TICK DESPUÉS
         * ========================================
         *
         * Algunas velocidades pueden aplicarse
         * durante la actualización siguiente
         * del servidor.
         *
         * Por eso restauramos nuevamente
         * un tick después.
         */

        plugin
                .getServer()
                .getScheduler()
                .runTask(
                        plugin,
                        () -> restoreVelocities(
                                previousVelocities
                        )
                );
    }


    /*
     * ========================================
     * EVITAR DAÑO VANILLA DEL COHETE CUSTOM
     * ========================================
     *
     * El cohete visual tiene su propio daño;
     * cancelarlo evita duplicarlo con el
     * daño personalizado de potencia 3.
     */

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCustomFireworkVanillaDamage(
            EntityDamageEvent event
    ) {
        Entity direct = event.getDamageSource()
                .getDirectEntity();

        if (!(direct instanceof Firework firework)) {
            return;
        }

        if (!firework.getPersistentDataContainer().has(
                explosiveProjectileKey,
                PersistentDataType.BYTE
        )) {
            return;
        }

        // El listener tambien recibe el dano que provocamos nosotros.
        // No cancelar ese evento, solo el dano vanilla del cohete.
        if (applyingCustomDamage.contains(event.getEntity().getUniqueId())) {
            return;
        }

        event.setCancelled(true);
    }

    /*
     * ========================================
     * RESTAURAR VELOCIDAD
     * ========================================
     */

    private void restoreVelocities(
            Map<LivingEntity, Vector> previousVelocities
    ) {


        for (Map.Entry<LivingEntity, Vector> entry :
                previousVelocities.entrySet()) {


            LivingEntity entity =
                    entry.getKey();


            /*
             * Si murió por la explosión
             * o dejó de existir,
             * no hacemos nada.
             */

            if (!entity.isValid()
                    || entity.isDead()) {

                continue;
            }


            /*
             * Restauramos exactamente
             * su movimiento anterior.
             */

            entity.setVelocity(
                    entry.getValue()
            );
        }
    }
}