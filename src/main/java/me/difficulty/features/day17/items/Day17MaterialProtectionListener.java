package me.difficulty.features.day17.items;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

/** Evita usos vanilla accidentales de materiales que solo son ingredientes custom. */
public final class Day17MaterialProtectionListener implements Listener {
    private final Day17Materials materials;
    private final NamespacedKey recipeKey;

    public Day17MaterialProtectionListener(Day17Materials materials, NamespacedKey recipeKey) {
        this.materials = materials;
        this.recipeKey = recipeKey;
    }

    private boolean protectedItem(ItemStack stack) {
        return stack != null && materials.all().stream().anyMatch(m -> m.matches(stack));
    }

    private boolean containsProtected(ItemStack[] stacks) {
        for (ItemStack stack : stacks) if (protectedItem(stack)) return true;
        return false;
    }

    private boolean allowed(Recipe recipe) {
        return recipe instanceof Keyed keyed && keyed.getKey().equals(recipeKey);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepare(PrepareItemCraftEvent event) {
        if (containsProtected(event.getInventory().getMatrix()) && !allowed(event.getRecipe())) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (containsProtected(event.getInventory().getMatrix()) && !allowed(event.getRecipe())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (protectedItem(event.getItemInHand())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (protectedItem(event.getItem())) event.setUseItemInHand(Event.Result.DENY);
    }
}
