
package me.difficulty.features.day20.mobs.piglins;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;

import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class PiglinCombatListener
        implements Listener {

    private final DayManager dayManager;
    private final CustomMobRegistry registry;

    public PiglinCombatListener(
            DayManager dayManager,
            CustomMobRegistry registry
    ) {
        this.dayManager = dayManager;
        this.registry = registry;
    }

    /*
     * ========================================
     * PIGLIN CORRUPTO
     * ELIMINAR VELOCIDAD AL GOLPEAR
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCorruptedHit(
            EntityDamageByEntityEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        if (!(event.getDamager()
                instanceof Piglin piglin)) {
            return;
        }

        if (!registry.is(
                piglin,
                MobIds.CORRUPTED_PIGLIN
        )) {
            return;
        }

        if (!(event.getEntity()
                instanceof Player player)) {
            return;
        }

        if (event.getDamage() <= 0.0) {
            return;
        }

        /*
         * El escudo frontal protege
         * contra esta habilidad.
         */
        if (isShieldFacingAttacker(
                player,
                piglin
        )) {
            return;
        }

        player.removePotionEffect(
                PotionEffectType.SPEED
        );
    }

    /*
     * ========================================
     * PIGLIN EXPLOSIVO
     * INMUNIDAD A EXPLOSIONES
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onExplosionDamage(
            EntityDamageEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        if (!(event.getEntity()
                instanceof Piglin piglin)) {
            return;
        }

        if (!registry.is(
                piglin,
                MobIds.EXPLOSIVE_PIGLIN
        )) {
            return;
        }

        EntityDamageEvent.DamageCause cause =
                event.getCause();

        if (cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                || cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {

            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * COMPROBAR ESCUDO FRONTAL
     * ========================================
     */
    private boolean isShieldFacingAttacker(
            Player player,
            Piglin attacker
    ) {

        if (!player.isBlocking()) {
            return false;
        }

        Vector towardAttacker = attacker
                .getEyeLocation()
                .toVector()
                .subtract(
                        player.getEyeLocation().toVector()
                );

        Vector facing = player
                .getEyeLocation()
                .getDirection();

        towardAttacker.setY(0.0);
        facing.setY(0.0);

        if (towardAttacker.lengthSquared() < 0.0001
                || facing.lengthSquared() < 0.0001) {
            return false;
        }

        return facing.normalize()
                .dot(towardAttacker.normalize()) > 0.0;
    }
}