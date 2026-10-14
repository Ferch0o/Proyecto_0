package me.difficulty.listeners;

import me.difficulty.day.DayManager;
import me.difficulty.mobs.CustomMobRegistry;
import me.difficulty.mobs.MobIds;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
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


        for (Player player :
                creeper.getWorld().getPlayers()) {


            /*
             * Calculamos desde aproximadamente
             * el centro del jugador.
             */
            Location playerCenter =
                    player.getLocation()
                            .add(
                                    0.0,
                                    player.getHeight() / 2.0,
                                    0.0
                            );


            double distance =
                    explosionLocation.distance(
                            playerCenter
                    );


            /*
             * Está fuera del alcance.
             */
            if (distance
                    > maximumDistance) {

                continue;
            }


            double damage =
                    calculateDamage(
                            distance,
                            maximumDistance
                    );


            if (damage <= 0.0) {
                continue;
            }


            /*
             * Creamos daño de tipo EXPLOSION.
             *
             * Al seguir siendo daño de explosión,
             * Minecraft puede aplicar las defensas
             * correspondientes.
             */
            DamageSource damageSource =
                    DamageSource
                            .builder(
                                    DamageType.EXPLOSION
                            )
                            .withCausingEntity(
                                    creeper
                            )
                            .withDirectEntity(
                                    creeper
                            )
                            .withDamageLocation(
                                    explosionLocation
                            )
                            .build();


            UUID playerId =
                    player.getUniqueId();

            applyingCustomDamage.add(
                    playerId
            );

            try {

                player.damage(
                        damage,
                        damageSource
                );

            } finally {

                applyingCustomDamage.remove(
                        playerId
                );
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

        if (!(event.getEntity()
                instanceof Player player)) {

            return;
        }


        /*
         * Si este daño lo estamos aplicando
         * nosotros mismos, lo dejamos pasar.
         */
        if (applyingCustomDamage.contains(
                player.getUniqueId()
        )) {

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

        /*
         * 1.0 cerca del centro.
         * 0.0 en el límite.
         */
        double impact =
                1.0
                        - (
                        distance
                                / maximumDistance
                );


        if (impact <= 0.0) {
            return 0.0;
        }


        /*
         * Fórmula de explosión con
         * exposición completa.
         *
         * NO hacemos raycast.
         * NO comprobamos paredes.
         */
        return (
                (
                        impact * impact
                                + impact
                )
                        / 2.0
        )
                * 7.0
                * maximumDistance
                + 1.0;
    }
}