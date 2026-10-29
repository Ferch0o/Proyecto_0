
package me.difficulty.features.day7.gigante;

import me.difficulty.core.day.DayManager;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public final class SpawnGiantCommand implements CommandExecutor {

    private final GiantZombie giantZombie;
    private final DayManager dayManager;

    public SpawnGiantCommand(
            GiantZombie giantZombie,
            DayManager dayManager
    ) {
        this.giantZombie = giantZombie;
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

        // Disponible desde el dia 7
        if (!dayManager.isAtLeast(7)) {
            player.sendMessage(
                    "§cEl Zombie Gigante aparece desde el Dia 7."
            );
            return true;
        }

        // Direccion horizontal del jugador
        Vector direction = player.getLocation().getDirection();
        direction.setY(0);

        if (direction.lengthSquared() < 0.0001) {
            direction.setX(1);
        }

        direction.normalize().multiply(8);

        // Aparece a ocho bloques del jugador
        Location location = player.getLocation()
                .clone()
                .add(direction);

        Giant giant = player.getWorld().spawn(
                location,
                Giant.class
        );

        // Aplicar identidad y configuracion personalizada
        giantZombie.apply(giant);

        player.sendMessage(
                "§aZombie Gigante creado correctamente."
        );

        return true;
    }
}
