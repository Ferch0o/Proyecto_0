package me.difficulty.commands;

import me.difficulty.day.DayManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SetDayCommand implements CommandExecutor {

    private final DayManager dayManager;

    public SetDayCommand(DayManager dayManager) {
        this.dayManager = dayManager;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {

        if (args.length != 1) {
            sender.sendMessage(
                    ChatColor.RED + "Uso correcto: /setday <día>"
            );
            return true;
        }

        int day;

        try {
            day = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {

            sender.sendMessage(
                    ChatColor.RED + "Debes introducir un número."
            );

            return true;
        }

        if (day < 1) {
            sender.sendMessage(
                    ChatColor.RED + "El día debe ser mayor o igual a 1."
            );
            return true;
        }

        dayManager.setCurrentDay(day);

        sender.sendMessage(
                ChatColor.GREEN
                        + "Día establecido en "
                        + day
                        + "."
        );

        return true;
    }
}