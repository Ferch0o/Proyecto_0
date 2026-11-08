
package me.difficulty.features.day20.mobs.piglins;

import me.difficulty.core.day.DayManager;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

public final class SpawnExplosivePiglinCommand
        implements CommandExecutor {

    private final DayManager dayManager;
    private final ExplosivePiglin explosive;

    public SpawnExplosivePiglinCommand(
            DayManager dayManager,
            ExplosivePiglin explosive
    ) {
        this.dayManager = dayManager;
        this.explosive = explosive;
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

        if (!dayManager.isAtLeast(20)) {

            player.sendMessage(
                    "§cEl Piglin Explosivo no está disponible."
            );

            return true;
        }

        Location location = player.getLocation()
                .clone()
                .add(
                        player.getLocation()
                                .getDirection()
                                .multiply(3.0)
                );

        Piglin piglin = player.getWorld().spawn(
                location,
                Piglin.class
        );

        explosive.apply(piglin);

        player.sendMessage(
                "§aPiglin Explosivo creado para pruebas."
        );

        return true;
    }
}