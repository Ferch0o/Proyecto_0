
package me.difficulty.core.day;

import org.bukkit.plugin.java.JavaPlugin;

public class DayManager {

    private final JavaPlugin plugin;
    private int currentDay;

    public DayManager(JavaPlugin plugin) {
        this.plugin = plugin;

        // Día inicial para servidores nuevos.
        // Respeta el día guardado en servidores existentes.
        this.currentDay = Math.max(
                0,
                plugin.getConfig().getInt("current-day", 0)
        );
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(int day) {

        if (day < 0) {
            return;
        }

        this.currentDay = day;

        plugin.getConfig().set("current-day", day);
        plugin.saveConfig();
    }

    public boolean isAtLeast(int day) {
        return currentDay >= day;
    }
}
