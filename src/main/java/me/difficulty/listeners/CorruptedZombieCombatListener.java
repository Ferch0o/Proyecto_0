package me.difficulty.listeners;

import me.difficulty.day.DayManager;
import me.difficulty.mobs.CustomMobRegistry;
import me.difficulty.mobs.MobIds;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CorruptedZombieCombatListener
        implements Listener {

    /*
     * Guardamos:
     *
     * UUID del jugador
     *      ↓
     * momento en que termina su stun
     */
    private final Map<UUID, Long> stunnedPlayers =
            new HashMap<>();

    private final CustomMobRegistry mobRegistry;
    private final DayManager dayManager;

    public CorruptedZombieCombatListener(
            CustomMobRegistry mobRegistry,
            DayManager dayManager
    ) {

        this.mobRegistry =
                mobRegistry;

        this.dayManager =
                dayManager;
    }

    /*
     * ========================================
     * ZOMBIE CORRUPTO GOLPEA AL JUGADOR
     * ========================================
     */

    @EventHandler
    public void onCorruptedZombieHit(
            EntityDamageByEntityEvent event
    ) {

        /*
         * El objetivo debe ser un jugador.
         */
        if (!(event.getEntity()
                instanceof Player player)) {

            return;
        }

        /*
         * El atacante debe ser un Zombie.
         */
        if (!(event.getDamager()
                instanceof Zombie zombie)) {

            return;
        }

        /*
         * El comportamiento pertenece
         * al Día 10.
         */
        if (!dayManager.isAtLeast(10)) {
            return;
        }

        /*
         * Comprobamos que sea exactamente
         * nuestro Zombie Corrupto.
         */
        if (!mobRegistry.is(
                zombie,
                MobIds.CORRUPTED_ZOMBIE
        )) {

            return;
        }

        /*
         * 3 segundos = 3000 milisegundos.
         *
         * Si vuelve a golpear al jugador,
         * el tiempo vuelve a empezar.
         */
        long stunEnd =
                System.currentTimeMillis()
                        + 3000L;

        stunnedPlayers.put(
                player.getUniqueId(),
                stunEnd
        );

        /*
         * Quitamos la velocidad actual.
         *
         * Evita que el golpe lance al jugador
         * mientras comienza el stun.
         */
        player.setVelocity(
                new Vector(
                        0,
                        0,
                        0
                )
        );
    }


    /*
     * ========================================
     * IMPEDIR MOVIMIENTO
     * ========================================
     */

    @EventHandler
    public void onPlayerMove(
            PlayerMoveEvent event
    ) {

        Player player =
                event.getPlayer();

        Long stunEnd =
                stunnedPlayers.get(
                        player.getUniqueId()
                );

        /*
         * No está stuneado.
         */
        if (stunEnd == null) {
            return;
        }

        /*
         * Ya terminaron los 3 segundos.
         */
        if (System.currentTimeMillis()
                >= stunEnd) {

            stunnedPlayers.remove(
                    player.getUniqueId()
            );

            return;
        }

        Location from =
                event.getFrom();

        Location to =
                event.getTo();

        if (to == null) {
            return;
        }

        /*
         * Solo bloqueamos desplazamiento.
         *
         * El jugador todavía puede mover
         * la cámara.
         */
        boolean changedPosition =
                from.getX() != to.getX()
                        || from.getY() != to.getY()
                        || from.getZ() != to.getZ();

        if (!changedPosition) {
            return;
        }

        /*
         * Mantenemos la posición anterior,
         * pero conservamos hacia dónde está
         * mirando el jugador.
         */
        Location lockedLocation =
                from.clone();

        lockedLocation.setYaw(
                to.getYaw()
        );

        lockedLocation.setPitch(
                to.getPitch()
        );

        event.setTo(
                lockedLocation
        );

        /*
         * Evitamos acumulación de velocidad,
         * salto o knockback.
         */
        player.setVelocity(
                new Vector(
                        0,
                        0,
                        0
                )
        );
    }


    /*
     * ========================================
     * LIMPIEZA
     * ========================================
     */

    @EventHandler
    public void onPlayerQuit(
            PlayerQuitEvent event
    ) {

        stunnedPlayers.remove(
                event.getPlayer()
                        .getUniqueId()
        );
    }
}