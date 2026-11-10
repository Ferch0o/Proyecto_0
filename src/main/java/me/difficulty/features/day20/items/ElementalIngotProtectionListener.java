
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

public final class ElementalIngotProtectionListener
        implements Listener {

    private final ElementalIngot elementalIngot;
    private final Set<NamespacedKey> armorRecipeKeys;


    public ElementalIngotProtectionListener(
            JavaPlugin plugin,
            ElementalIngot elementalIngot
    ) {

        this.elementalIngot = elementalIngot;

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

    private boolean isAllowedSmithingRecipe(
            Recipe recipe
    ) {

        return recipe instanceof Keyed keyed
                && armorRecipeKeys.contains(
                keyed.getKey()
        );
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareSmithing(
            PrepareSmithingEvent event
    ) {

        if (!containsIngot(
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

        if (!containsIngot(
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