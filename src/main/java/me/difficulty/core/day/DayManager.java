
package me.difficulty.core.day;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

public class DayManager {

    private final JavaPlugin plugin;

    private volatile int currentDay;

    /*
     * Sistemas interesados en los cambios de dia.
     */
    private final List<BiConsumer<Integer, Integer>>
            dayChangeListeners =
            new CopyOnWriteArrayList<>();

    public DayManager(JavaPlugin plugin) {

        this.plugin = plugin;

        // Respeta el dia guardado en servidores existentes.
        this.currentDay = Math.max(
                0,
                plugin.getConfig().getInt("current-day", 0)
        );
    }

    public int getCurrentDay() {
        return currentDay;
    }

    /*
     * Registrar un listener que recibira:
     *
     * - Dia anterior
     * - Dia nuevo
     */
    public void addDayChangeListener(
            BiConsumer<Integer, Integer> listener
    ) {

        dayChangeListeners.add(listener);
    }

    public void setCurrentDay(int day) {

        if (day < 0) {
            return;
        }

        int previousDay = this.currentDay;

        // No generar eventos si el dia no cambia.
        if (previousDay == day) {
            return;
        }

        this.currentDay = day;

        plugin.getConfig().set(
                "current-day",
                day
        );

        plugin.saveConfig();

        /*
         * Notificar a los sistemas registrados.
         */
        for (BiConsumer<Integer, Integer> listener :
                dayChangeListeners) {

            listener.accept(previousDay, day);
        }
    }

    public boolean isAtLeast(int day) {
        return currentDay >= day;
    }
}