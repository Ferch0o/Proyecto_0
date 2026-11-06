
package me.difficulty.features.day20.mobs.ghasts;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMob;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

public final class SpawnGhastCommand
        implements CommandExecutor {

    private final DayManager dayManager;
    private final CustomMob variant;
    private final String name;

    public SpawnGhastCommand(
            DayManager dayManager,
            CustomMob variant,
            String name
    ) {
        this.dayManager = dayManager;
        this.variant = variant;
        this.name = name;
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
                    "§cLos Ghasts del Día 20 "
                            + "todavía no están disponibles."
            );

            return true;
        }

        /*
         * ========================================
         * GENERAR GHAST FRENTE AL JUGADOR
         * ========================================
         *
         * Buscar una zona abierta al probar.
         */
        Location location = player.getEyeLocation()
                .clone()
                .add(
                        player.getEyeLocation()
                                .getDirection()
                                .multiply(6.0)
                )
                .add(0.0, 2.0, 0.0);

        Ghast ghast = player.getWorld().spawn(
                location,
                Ghast.class
        );

        variant.apply(ghast);

        player.sendMessage(
                "§a" + name + " creado para pruebas."
        );

        return true;
    }
}
