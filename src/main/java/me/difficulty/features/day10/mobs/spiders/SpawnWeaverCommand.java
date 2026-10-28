package me.difficulty.features.day10.mobs.spiders;

import me.difficulty.core.day.DayManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.jetbrains.annotations.NotNull;

public class SpawnWeaverCommand
        implements CommandExecutor {

    private final Weaver weaver;
    private final DayManager dayManager;


    public SpawnWeaverCommand(
            Weaver weaver,
            DayManager dayManager
    ) {

        this.weaver = weaver;
        this.dayManager = dayManager;
    }


    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {

        if (!(sender instanceof Player player)) {

            sender.sendMessage(
                    "Este comando solo puede usarlo un jugador."
            );

            return true;
        }


        if (!dayManager.isAtLeast(10)) {

            player.sendMessage(
                    "§cLos mobs del Día 10 todavía no están disponibles."
            );

            return true;
        }


        Location location =
                player.getLocation()
                        .add(
                                player.getLocation()
                                        .getDirection()
                                        .multiply(3)
                        );


        Spider spider =
                player.getWorld().spawn(
                        location,
                        Spider.class
                );


        weaver.apply(
                spider
        );


        player.sendMessage(
                "§aTejedora creada."
        );

        return true;
    }
}