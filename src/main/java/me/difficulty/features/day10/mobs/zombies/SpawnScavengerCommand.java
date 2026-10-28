package me.difficulty.features.day10.mobs.zombies;

import me.difficulty.core.day.DayManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.jetbrains.annotations.NotNull;

public class SpawnScavengerCommand
        implements CommandExecutor {

    private final Scavenger scavenger;
    private final DayManager dayManager;

    public SpawnScavengerCommand(
            Scavenger scavenger,
            DayManager dayManager
    ) {

        this.scavenger =
                scavenger;

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

        Zombie zombie =
                player.getWorld().spawn(
                        location,
                        Zombie.class
                );

        scavenger.apply(
                zombie
        );

        player.sendMessage(
                "§aCarroñero creado."
        );

        return true;
    }
}