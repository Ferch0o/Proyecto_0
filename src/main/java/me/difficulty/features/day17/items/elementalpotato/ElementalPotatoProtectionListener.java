
package me.difficulty.features.day17.items.elementalpotato;

import org.bukkit.Material;
import org.bukkit.block.Crafter;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.CrafterCraftEvent;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class ElementalPotatoProtectionListener
        implements Listener {

    private final ElementalPotato potato;

    public ElementalPotatoProtectionListener(
            ElementalPotato potato
    ) {
        this.potato = potato;
    }

    private boolean isPotato(ItemStack item) {
        return potato.isElementalPotato(item);
    }

    private boolean containsPotato(ItemStack[] contents) {

        for (ItemStack item : contents) {
            if (isPotato(item)) {
                return true;
            }
        }

        return false;
    }

    private boolean isCookingInventory(Inventory inventory) {

        if (inventory == null) {
            return false;
        }

        InventoryType type = inventory.getType();

        return type == InventoryType.FURNACE
                || type == InventoryType.SMOKER
                || type == InventoryType.BLAST_FURNACE;
    }

    /*
     * ========================================
     * IMPEDIR PLANTAR Y COMPOSTAR
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {

        if (!isPotato(event.getItem())) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.getClickedBlock() == null) {
            return;
        }

        Material block = event.getClickedBlock().getType();

        if (block == Material.FARMLAND
                || block == Material.COMPOSTER) {

            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
        }
    }

    /*
     * SEGUNDA PROTECCIÓN CONTRA PLANTACIÓN
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onPlace(BlockPlaceEvent event) {

        if (isPotato(event.getItemInHand())) {
            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * IMPEDIR COCCIÓN
     * Incluye hornos y hogueras.
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCook(BlockCookEvent event) {

        if (isPotato(event.getSource())) {
            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * IMPEDIR INTRODUCIRLA EN HORNOS
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onInventoryClick(InventoryClickEvent event) {

        Inventory top = event.getView().getTopInventory();

        if (!isCookingInventory(top)) {
            return;
        }

        // Colocarla con el cursor.
        if (event.getClickedInventory() == top
                && isPotato(event.getCursor())) {

            event.setCancelled(true);
            return;
        }

        // Moverla con Shift + clic.
        if (event.isShiftClick()
                && isPotato(event.getCurrentItem())) {

            event.setCancelled(true);
            return;
        }

        // Introducirla con las teclas 1-9.
        int hotbarSlot = event.getHotbarButton();

        if (event.getClickedInventory() == top
                && hotbarSlot >= 0
                && isPotato(
                event.getWhoClicked()
                        .getInventory()
                        .getItem(hotbarSlot)
        )) {

            event.setCancelled(true);
            return;
        }

        // Introducirla desde la mano secundaria.
        if (event.getClickedInventory() == top
                && event.getClick() == ClickType.SWAP_OFFHAND
                && isPotato(
                event.getWhoClicked()
                        .getInventory()
                        .getItemInOffHand()
        )) {

            event.setCancelled(true);
        }
    }

    /*
     * IMPEDIR ARRASTRARLA A UN HORNO
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onDrag(InventoryDragEvent event) {

        if (!isCookingInventory(
                event.getView().getTopInventory()
        )) {
            return;
        }

        if (!isPotato(event.getOldCursor())) {
            return;
        }

        int topSize =
                event.getView().getTopInventory().getSize();

        for (int rawSlot : event.getRawSlots()) {

            if (rawSlot < topSize) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /*
     * ========================================
     * TOLVAS
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onHopper(InventoryMoveItemEvent event) {

        if (!isPotato(event.getItem())) {
            return;
        }

        Inventory destination = event.getDestination();

        if (isCookingInventory(destination)
                || destination.getType()
                == InventoryType.COMPOSTER) {

            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * BLOQUEAR OTRAS RECETAS
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareCraft(
            PrepareItemCraftEvent event
    ) {

        if (containsPotato(
                event.getInventory().getMatrix()
        )) {

            event.getInventory().setResult(null);
        }
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCraft(CraftItemEvent event) {

        if (containsPotato(
                event.getInventory().getMatrix()
        )) {

            event.setCancelled(true);
        }
    }

    /*
     * IMPEDIR CRAFTER AUTOMÁTICO
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCrafter(CrafterCraftEvent event) {

        if (event.getBlock().getState()
                instanceof Crafter crafter) {

            if (containsPotato(
                    crafter.getInventory().getContents()
            )) {

                event.setCancelled(true);
            }
        }
    }
}
