
package me.difficulty.features.day20.mobs.withers;

import me.difficulty.core.day.DayManager;
import me.difficulty.core.mobs.CustomMob;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;

import org.jetbrains.annotations.NotNull;

public final class SpawnWitherSkeletonCommand
        implements CommandExecutor {

    private final DayManager dayManager;
    private final CustomMob variant;
    private final String name;

    public SpawnWitherSkeletonCommand(
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

        /*
         * SOLO JUGADORES
         */
        if (!(sender instanceof Player player)) {

            sender.sendMessage(
                    "Este comando solo puede ejecutarlo un jugador."
            );

            return true;
        }

        /*
         * DISPONIBLE DESDE EL DÍA 20
         */
        if (!dayManager.isAtLeast(20)) {

            player.sendMessage(
                    "§cLos Wither Skeleton del Día 20 "
                            + "todavía no están disponibles."
            );

            return true;
        }

        /*
         * GENERAR FRENTE AL JUGADOR
         */
        Location location = player.getLocation()
                .clone()
                .add(
                        player.getLocation()
                                .getDirection()
                                .multiply(3.0)
                );

        WitherSkeleton skeleton =
                player.getWorld().spawn(
                        location,
                        WitherSkeleton.class
                );

        /*
         * APLICAR VARIANTE PERSONALIZADA
         */
        variant.apply(skeleton);

        player.sendMessage(
                "§a" + name + " creado para pruebas."
        );

        return true;
    }
}
