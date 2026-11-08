
package me.difficulty.features.day20.mobs.endermen;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;

import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public final class EndermanCombatListener
        implements Listener {

    private static final int LEVITATION_CHANCE = 50;
    private static final int LEVITATION_DURATION = 100;
    private static final int LEVITATION_AMPLIFIER = 11;

    private final DayManager dayManager;
    private final CustomMobRegistry registry;

    public EndermanCombatListener(
            DayManager dayManager,
            CustomMobRegistry registry
    ) {
        this.dayManager = dayManager;
        this.registry = registry;
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onEndermanHit(
            EntityDamageByEntityEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        if (!(event.getDamager()
                instanceof Enderman enderman)) {
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
         * El escudo frontal bloquea
         * las habilidades especiales.
         */
        if (isShieldFacingAttacker(player, enderman)) {
            return;
        }

        /*
         * ========================================
         * ENDERMAN CORRUPTO
         * ========================================
         */
        if (registry.is(
                enderman,
                MobIds.CORRUPTED_ENDERMAN
        )) {

            player.setSaturation(0.0f);
            return;
        }

        /*
         * ========================================
         * ENDERMAN ANCESTRAL
         * ========================================
         */
        if (!registry.is(
                enderman,
                MobIds.ANCESTRAL_ENDERMAN
        )) {
            return;
        }

        /*
         * 5% por cada golpe válido.
         */
        if (ThreadLocalRandom.current()
                .nextInt(100) >= LEVITATION_CHANCE) {
            return;
        }

        /*
         * Levitación III durante 5 segundos.
         *
         * Amplificador 2 = nivel III.
         * 100 ticks = 5 segundos.
         */
        player.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.LEVITATION,
                        LEVITATION_DURATION,
                        LEVITATION_AMPLIFIER,
                        false,
                        true,
                        true
                )
        );
    }

    private boolean isShieldFacingAttacker(
            Player player,
            Enderman attacker
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