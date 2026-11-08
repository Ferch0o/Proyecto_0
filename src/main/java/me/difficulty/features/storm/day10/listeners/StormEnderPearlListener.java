package me.difficulty.features.storm.day10.listeners;

import me.difficulty.features.day20.items.ElementalPearlTeleportUtil;
import me.difficulty.features.storm.core.StormManager;

import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;


public class StormEnderPearlListener
        implements Listener {


    private final StormManager stormManager;
    private final NamespacedKey elementalPearlProjectileKey;

    /*
     * ========================================
     * PROBABILIDAD DE FALLO
     * ========================================
     *
     * 5 = 5%
     */
    private static final int FAILURE_CHANCE = 10;

    public StormEnderPearlListener(
            StormManager stormManager,
            JavaPlugin plugin
    ) {
        this.stormManager = stormManager;

        this.elementalPearlProjectileKey =
                new NamespacedKey(
                        plugin,
                        "elemental_pearl_projectile"
                );
    }
    /*
     * ========================================
     * ENDER PEARL
     * ========================================
     */

    @EventHandler(
            priority = EventPriority.HIGH,
            ignoreCancelled = true
    )
    public void onEnderPearlTeleport(
            PlayerTeleportEvent event
    ) {


        /*
         * ========================================
         * SOLO NUESTRA TORMENTA
         * ========================================
         */

        if (!stormManager.hasDay10Effects()) {
            return;
        }


        /*
         * ========================================
         * SOLO ENDER PEARL
         * ========================================
         *
         * No afecta:
         *
         * /tp
         * portales
         * End Gateway
         * Chorus Fruit
         * plugins
         * etc.
         */

        if (event.getCause()
                != PlayerTeleportEvent
                .TeleportCause
                .ENDER_PEARL) {

            return;
        }

        /*
         * ========================================
         * PERLA ELEMENTAL
         * ========================================
         *
         * No puede fallar por nuestra Tormenta.
         *
         * La identificación depende de la
         * perla lanzada, no del inventario.
         */
        if (ElementalPearlTeleportUtil.isElementalTeleport(
                event,
                elementalPearlProjectileKey
        )) {
            return;
        }

        int roll =
                ThreadLocalRandom
                        .current()
                        .nextInt(100);

        /*
         * 0,1,2,3,4
         *
         * = 5 resultados de 100.
         */
        if (roll >= FAILURE_CHANCE) {
            return;
        }


        /*
         * ========================================
         * FALLÓ
         * ========================================
         *
         * La Ender Pearl ya fue lanzada
         * y consumida.
         *
         * Simplemente impedimos que el
         * teletransporte llegue a ejecutarse.
         */

        event.setCancelled(true);


        Player player =
                event.getPlayer();


        /*
         * ========================================
         * EFECTO VISUAL
         * ========================================
         *
         * Solo para que el jugador note
         * claramente que la perla falló.
         */

        player.getWorld()
                .spawnParticle(
                        Particle.PORTAL,
                        player.getLocation()
                                .add(
                                        0,
                                        1,
                                        0
                                ),
                        35,
                        0.4,
                        0.7,
                        0.4,
                        0.15
                );


        /*
         * ========================================
         * SONIDO
         * ========================================
         */

        player.playSound(
                player.getLocation(),
                Sound.ENTITY_ENDERMAN_TELEPORT,
                0.7f,
                0.6f
        );


        /*
         * ========================================
         * MENSAJE
         * ========================================
         */

        player.sendMessage(
                "§5La Tormenta ha interferido con tu Ender Pearl."
        );
    }
}