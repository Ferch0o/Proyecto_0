
package me.difficulty.features.day20.items;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Crafter;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;

import org.bukkit.event.inventory.*;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public final class ElementalTemplateProtectionListener
        implements Listener {

    private final ElementalTemplate template;
    private final NamespacedKey copyRecipeKey;
    private final Set<NamespacedKey> armorRecipeKeys;

    public ElementalTemplateProtectionListener(
            JavaPlugin plugin,
            ElementalTemplate template,
            NamespacedKey copyRecipeKey
    ) {
        this.template = template;
        this.copyRecipeKey = copyRecipeKey;

        armorRecipeKeys = Set.of(
                new NamespacedKey(
                        plugin, "elemental_helmet_smithing"
                ),
                new NamespacedKey(
                        plugin, "elemental_chestplate_smithing"
                ),
                new NamespacedKey(
                        plugin, "elemental_leggings_smithing"
                ),
                new NamespacedKey(
                        plugin, "elemental_boots_smithing"
                )
        );
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


    private boolean isAllowedSmithingRecipe(
            Recipe recipe
    ) {

        return recipe instanceof Keyed keyed
                && armorRecipeKeys.contains(
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareSmithing(
            PrepareSmithingEvent event
    ) {

        if (!containsTemplate(
                event.getInventory().getContents()
        )) {
            return;
        }

        if (isAllowedSmithingRecipe(
                event.getInventory().getRecipe()
        )) {
            return;
        }

        event.setResult(null);
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onSmithItem(
            SmithItemEvent event
    ) {

        if (!containsTemplate(
                event.getInventory().getContents()
        )) {
            return;
        }

        if (!isAllowedSmithingRecipe(
                event.getInventory().getRecipe()
        )) {

            event.setCancelled(true);
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