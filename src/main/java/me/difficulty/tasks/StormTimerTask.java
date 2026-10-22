package me.difficulty.tasks;

import me.difficulty.storm.StormManager;
import org.bukkit.scheduler.BukkitRunnable;

public class StormTimerTask
        extends BukkitRunnable {

    private final StormManager stormManager;


    public StormTimerTask(
            StormManager stormManager
    ) {

        this.stormManager =
                stormManager;
    }


    @Override
    public void run() {

        stormManager.tickSecond();
    }
}