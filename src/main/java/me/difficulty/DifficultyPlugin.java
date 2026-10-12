package me.difficulty;

import me.difficulty.commands.DayCommand;
import me.difficulty.commands.GiveCorruptedStarCommand;
import me.difficulty.commands.SetDayCommand;
import me.difficulty.day.DayManager;
import me.difficulty.items.CorruptedStar;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class DifficultyPlugin extends JavaPlugin{
    private DayManager dayManager;
    private CorruptedStar corruptedStar;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        dayManager = new DayManager(this);

        corruptedStar = new CorruptedStar(this);

        Objects.requireNonNull(getCommand("day"))
                .setExecutor(new DayCommand(dayManager));

        Objects.requireNonNull(getCommand("setday"))
                .setExecutor(new SetDayCommand(dayManager));

        Objects.requireNonNull(getCommand("givecorruptedstar"))
                .setExecutor(new GiveCorruptedStarCommand(corruptedStar));

        getLogger().info(
                "DifficultyPlugin iniciado correctamente. Día actual: "
                        + dayManager.getCurrentDay()
        );
    }
    @Override
    public void onDisable(){
        getLogger().info("DifficultyPlugin apagado");
    }
}