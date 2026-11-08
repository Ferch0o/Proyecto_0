
package me.difficulty.features.day20.mobs.piglins;

import me.difficulty.core.day.DayManager;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.PiglinAbstract;
import org.bukkit.entity.PiglinBrute;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

public final class SpawnKingPiglinCommand
        implements CommandExecutor {

    private final DayManager dayManager;
    private final KingPiglin king;

    public SpawnKingPiglinCommand(
            DayManager dayManager,
            KingPiglin king
    ) {
        this.dayManager = dayManager;
        this.king = king;
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
                    "§cEl Rey Piglin todavía no está disponible."
            );

            return true;
        }

        /*
         * ========================================
         * TIPO DE ENTIDAD
         * ========================================
         */
        boolean normal = false;

        if (args.length > 0) {

            if (args[0].equalsIgnoreCase("normal")) {

                normal = true;

            } else if (!args[0].equalsIgnoreCase("brute")) {

                player.sendMessage(
                        "§cUso: /spawnkingpiglin [normal|brute]"
                );

                return true;
            }
        }

        /*
         * Generar delante del jugador.
         */
        Location location = player.getLocation()
                .clone()
                .add(
                        player.getLocation()
                                .getDirection()
                                .multiply(3.0)
                );

        PiglinAbstract piglin;

        if (normal) {

            piglin = player.getWorld().spawn(
                    location,
                    Piglin.class
            );

        } else {

            piglin = player.getWorld().spawn(
                    location,
                    PiglinBrute.class
            );
        }

        king.apply(piglin);

        player.sendMessage(
                "§aRey Piglin creado para pruebas "
                        + (normal
                        ? "(Piglin normal)."
                        : "(Piglin Bruto).")
        );

        return true;
    }
}