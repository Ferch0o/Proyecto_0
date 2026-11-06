
package me.difficulty.features.day20.mobs.blazes;

import me.difficulty.core.day.DayManager;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

public final class SpawnCorruptedBlazeCommand
        implements CommandExecutor {

    private final CorruptedBlaze corruptedBlaze;
    private final DayManager dayManager;

    public SpawnCorruptedBlazeCommand(
            CorruptedBlaze corruptedBlaze,
            DayManager dayManager
    ) {
        this.corruptedBlaze = corruptedBlaze;
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

        /*
         * Crear Blaze a 3 bloques
         * delante del jugador.
         */
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

        /*
         * Aplicar la variante
         * sin usar probabilidades.
         */
        corruptedBlaze.apply(blaze);

        player.sendMessage(
                "§aBlaze Corrupto creado."
        );

        return true;
    }
}