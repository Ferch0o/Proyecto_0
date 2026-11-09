
package me.difficulty.features.day20.items;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Crafter;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import org.bukkit.plugin.java.JavaPlugin;

public final class ElementalPearlProtectionListener
        implements Listener {

    private final ElementalPearl elementalPearl;
    private final NamespacedKey multiPearlsRecipeKey;

    public ElementalPearlProtectionListener(
            JavaPlugin plugin,
            ElementalPearl elementalPearl
    ) {

        this.elementalPearl = elementalPearl;

        /*
         * Unica receta permitida actualmente.
         */
        multiPearlsRecipeKey = new NamespacedKey(
                plugin,
                "multi_pearls_recipe"
        );
    }

    /*
     * ========================================
     * DETECTAR PERLAS ELEMENTALES
     * ========================================
     */
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
     * ========================================
     * RECETA PERMITIDA
     * ========================================
     */
    private boolean isAllowedRecipe(
            Recipe recipe
    ) {

        return recipe instanceof Keyed keyed
                && multiPearlsRecipeKey.equals(
                keyed.getKey()
        );
    }

    /*
     * ========================================
     * PREPARAR CRAFTEO
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareCraft(
            PrepareItemCraftEvent event
    ) {

        if (!containsElementalPearl(
                event.getInventory().getMatrix()
        )) {
            return;
        }

        /*
         * Permitir Multi Pearls.
         */
        if (isAllowedRecipe(event.getRecipe())) {
            return;
        }

        /*
         * Bloquear cualquier otra receta.
         */
        event.getInventory().setResult(null);
    }

    /*
     * ========================================
     * CRAFTEO MANUAL
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCraft(
            CraftItemEvent event
    ) {

        if (!containsElementalPearl(
                event.getInventory().getMatrix()
        )) {
            return;
        }

        /*
         * Permitir fabricar Multi Pearls.
         */
        if (isAllowedRecipe(event.getRecipe())) {
            return;
        }

        event.setCancelled(true);
    }

    /*
     * ========================================
     * CRAFTER AUTOMATICO
     * ========================================
     *
     * Seguimos bloqueando el consumo
     * automatico de Perlas Elementales.
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