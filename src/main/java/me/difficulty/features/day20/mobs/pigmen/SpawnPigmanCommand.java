
package me.difficulty.features.day20.mobs.pigmen;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMob;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

public final class SpawnPigmanCommand
        implements CommandExecutor {

    private final DayManager dayManager;
    private final CustomMob variant;
    private final String name;

    public SpawnPigmanCommand(
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
                    "Este comando solo puede ejecutarlo un jugador."
            );

            return true;
        }

        if (!dayManager.isAtLeast(20)) {

            player.sendMessage(
                    "§cLos Pigman del día 20 "
                            + "todavía no están disponibles."
            );

            return true;
        }

        /*
         * Crear frente al jugador.
         * Utilizar una zona despejada.
         */
        Location location = player.getLocation()
                .clone()
                .add(
                        player.getLocation()
                                .getDirection()
                                .multiply(3.0)
                );

        PigZombie pigman = player.getWorld().spawn(
                location,
                PigZombie.class
        );

        variant.apply(pigman);

        player.sendMessage(
                "§a" + name + " creado para pruebas."
        );

        return true;
    }
}
