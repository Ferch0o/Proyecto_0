package me.difficulty.commands;

import me.difficulty.core.day.DayManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class DayCommand implements CommandExecutor {

    private final DayManager dayManager;

    public DayCommand(DayManager dayManager) {
        this.dayManager = dayManager;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {

        sender.sendMessage(
                ChatColor.GOLD + "Día actual: "
                        + ChatColor.YELLOW
                        + dayManager.getCurrentDay()
        );

        return true;
    }
}