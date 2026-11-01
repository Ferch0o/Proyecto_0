package me.difficulty.features.day17.items;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class GiveDay17MaterialsCommand implements CommandExecutor {
    private final Day17Materials materials;

    public GiveDay17MaterialsCommand(Day17Materials materials) {
        this.materials = materials;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo jugadores.");
            return true;
        }
        if (args.length != 0) return false;
        give(player, materials.driedMeat().createItem(), 64);
        give(player, materials.guardianBone().createItem(), 64);
        give(player, materials.magicGunpowder().createItem(), 64);
        give(player, materials.goliathEye().createItem(), 64);
        give(player, materials.titaniumBlock().createItem(), 32);
        give(player, materials.wardenHeart().createItem(), 2);
        give(player, new ItemStack(Material.POISONOUS_POTATO), 16);
        player.sendMessage("§aRecibiste los materiales de una Patata Elemental.");
        return true;
    }

    private void give(Player player, ItemStack item, int amount) {
        item.setAmount(amount);
        for (ItemStack excess : player.getInventory().addItem(item).values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), excess);
        }
    }
}
