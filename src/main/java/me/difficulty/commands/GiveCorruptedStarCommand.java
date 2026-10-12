package me.difficulty.commands;

import me.difficulty.items.CorruptedStar;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class GiveCorruptedStarCommand implements CommandExecutor {

    private final CorruptedStar corruptedStar;

    public GiveCorruptedStarCommand(CorruptedStar corruptedStar) {
        this.corruptedStar = corruptedStar;
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
                    ChatColor.RED + "Este comando solo puede usarlo un jugador."
            );
            return true;
        }

        ItemStack item = corruptedStar.createItem();

        Map<Integer, ItemStack> remaining =
                player.getInventory().addItem(item);

        remaining.values().forEach(
                leftover -> player.getWorld().dropItemNaturally(
                        player.getLocation(),
                        leftover
                )
        );

        player.sendMessage(
                ChatColor.LIGHT_PURPLE + "Has recibido una Estrella Corrupta."
        );

        return true;
    }
}