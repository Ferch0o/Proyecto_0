package me.difficulty.day;

import org.bukkit.plugin.java.JavaPlugin;

public class DayManager {

    private final JavaPlugin plugin;
    private int currentDay;

    public DayManager(JavaPlugin plugin) {
        this.plugin = plugin;

        this.currentDay = plugin.getConfig().getInt("current-day", 1);
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(int day) {

        if (day < 1) {
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