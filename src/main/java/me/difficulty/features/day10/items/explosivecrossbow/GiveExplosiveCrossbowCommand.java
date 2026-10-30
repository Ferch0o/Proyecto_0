
package me.difficulty.features.day10.items.explosivecrossbow;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class GiveExplosiveCrossbowCommand
        implements CommandExecutor {

    private final ExplosiveCrossbow explosiveCrossbow;

    public GiveExplosiveCrossbowCommand(
            ExplosiveCrossbow explosiveCrossbow
    ) {
        this.explosiveCrossbow = explosiveCrossbow;
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
                    ChatColor.RED
                            + "Este comando solo puede usarlo un jugador."
            );
            return true;
        }

        if (args.length > 0) {
            player.sendMessage(
                    ChatColor.RED
                            + "Uso: /giveexplosivecrossbow"
            );
            return true;
        }

        // Crear la ballesta personalizada real.
        ItemStack item = explosiveCrossbow.createItem();

        // Añadirla al inventario.
        Map<Integer, ItemStack> remaining =
                player.getInventory().addItem(item);

        // Si el inventario está lleno,
        // dejar la ballesta cerca del jugador.
        remaining.values().forEach(
                leftover -> player.getWorld().dropItemNaturally(
                        player.getLocation(),
                        leftover
                )
        );

        player.sendMessage(
                ChatColor.GREEN
                        + "Has recibido una Ballesta Explosiva."
        );

        return true;
    }
}