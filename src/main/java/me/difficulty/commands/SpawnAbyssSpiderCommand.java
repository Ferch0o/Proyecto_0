package me.difficulty.commands;

import me.difficulty.day.DayManager;
import me.difficulty.mobs.day10.AbyssSpider;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.jetbrains.annotations.NotNull;

public class SpawnAbyssSpiderCommand
        implements CommandExecutor {

    private final AbyssSpider abyssSpider;
    private final DayManager dayManager;


    public SpawnAbyssSpiderCommand(
            AbyssSpider abyssSpider,
            DayManager dayManager
    ) {

        this.abyssSpider =
                abyssSpider;

        this.dayManager =
                dayManager;
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


        abyssSpider.apply(
                spider
        );


        player.sendMessage(
                "§aAraña del Abismo creada."
        );

        return true;
    }
}