
package me.difficulty.features.day20.items;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
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
import org.bukkit.inventory.Recipe;

public final class ElementalTemplateProtectionListener
        implements Listener {

    private final ElementalTemplate template;
    private final NamespacedKey copyRecipeKey;

    public ElementalTemplateProtectionListener(
            ElementalTemplate template,
            NamespacedKey copyRecipeKey
    ) {

        this.template = template;
        this.copyRecipeKey = copyRecipeKey;
    }

    private boolean containsTemplate(
            ItemStack[] contents
    ) {

        if (contents == null) {
            return false;
        }

        for (ItemStack item : contents) {

            if (template.isItem(item)) {
                return true;
            }
        }

        return false;
    }

    /*
     * La duplicación es la única receta
     * de mesa que puede consumir
     * nuestra Plantilla Elemental.
     */
    private boolean isAllowedRecipe(
            Recipe recipe
    ) {

        return recipe instanceof Keyed keyed
                && copyRecipeKey.equals(
                keyed.getKey()
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepare(
            PrepareItemCraftEvent event
    ) {

        if (!containsTemplate(
                event.getInventory().getMatrix()
        )) {
            return;
        }

        if (isAllowedRecipe(event.getRecipe())) {
            return;
        }

        event.getInventory().setResult(null);
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCraft(
            CraftItemEvent event
    ) {

        if (!containsTemplate(
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

        if (containsTemplate(
                crafter.getInventory().getContents()
        )) {

            event.setCancelled(true);
        }
    }

    /*
     * Reservar las futuras recetas
     * de herrería para la Armadura Elemental.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareSmithing(
            PrepareSmithingEvent event
    ) {

        if (containsTemplate(
                event.getInventory().getContents()
        )) {

            event.setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareAnvil(
            PrepareAnvilEvent event
    ) {

        if (containsTemplate(
                event.getInventory().getContents()
        )) {

            event.setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareGrindstone(
            PrepareGrindstoneEvent event
    ) {

        if (containsTemplate(
                event.getInventory().getContents()
        )) {

            event.setResult(null);
        }
    }
}