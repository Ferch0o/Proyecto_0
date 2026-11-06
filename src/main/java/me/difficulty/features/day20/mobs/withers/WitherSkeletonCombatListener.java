
package me.difficulty.features.day20.mobs.withers;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;

import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class WitherSkeletonCombatListener
        implements Listener {

    /*
     * ========================================
     * CONFIGURACIÓN
     * ========================================
     *
     * Wither X durante 10 segundos.
     * Misma duración que el Wither vanilla.
     */
    private static final int WITHER_DURATION_TICKS = 200;
    private static final int WITHER_AMPLIFIER = 9;

    private final DayManager dayManager;
    private final CustomMobRegistry registry;

    public WitherSkeletonCombatListener(
            DayManager dayManager,
            CustomMobRegistry registry
    ) {
        this.dayManager = dayManager;
        this.registry = registry;
    }

    /*
     * ========================================
     * COMBATE DEL WITHER CORRUPTO
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onMeleeHit(
            EntityDamageByEntityEvent event
    ) {

        /*
         * SOLO DESDE EL DÍA 20
         */
        if (!dayManager.isAtLeast(20)) {
            return;
        }

        /*
         * IDENTIFICAR WITHER SKELETON
         */
        if (!(event.getDamager()
                instanceof WitherSkeleton skeleton)) {
            return;
        }

        /*
         * SOLO WITHER CORRUPTO
         */
        if (!registry.is(
                skeleton,
                MobIds.CORRUPTED_WITHER_SKELETON
        )) {
            return;
        }

        /*
         * SOLO JUGADORES
         */
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        /*
         * ========================================
         * COMPROBAR ATAQUE
         * ========================================
         *
         * Utilizamos el daño bruto.
         *
         * No utilizamos getFinalDamage()
         * porque la Absorción puede impedir
         * que el daño llegue a la vida normal.
         */
        if (event.getDamage() <= 0.0) {
            return;
        }

        /*
         * ========================================
         * PROTECCIÓN DEL ESCUDO
         * ========================================
         *
         * Si el jugador bloquea frontalmente,
         * no recibe las habilidades especiales.
         */
        if (isShieldFacingAttacker(
                player,
                skeleton
        )) {
            return;
        }

        /*
         * ========================================
         * ELIMINAR REGENERACIÓN
         * ========================================
         *
         * Funciona aunque el jugador
         * conserve corazones de Absorción.
         */
        if (player.hasPotionEffect(
                PotionEffectType.REGENERATION
        )) {

            player.removePotionEffect(
                    PotionEffectType.REGENERATION
            );
        }

        /*
         * ========================================
         * APLICAR WITHER X
         * ========================================
         *
         * Se aplica al impactar al jugador,
         * sin depender de que pierda
         * los corazones amarillos.
         *
         * No eliminamos la Absorción.
         */
        player.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.WITHER,
                        WITHER_DURATION_TICKS,
                        WITHER_AMPLIFIER,
                        false,
                        true,
                        true
                )
        );
    }

    /*
     * ========================================
     * COMPROBAR ESCUDO FRONTAL
     * ========================================
     */
    private boolean isShieldFacingAttacker(
            Player player,
            WitherSkeleton attacker
    ) {

        if (!player.isBlocking()) {
            return false;
        }

        /*
         * Dirección desde el jugador
         * hacia el atacante.
         */
        Vector towardAttacker = attacker
                .getEyeLocation()
                .toVector()
                .subtract(
                        player.getEyeLocation().toVector()
                );

        /*
         * Dirección hacia la que mira
         * el jugador.
         */
        Vector looking = player
                .getEyeLocation()
                .getDirection();

        /*
         * Evaluación horizontal.
         */
        towardAttacker.setY(0.0);
        looking.setY(0.0);

        if (towardAttacker.lengthSquared() < 0.0001
                || looking.lengthSquared() < 0.0001) {
            return false;
        }

        towardAttacker.normalize();
        looking.normalize();

        /*
         * Ataque desde delante:
         * el escudo protege.
         */
        return looking.dot(towardAttacker) > 0.0;
    }
}
