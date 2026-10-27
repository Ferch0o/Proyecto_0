package me.difficulty.features.day10.mobs.skeletons;

import me.difficulty.day.DayManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.jetbrains.annotations.NotNull;

public class SpawnGladiatorCommand
        implements CommandExecutor {

    private final Gladiator gladiator;
    private final DayManager dayManager;

    public SpawnGladiatorCommand(
            Gladiator gladiator,
            DayManager dayManager
    ) {

        this.gladiator =
                gladiator;

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

        Skeleton skeleton =
                player.getWorld().spawn(
                        location,
                        Skeleton.class
                );

        gladiator.apply(
                skeleton
        );

        player.sendMessage(
                "§aGladiador creado."
        );

        return true;
    }
}