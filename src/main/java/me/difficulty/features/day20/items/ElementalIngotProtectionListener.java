
package me.difficulty.features.day20.items;

import org.bukkit.block.Crafter;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;

import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;

import org.bukkit.inventory.ItemStack;

public final class ElementalIngotProtectionListener
        implements Listener {

    private final ElementalIngot elementalIngot;

    public ElementalIngotProtectionListener(
            ElementalIngot elementalIngot
    ) {

        this.elementalIngot = elementalIngot;
    }

    private boolean containsIngot(
            ItemStack[] contents
    ) {

        if (contents == null) {
            return false;
        }

        for (ItemStack item : contents) {

            if (elementalIngot.isItem(item)) {
                return true;
            }
        }

        return false;
    }

    /*
     * ========================================
     * BLOQUEAR RECETAS VANILLA
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareCraft(
            PrepareItemCraftEvent event
    ) {

        if (containsIngot(
                event.getInventory().getMatrix()
        )) {

            event.getInventory().setResult(null);
        }
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCraft(
            CraftItemEvent event
    ) {

        if (containsIngot(
                event.getInventory().getMatrix()
        )) {

            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * BLOQUEAR CRAFTER AUTOMATICO
     * ========================================
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

        if (containsIngot(
                crafter.getInventory().getContents()
        )) {

            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * HERRERIA - RESERVADO PARA
     * LA ARMADURA ELEMENTAL
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareSmithing(
            PrepareSmithingEvent event
    ) {

        if (containsIngot(
                event.getInventory().getContents()
        )) {

            event.setResult(null);
        }
    }

    /*
     * ========================================
     * YUNQUE Y PIEDRA DE AFILAR
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareAnvil(
            PrepareAnvilEvent event
    ) {

        if (containsIngot(
                event.getInventory().getContents()
        )) {

            event.setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareGrindstone(
            PrepareGrindstoneEvent event
    ) {

        if (containsIngot(
                event.getInventory().getContents()
        )) {

            event.setResult(null);
        }
    }
}