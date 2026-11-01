package me.difficulty.features.day17.items.elementalpotato;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class GiveElementalPotatoCommand implements CommandExecutor {
    private final ElementalPotato potato;

    public GiveElementalPotatoCommand(ElementalPotato potato) {
        this.potato = potato;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo un jugador puede recibir la patata.");
            return true;
        }
        if (args.length != 0) return false;
        for (ItemStack excess : player.getInventory().addItem(potato.createItem()).values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), excess);
        }
        player.sendMessage("§aRecibiste una Patata Elemental de prueba.");
        return true;
    }
}
