package me.difficulty.features.day10.mobs.creepers;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FuturisticCreeperExplosionListener
        implements Listener {

    private final DayManager dayManager;
    private final CustomMobRegistry mobRegistry;

    /*
     * Nos permite distinguir nuestro daño
     * personalizado del daño vanilla.
     */
    private final Set<UUID> applyingCustomDamage =
            new HashSet<>();


    public FuturisticCreeperExplosionListener(
            DayManager dayManager,
            CustomMobRegistry mobRegistry
    ) {

        this.dayManager = dayManager;
        this.mobRegistry = mobRegistry;
    }


    /*
     * ========================================
     * EXPLOSIÓN
     * ========================================
     */

    @EventHandler(
            priority = EventPriority.HIGH,
            ignoreCancelled = true
    )
    public void onFuturisticCreeperExplode(
            EntityExplodeEvent event
    ) {

        if (!(event.getEntity()
                instanceof Creeper creeper)) {

            return;
        }

        if (!dayManager.isAtLeast(10)) {
            return;
        }

        if (!mobRegistry.is(
                creeper,
                MobIds.FUTURISTIC_CREEPER
        )) {

            return;
        }


        /*
         * ========================================
         * NO ROMPER BLOQUES
         * ========================================
         */

        event.blockList().clear();

        event.setYield(
                0.0f
        );


        /*
         * ========================================
         * DAÑO A JUGADORES
         * ========================================
         *
         * Lo calculamos nosotros ignorando
         * completamente paredes y bloques.
         */

        Location explosionLocation =
                event.getLocation();

        double radius =
                creeper.getExplosionRadius();

        /*
         * Alcance aproximado que utiliza
         * la explosión sobre entidades.
         *
         * Radio 5:
         * alcance ≈ 10 bloques.
         */
        double maximumDistance =
                radius * 2.0;



        for (Entity entity : creeper.getWorld().getNearbyEntities(
                explosionLocation,
                maximumDistance,
                maximumDistance,
                maximumDistance
        )) {
            if (!(entity instanceof LivingEntity target)) {
                continue;
            }

            if (!target.isValid() || target.isDead()
                    || target.equals(creeper)) {
                continue;
            }

            // No atacar jugadores en creativo o espectador.
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

            if (distance > maximumDistance) {
                continue;
            }

            double damage = calculateDamage(
                    distance,
                    maximumDistance
            );

            if (damage <= 0.0) {
                continue;
            }

            DamageSource damageSource = DamageSource
                    .builder(DamageType.EXPLOSION)
                    .withCausingEntity(creeper)
                    .withDirectEntity(creeper)
                    .withDamageLocation(explosionLocation)
                    .build();

            UUID targetId = target.getUniqueId();
            applyingCustomDamage.add(targetId);

            try {
                target.damage(damage, damageSource);
            } finally {
                applyingCustomDamage.remove(targetId);
            }
        }
    }


    /*
     * ========================================
     * ELIMINAR DAÑO VANILLA DEL CREEPER
     * ========================================
     *
     * Evita:
     *
     * daño vanilla
     * +
     * daño personalizado
     *
     * al mismo tiempo.
     */

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onVanillaExplosionDamage(
            EntityDamageEvent event
    ) {
        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }

        if (applyingCustomDamage.contains(target.getUniqueId())) {
            return;
        }

        if (event.getCause()
                != EntityDamageEvent
                .DamageCause
                .ENTITY_EXPLOSION) {

            return;
        }


        Entity source =
                event.getDamageSource()
                        .getCausingEntity();

        if (!(source
                instanceof Creeper creeper)) {

            source =
                    event.getDamageSource()
                            .getDirectEntity();
        }


        if (!(source
                instanceof Creeper creeper)) {

            return;
        }


        if (!mobRegistry.is(
                creeper,
                MobIds.FUTURISTIC_CREEPER
        )) {

            return;
        }


        /*
         * Cancelamos únicamente el daño
         * vanilla del Creeper Futurista.
         *
         * Nuestro daño personalizado
         * se aplica aparte.
         */
        event.setCancelled(
                true
        );
    }

    /*
     * ========================================
     * CÁLCULO DE DAÑO
     * ========================================
     */

    private double calculateDamage(
            double distance,
            double maximumDistance
    ) {
        double impact = 1.0 - (distance / maximumDistance);

        if (impact <= 0.0) {
            return 0.0;
        }

        // Radio del Creeper normal
        double normalExplosionRange = 3.0 * 2.0;

        // Fórmula base de daño de explosión vanilla.
        // Adaptamos la distancia al radio ampliado del
        // Creeper Futurista.
        double normalDamage = (
                (impact * impact + impact) / 2.0
        ) * 7.0 * normalExplosionRange + 1.0;

        // Doble del daño base
        return normalDamage * 2.0;
    }
}