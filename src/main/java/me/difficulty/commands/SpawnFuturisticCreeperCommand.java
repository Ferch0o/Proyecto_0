package me.difficulty.commands;

import me.difficulty.day.DayManager;
import me.difficulty.mobs.day10.FuturisticCreeper;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpawnFuturisticCreeperCommand
        implements CommandExecutor {

    private final FuturisticCreeper futuristicCreeper;
    private final DayManager dayManager;

    public SpawnFuturisticCreeperCommand(
            FuturisticCreeper futuristicCreeper,
            DayManager dayManager
    ) {

        this.futuristicCreeper =
                futuristicCreeper;

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


        Creeper creeper =
                player.getWorld().spawn(
                        location,
                        Creeper.class
                );


        futuristicCreeper.apply(
                creeper
        );


        player.sendMessage(
                "§aCreeper Futurista creado."
        );

        return true;
    }
}