
package me.difficulty.features.day20.items;

import org.bukkit.block.Crafter;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import org.bukkit.inventory.ItemStack;

public final class ElementalPearlProtectionListener
        implements Listener {

    private final ElementalPearl elementalPearl;

    public ElementalPearlProtectionListener(
            ElementalPearl elementalPearl
    ) {
        this.elementalPearl = elementalPearl;
    }

    private boolean containsElementalPearl(
            ItemStack[] items
    ) {

        for (ItemStack item : items) {

            if (elementalPearl.isItem(item)) {
                return true;
            }
        }

        return false;
    }

    /*
     * Impedir resultados de recetas
     * que consuman Perlas Elementales.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareCraft(
            PrepareItemCraftEvent event
    ) {

        if (containsElementalPearl(
                event.getInventory().getMatrix()
        )) {
            event.getInventory().setResult(null);
        }
    }

    /*
     * Segunda protección del crafteo manual.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCraft(CraftItemEvent event) {

        if (containsElementalPearl(
                event.getInventory().getMatrix()
        )) {
            event.setCancelled(true);
        }
    }

    /*
     * Bloquear el Crafter automático.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCrafter(
            CrafterCraftEvent event
    ) {

        if (!(event.getBlock().getState()
                instanceof Crafter crafter)) {
            return;
        }

        if (containsElementalPearl(
                crafter.getInventory().getContents()
        )) {
            event.setCancelled(true);
        }
    }
}