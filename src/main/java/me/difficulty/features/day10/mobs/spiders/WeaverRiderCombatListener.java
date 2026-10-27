package me.difficulty.features.day10.mobs.spiders;

import me.difficulty.day.DayManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class WeaverRiderCombatListener
        implements Listener {

    private final Weaver weaver;
    private final DayManager dayManager;


    public WeaverRiderCombatListener(
            Weaver weaver,
            DayManager dayManager
    ) {

        this.weaver = weaver;
        this.dayManager = dayManager;
    }


    @EventHandler(ignoreCancelled = true)
    public void onWeaverRiderHit(
            EntityDamageByEntityEvent event
    ) {

        /*
         * ========================================
         * SOLO JUGADORES
         * ========================================
         */

        if (!(event.getEntity()
                instanceof Player player)) {

            return;
        }


        /*
         * ========================================
         * SOLO FLECHAS
         * ========================================
         */

        if (!(event.getDamager()
                instanceof Arrow arrow)) {

            return;
        }


        /*
         * ========================================
         * EL TIRADOR DEBE SER UN ESQUELETO
         * ========================================
         */

        if (!(arrow.getShooter()
                instanceof Skeleton skeleton)) {

            return;
        }


        /*
         * ========================================
         * SOLO DESDE EL DÍA 10
         * ========================================
         */

        if (!dayManager.isAtLeast(10)) {
            return;
        }


        /*
         * ========================================
         * DEBE SER JINETE DE UNA TEJEDORA
         * ========================================
         */

        if (!weaver.isWeaverRider(
                skeleton
        )) {

            return;
        }


        /*
         * ========================================
         * 50% → TELARAÑA
         * ========================================
         */

        if (ThreadLocalRandom
                .current()
                .nextInt(100) < 30) {

            placeCobweb(
                    player
            );
        }


        /*
         * ========================================
         * 1% → FATIGA MINERA I
         * ========================================
         */

        if (ThreadLocalRandom
                .current()
                .nextInt(100) < 10) {

            player.addPotionEffect(
                    new PotionEffect(
                            PotionEffectType.MINING_FATIGUE,
                            60 * 20,
                            0
                    )
            );
        }
    }

    /*
     * ========================================
     * COLOCAR TELARAÑA
     * ========================================
     */

    private void placeCobweb(
            Player player
    ) {

        Block block =
                player.getLocation()
                        .getBlock();


        /*
         * No reemplazamos bloques existentes.
         *
         * Normalmente el bloque donde están
         * los pies del jugador es aire.
         */
        if (!block.getType().isAir()) {
            return;
        }


        block.setType(
                Material.COBWEB,
                false
        );
    }
}