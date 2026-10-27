
package me.difficulty.features.qualityoflife.friendlyfire;

import org.bukkit.World;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.world.WorldLoadEvent;

import org.bukkit.plugin.java.JavaPlugin;


public class FriendlyFireProtectionListener
        implements Listener {


    /*
     * ========================================
     * CONSTRUCTOR
     * ========================================
     */

    public FriendlyFireProtectionListener(
            JavaPlugin plugin
    ) {

        /*
         * Desactivar PvP en todos
         * los mundos ya cargados.
         *
         * Overworld
         * Nether
         * End
         */

        for (World world :
                plugin.getServer().getWorlds()) {

            world.setPVP(false);
        }
    }


    /*
     * ========================================
     * MUNDOS NUEVOS
     * ========================================
     *
     * Si el servidor carga otro mundo,
     * también se desactiva el PvP.
     */

    @EventHandler
    public void onWorldLoad(
            WorldLoadEvent event
    ) {

        event.getWorld().setPVP(false);
    }


    /*
     * ========================================
     * PROTECCIÓN ADICIONAL
     * ========================================
     *
     * Bloquear:
     *
     * Jugador → Jugador
     *
     * Proyectil de jugador → Jugador
     *
     * No afecta ataques de mobs.
     */

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onPlayerDamage(
            EntityDamageByEntityEvent event
    ) {


        /*
         * Solo protegemos a jugadores.
         */

        if (!(event.getEntity()
                instanceof Player victim)) {

            return;
        }


        Entity damager =
                event.getDamager();


        /*
         * ========================================
         * ATAQUE DIRECTO
         * ========================================
         */

        if (damager instanceof Player attacker) {

            if (!attacker.getUniqueId()
                    .equals(victim.getUniqueId())) {

                event.setCancelled(true);
            }

            return;
        }


        /*
         * ========================================
         * PROYECTILES
         * ========================================
         *
         * Flechas
         * Tridentes
         * Cohetes
         * Otros proyectiles
         */

        if (damager instanceof Projectile projectile) {

            if (projectile.getShooter()
                    instanceof Player attacker) {

                if (!attacker.getUniqueId()
                        .equals(victim.getUniqueId())) {

                    event.setCancelled(true);
                }
            }
        }
    }
}
