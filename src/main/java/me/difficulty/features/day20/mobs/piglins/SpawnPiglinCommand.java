
package me.difficulty.features.day20.mobs.piglins;

import me.difficulty.core.day.DayManager;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

public final class SpawnPiglinCommand
        implements CommandExecutor {

    private final DayManager dayManager;
    private final CorruptedPiglin corrupted;

    public SpawnPiglinCommand(
            DayManager dayManager,
            CorruptedPiglin corrupted
    ) {
        this.dayManager = dayManager;
        this.corrupted = corrupted;
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
                    "§cEl Piglin Corrupto todavía "
                            + "no está disponible."
            );

            return true;
        }

        /*
         * Generar tres bloques delante
         * del jugador.
         */
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

        /*
         * Aplicar la variante personalizada.
         */
        corrupted.apply(piglin);

        player.sendMessage(
                "§aPiglin Corrupto creado para pruebas."
        );

        return true;
    }
}