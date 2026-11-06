
package me.difficulty.features.day20.items;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

import org.bukkit.block.Crafter;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.block.CrafterCraftEvent;

import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import org.bukkit.plugin.java.JavaPlugin;

public final class ElementalRodProtectionListener
        implements Listener {

    private final ElementalRod elementalRod;

    /*
     * Identificador reservado para
     * la futura receta de la
     * Plantilla Elemental.
     */
    private final NamespacedKey elementalTemplateRecipeKey;

    /*
     * ========================================
     * CONSTRUCTOR
     * ========================================
     */
    public ElementalRodProtectionListener(
            JavaPlugin plugin,
            ElementalRod elementalRod
    ) {

        this.elementalRod = elementalRod;

        this.elementalTemplateRecipeKey =
                new NamespacedKey(
                        plugin,
                        "elemental_template_recipe"
                );
    }

    /*
     * ========================================
     * IDENTIFICAR VARA ELEMENTAL
     * ========================================
     */
    private boolean isElementalRod(
            ItemStack item
    ) {

        return elementalRod.isItem(item);
    }

    /*
     * ========================================
     * BUSCAR VARA ELEMENTAL
     * ========================================
     */
    private boolean containsElementalRod(
            ItemStack[] items
    ) {

        for (ItemStack item : items) {

            if (isElementalRod(item)) {
                return true;
            }
        }

        return false;
    }

    /*
     * ========================================
     * RECETA PERMITIDA
     * ========================================
     *
     * Por ahora no hemos registrado
     * la receta de la Plantilla Elemental.
     *
     * Cuando la implementemos, deberá
     * utilizar esta misma clave.
     */
    private boolean isAllowedRecipe(
            Recipe recipe
    ) {

        return recipe instanceof Keyed keyed
                && elementalTemplateRecipeKey.equals(
                keyed.getKey()
        );
    }

    /*
     * ========================================
     * BLOQUEAR RESULTADOS VANILLA
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onPrepareCraft(
            PrepareItemCraftEvent event
    ) {

        if (!containsElementalRod(
                event.getInventory().getMatrix()
        )) {
            return;
        }

        /*
         * Reservar la única excepción:
         * Plantilla Elemental.
         */
        if (isAllowedRecipe(event.getRecipe())) {
            return;
        }

        /*
         * No mostrar resultados vanilla.
         */
        event.getInventory().setResult(null);
    }

    /*
     * ========================================
     * CANCELAR CRAFTEOS VANILLA
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCraft(
            CraftItemEvent event
    ) {

        if (!containsElementalRod(
                event.getInventory().getMatrix()
        )) {
            return;
        }

        /*
         * Permitir solamente la futura
         * receta de la Plantilla Elemental.
         */
        if (isAllowedRecipe(event.getRecipe())) {
            return;
        }

        /*
         * Impedir consumir la vara
         * en cualquier otra receta.
         */
        event.setCancelled(true);
    }

    /*
     * ========================================
     * BLOQUEAR CRAFTER AUTOMÁTICO
     * ========================================
     *
     * Por ahora no permitimos que
     * un Crafter consuma Varas Elementales.
     *
     * Cuando implementemos la Plantilla
     * Elemental decidiremos si también
     * se podrá fabricar automáticamente.
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

        if (containsElementalRod(
                crafter.getInventory().getContents()
        )) {

            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * IMPEDIR UTILIZAR COMO COMBUSTIBLE
     * ========================================
     *
     * Afecta a hornos, altos hornos
     * y ahumadores.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onFurnaceBurn(
            FurnaceBurnEvent event
    ) {

        if (!isElementalRod(
                event.getFuel()
        )) {
            return;
        }

        /*
         * No iniciar la combustión
         * ni consumir la Vara Elemental.
         */
        event.setCancelled(true);
    }
}