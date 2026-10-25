
package me.difficulty.listeners;

import me.difficulty.day.DayManager;
import me.difficulty.storm.StormManager;

import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;


public class StormDeathListener implements Listener {

    private final DayManager dayManager;
    private final StormManager stormManager;


    public StormDeathListener(
            DayManager dayManager,
            StormManager stormManager
    ) {

        this.dayManager = dayManager;
        this.stormManager = stormManager;
    }


    /*
     * ========================================
     * TORMENTA AL MORIR UN JUGADOR
     * ========================================
     *
     * Disponible desde el Día 0.
     *
     * Día 0 = 1 hora
     * Día 1 = 1 hora
     * Día 2 = 2 horas
     * Día 10 = 10 horas
     *
     * Si ya existe una tormenta,
     * las horas se acumulan.
     */

    @EventHandler(
            priority = EventPriority.MONITOR
    )
    public void onPlayerDeath(
            PlayerDeathEvent event
    ) {

        Player player = event.getEntity();

        int currentDay =
                dayManager.getCurrentDay();

        int addedHours =
                Math.max(1, currentDay);

        long addedSeconds =
                addedHours * 3600L;

        stormManager.addStormTime(
                addedSeconds,
                "Muerte de " + player.getName()
        );
    }
}
