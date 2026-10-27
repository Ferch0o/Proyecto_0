
package me.difficulty.listeners;

import me.difficulty.storm.StormManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import org.bukkit.plugin.java.JavaPlugin;

public class StormBossBarListener implements Listener {

    private final JavaPlugin plugin;
    private final StormManager stormManager;

    public StormBossBarListener(
            JavaPlugin plugin,
            StormManager stormManager
    ) {
        this.plugin = plugin;
        this.stormManager = stormManager;
    }

    /*
     * ========================================
     * JUGADOR ENTRA AL SERVIDOR
     * ========================================
     *
     * Restaurar la barra si la
     * Tormenta ya estaba activa.
     */

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        /*
         * Esperamos 10 ticks (aprox. 0,5 s)
         * para que termine de establecerse
         * la conexión del jugador.
         */

        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {

                    if (!player.isOnline()) {
                        return;
                    }

                    if (!stormManager.isActive()) {
                        return;
                    }

                    stormManager.showBossBarFor(player);
                },
                10L
        );
    }

    /*
     * ========================================
     * JUGADOR SALE DEL SERVIDOR
     * ========================================
     *
     * Retirar su conexión de la BossBar.
     * La Tormenta y su temporizador
     * continúan normalmente.
     */

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        stormManager.removeBossBarFor(
                event.getPlayer()
        );
    }
}
