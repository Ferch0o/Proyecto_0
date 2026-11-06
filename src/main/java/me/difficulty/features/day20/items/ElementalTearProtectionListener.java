
package me.difficulty.features.day20.items;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Crafter;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import org.bukkit.plugin.java.JavaPlugin;

public final class ElementalTearProtectionListener
        implements Listener {

    private final ElementalTear elementalTear;
    private final NamespacedKey templateRecipeKey;

    public ElementalTearProtectionListener(
            JavaPlugin plugin,
            ElementalTear elementalTear
    ) {
        this.elementalTear = elementalTear;

        this.templateRecipeKey = new NamespacedKey(
                plugin,
                "elemental_template_recipe"
        );
    }

    /*
     * ========================================
     * COMPROBAR LÁGRIMAS ELEMENTALES
     * ========================================
     */
    private boolean containsElementalTear(
            ItemStack[] items
    ) {

        for (ItemStack item : items) {

            if (elementalTear.isItem(item)) {
                return true;
            }
        }

        return false;
    }

    /*
     * ========================================
     * PERMITIR FUTURA PLANTILLA ELEMENTAL
     * ========================================
     */
    private boolean isAllowedRecipe(Recipe recipe) {

        return recipe instanceof Keyed keyed
                && templateRecipeKey.equals(keyed.getKey());
    }

    /*
     * ========================================
     * BLOQUEAR RESULTADO VANILLA
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareCraft(
            PrepareItemCraftEvent event
    ) {

        if (!containsElementalTear(
                event.getInventory().getMatrix()
        )) {
            return;
        }

        if (isAllowedRecipe(event.getRecipe())) {
            return;
        }

        event.getInventory().setResult(null);
    }

    /*
     * ========================================
     * BLOQUEAR CRAFTEO VANILLA
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCraft(CraftItemEvent event) {

        if (!containsElementalTear(
                event.getInventory().getMatrix()
        )) {
            return;
        }

        if (isAllowedRecipe(event.getRecipe())) {
            return;
        }

        event.setCancelled(true);
    }

    /*
     * ========================================
     * BLOQUEAR CRAFTER AUTOMÁTICO
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCrafterCraft(
            CrafterCraftEvent event
    ) {

        if (!(event.getBlock().getState()
                instanceof Crafter crafter)) {
            return;
        }

        if (containsElementalTear(
                crafter.getInventory().getContents()
        )) {
            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * IMPEDIR ELABORACIÓN DE POCIONES
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onBrew(BrewEvent event) {

        if (elementalTear.isItem(
                event.getContents().getIngredient()
        )) {
            event.setCancelled(true);
        }
    }
}
