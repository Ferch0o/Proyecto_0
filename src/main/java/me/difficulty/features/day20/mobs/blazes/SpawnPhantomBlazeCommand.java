
package me.difficulty.features.day20.mobs.blazes;

import me.difficulty.core.day.DayManager;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

public final class SpawnPhantomBlazeCommand
        implements CommandExecutor {

    private final PhantomBlaze phantomBlaze;
    private final DayManager dayManager;

    public SpawnPhantomBlazeCommand(
            PhantomBlaze phantomBlaze,
            DayManager dayManager
    ) {
        this.phantomBlaze = phantomBlaze;
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

        if (!dayManager.isAtLeast(20)) {
            player.sendMessage(
                    "§cLos mobs del Día 20 todavía no están disponibles."
            );
            return true;
        }

        Location location = player.getLocation()
                .clone()
                .add(
                        player.getLocation()
                                .getDirection()
                                .multiply(3)
                );

        Blaze blaze = player.getWorld().spawn(
                location,
                Blaze.class
        );

        phantomBlaze.apply(blaze);

        player.sendMessage(
                "§5Blaze Fantasmal creado."
        );

        return true;
    }
}
