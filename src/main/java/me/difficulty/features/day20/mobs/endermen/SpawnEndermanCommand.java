
package me.difficulty.features.day20.mobs.endermen;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMob;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

public final class SpawnEndermanCommand
        implements CommandExecutor {

    private final DayManager dayManager;
    private final CustomMob variant;
    private final String name;

    public SpawnEndermanCommand(
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
                    "§cLos Endermen del día 20 "
                            + "todavía no están disponibles."
            );

            return true;
        }

        /*
         * ========================================
         * GENERAR FRENTE AL JUGADOR
         * ========================================
         *
         * Utilizar una zona despejada
         * con suficiente altura.
         */
        Location location = player.getLocation()
                .clone()
                .add(
                        player.getLocation()
                                .getDirection()
                                .multiply(4.0)
                );

        Enderman enderman = player.getWorld().spawn(
                location,
                Enderman.class
        );

        /*
         * Aplicar variante personalizada.
         */
        variant.apply(enderman);

        player.sendMessage(
                "§a" + name + " creado para pruebas."
        );

        return true;
    }
}
