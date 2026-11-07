
package me.difficulty.features.day20.mobs.pigmen;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;

import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class PigmanCombatListener implements Listener {

    private final DayManager dayManager;
    private final CustomMobRegistry registry;

    public PigmanCombatListener(
            DayManager dayManager,
            CustomMobRegistry registry
    ) {
        this.dayManager = dayManager;
        this.registry = registry;
    }

    /*
     * ========================================
     * PIGMAN CORRUPTO - QUITAR RESISTENCIA
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCorruptedPigmanHit(
            EntityDamageByEntityEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        if (!(event.getDamager()
                instanceof PigZombie pigman)) {
            return;
        }

        if (!registry.is(
                pigman,
                MobIds.CORRUPTED_PIGMAN
        )) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        /*
         * Comprobar daño bruto para
         * funcionar también con Absorción.
         */
        if (event.getDamage() <= 0.0) {
            return;
        }

        /*
         * El escudo frontal protege
         * contra la habilidad especial.
         */
        if (isShieldFacingAttacker(player, pigman)) {
            return;
        }

        /*
         * Eliminar Resistencia,
         * sin importar su nivel.
         */
        player.removePotionEffect(
                PotionEffectType.RESISTANCE
        );
    }

    /*
     * ========================================
     * NINJA - INMUNIDAD A CAÍDAS
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onNinjaFall(
            EntityDamageEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        if (event.getCause()
                != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        if (!(event.getEntity()
                instanceof PigZombie pigman)) {
            return;
        }

        if (registry.is(
                pigman,
                MobIds.NINJA_PIGMAN
        )) {
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
            PigZombie attacker
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

        /*
         * Evaluación horizontal:
         * el escudo debe mirar
         * hacia el atacante.
         */
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
