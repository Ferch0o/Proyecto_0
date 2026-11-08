
package me.difficulty.features.day20.items;

import me.difficulty.core.day.DayManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.NotNull;

public final class GiveElementalPearlCommand
        implements CommandExecutor {

    private final DayManager dayManager;
    private final ElementalPearl elementalPearl;

    public GiveElementalPearlCommand(
            DayManager dayManager,
            ElementalPearl elementalPearl
    ) {
        this.dayManager = dayManager;
        this.elementalPearl = elementalPearl;
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
                    "§cLa Perla Elemental todavía no está disponible."
            );

            return true;
        }

        /*
         * Entregar 16 perlas de prueba.
         */
        ItemStack pearls = elementalPearl.createItem();
        pearls.setAmount(16);

        player.getInventory().addItem(pearls)
                .values()
                .forEach(leftover ->
                        player.getWorld().dropItemNaturally(
                                player.getLocation(),
                                leftover
                        )
                );

        player.sendMessage(
                "§aRecibiste 16 Perlas Elementales."
        );

        return true;
    }
}