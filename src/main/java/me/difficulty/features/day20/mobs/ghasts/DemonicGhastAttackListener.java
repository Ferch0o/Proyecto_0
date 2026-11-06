
package me.difficulty.features.day20.mobs.ghasts;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMobRegistry;
import me.difficulty.core.mobs.MobIds;

import io.papermc.paper.event.entity.EntityKnockbackEvent;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

import org.bukkit.entity.Ghast;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public final class DemonicGhastAttackListener
        implements Listener {

    /*
     * ========================================
     * CONFIGURACIÓN
     * ========================================
     */

    // Probabilidad de explosión especial.
    private static final int SPECIAL_CHANCE = 50;

    // Valores provisionales del alcance y duración.
    private static final double EFFECT_RADIUS = 5.0;
    private static final double EFFECT_RADIUS_SQUARED =
            EFFECT_RADIUS * EFFECT_RADIUS;

    private static final int EFFECT_DURATION = 20 * 20;

    private final DayManager dayManager;
    private final CustomMobRegistry registry;

    private final NamespacedKey specialFireballKey;

    public DemonicGhastAttackListener(
            JavaPlugin plugin,
            DayManager dayManager,
            CustomMobRegistry registry
    ) {
        this.dayManager = dayManager;
        this.registry = registry;

        this.specialFireballKey = new NamespacedKey(
                plugin,
                "demonic_ghast_special_fireball"
        );
    }

    /*
     * ========================================
     * SELECCIONAR BOLA DE FUEGO ESPECIAL
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

        if (!(event.getEntity()
                instanceof LargeFireball fireball)) {
            return;
        }

        if (!(fireball.getShooter()
                instanceof Ghast ghast)) {
            return;
        }

        if (!registry.is(
                ghast,
                MobIds.DEMONIC_GHAST
        )) {
            return;
        }

        /*
         * ========================================
         * PROBABILIDAD DEL 50%
         * ========================================
         *
         * La bola de fuego sigue causando
         * su explosión normal.
         *
         * Simplemente añadimos una marca
         * para activar los efectos al impactar.
         */
        if (ThreadLocalRandom.current()
                .nextInt(100) >= SPECIAL_CHANCE) {
            return;
        }

        fireball.getPersistentDataContainer().set(
                specialFireballKey,
                PersistentDataType.BYTE,
                (byte) 1
        );
    }

    /*
     * ========================================
     * IMPACTO DE EXPLOSIÓN ESPECIAL
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onProjectileHit(
            ProjectileHitEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        if (!(event.getEntity()
                instanceof LargeFireball fireball)) {
            return;
        }

        /*
         * SOLO PROYECTILES MARCADOS
         */
        if (!fireball.getPersistentDataContainer().has(
                specialFireballKey,
                PersistentDataType.BYTE
        )) {
            return;
        }

        Location impact = fireball.getLocation();

        /*
         * ========================================
         * EFECTOS EN ÁREA
         * ========================================
         */
        for (Player player :
                impact.getWorld().getPlayers()) {

            if (!player.isOnline() || player.isDead()) {
                continue;
            }

            if (player.getGameMode() == GameMode.CREATIVE
                    || player.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }

            Location playerCenter = player.getLocation()
                    .clone()
                    .add(0.0, 1.0, 0.0);

            if (playerCenter.distanceSquared(impact)
                    > EFFECT_RADIUS_SQUARED) {
                continue;
            }

            /*
             * Cada jugador recibe sus propias
             * probabilidades independientes.
             */
            applyRandomEffects(player);
        }
    }

    /*
     * ========================================
     * INMUNIDAD AL EMPUJE
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onKnockback(
            EntityKnockbackEvent event
    ) {

        if (!dayManager.isAtLeast(20)) {
            return;
        }

        if (!(event.getEntity() instanceof Ghast ghast)) {
            return;
        }

        if (registry.is(
                ghast,
                MobIds.DEMONIC_GHAST
        )) {
            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * EFECTOS INDEPENDIENTES
     * ========================================
     */
    private void applyRandomEffects(Player player) {

        // Debilidad III - 40%
        tryEffect(
                player,
                PotionEffectType.WEAKNESS,
                2,
                40
        );

        // Ceguera I - 35%
        tryEffect(
                player,
                PotionEffectType.BLINDNESS,
                0,
                35
        );

        // Lentitud III - 55%
        tryEffect(
                player,
                PotionEffectType.SLOWNESS,
                2,
                55
        );

        // Fatiga Minera II - 20%
        tryEffect(
                player,
                PotionEffectType.MINING_FATIGUE,
                1,
                20
        );

        // Hambre III - 60%
        tryEffect(
                player,
                PotionEffectType.HUNGER,
                2,
                60
        );

        // Veneno V - 30%
        tryEffect(
                player,
                PotionEffectType.POISON,
                4,
                30
        );

        // Oscuridad I - 15%
        tryEffect(
                player,
                PotionEffectType.DARKNESS,
                0,
                15
        );
    }

    /*
     * ========================================
     * INTENTAR APLICAR UN EFECTO
     * ========================================
     */
    private void tryEffect(
            Player player,
            PotionEffectType type,
            int amplifier,
            int chance
    ) {

        if (ThreadLocalRandom.current()
                .nextInt(100) >= chance) {
            return;
        }

        player.addPotionEffect(
                new PotionEffect(
                        type,
                        EFFECT_DURATION,
                        amplifier,
                        false,
                        true,
                        true
                )
        );
    }
}
