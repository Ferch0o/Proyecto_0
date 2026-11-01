
package me.difficulty.features.day17.curse;

import io.papermc.paper.event.player.PlayerPickItemEvent;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import org.bukkit.entity.Player;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public final class CurseInventoryListener implements Listener {

    private final JavaPlugin plugin;

    private final CurseInventoryService inventoryService;

    public CurseInventoryListener(
            JavaPlugin plugin,
            CurseInventoryService inventoryService
    ) {
        this.plugin = plugin;
        this.inventoryService = inventoryService;
    }

    /*
     * ========================================
     * PROTEGER RANURAS BLOQUEADAS
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Set<Integer> blocked =
                inventoryService.blockedSlots(player);

        if (blocked.isEmpty()) {
            return;
        }

        // No mover ni recoger los Structure Void.
        if (inventoryService.isMarker(event.getCurrentItem())
                || inventoryService.isMarker(event.getCursor())) {

            event.setCancelled(true);
            return;
        }

        Inventory clicked = event.getClickedInventory();

        // Proteger las posiciones bloqueadas.
        if (clicked instanceof PlayerInventory
                && blocked.contains(event.getSlot())) {

            event.setCancelled(true);
            return;
        }

        // Impedir mover objetos a un slot bloqueado
        // utilizando las teclas numéricas.
        int hotbar = event.getHotbarButton();

        if (hotbar >= 0 && blocked.contains(hotbar)) {
            event.setCancelled(true);
            return;
        }

        // No intercambiar un marcador con
        // la mano secundaria.
        if (event.getClick() == ClickType.SWAP_OFFHAND
                && blocked.contains(
                player.getInventory().getHeldItemSlot()
        )) {

            event.setCancelled(true);
        }

        /*
         * No bloqueamos todos los dobles clics.
         *
         * Los marcadores ya están protegidos
         * por su identificación personalizada.
         */
    }

    /*
     * ========================================
     * ARRASTRAR OBJETOS
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onDrag(InventoryDragEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Set<Integer> blocked =
                inventoryService.blockedSlots(player);

        if (blocked.isEmpty()) {
            return;
        }

        if (inventoryService.isMarker(event.getOldCursor())) {
            event.setCancelled(true);
            return;
        }

        for (int raw : event.getRawSlots()) {

            if (raw < event.getView()
                    .getTopInventory().getSize()) {
                continue;
            }

            int local = event.getView().convertSlot(raw);

            if (blocked.contains(local)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /*
     * ========================================
     * CLIC CENTRAL (PICK BLOCK)
     * ========================================
     *
     * El clic central puede intercambiar
     * objetos entre inventario y hotbar.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onPickItem(PlayerPickItemEvent event) {

        Set<Integer> blocked =
                inventoryService.blockedSlots(
                        event.getPlayer()
                );

        if (blocked.isEmpty()) {
            return;
        }

        int target = event.getTargetSlot();
        int source = event.getSourceSlot();

        if (blocked.contains(target)
                || (source >= 0 && blocked.contains(source))) {

            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * IMPEDIR USAR EL STRUCTURE VOID
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onUseMarker(PlayerInteractEvent event) {

        if (!inventoryService.isMarker(
                event.getItem()
        )) {
            return;
        }

        /*
         * Impedimos que Minecraft utilice
         * nuestro Structure Void.
         */
        event.setUseItemInHand(
                Event.Result.DENY
        );

        /*
         * Si hace clic derecho sobre
         * un bloque, también impedimos
         * esa interacción.
         */
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            event.setUseInteractedBlock(
                    Event.Result.DENY
            );
        }

        /*
         * El cliente puede mostrar
         * temporalmente que desapareció.
         *
         * Forzamos una sincronización
         * después del evento.
         */
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                || event.getAction() == Action.RIGHT_CLICK_AIR) {

            refreshMarker(event.getPlayer());
        }
    }

    /*
     * ========================================
     * IMPEDIR COLOCAR EL MARCADOR
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onPlaceMarker(BlockPlaceEvent event) {

        if (!inventoryService.isMarker(
                event.getItemInHand()
        )) {
            return;
        }

        event.setCancelled(true);

        refreshMarker(event.getPlayer());
    }

    /*
     * ========================================
     * INTERCAMBIO CON MANO SECUNDARIA
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onSwap(PlayerSwapHandItemsEvent event) {

        Player player = event.getPlayer();

        if (inventoryService.blockedSlots(player)
                .contains(
                        player.getInventory().getHeldItemSlot()
                )
                || inventoryService.isMarker(
                event.getMainHandItem()
        )
                || inventoryService.isMarker(
                event.getOffHandItem()
        )) {

            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * IMPEDIR DROPEAR STRUCTURE VOID
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onDrop(PlayerDropItemEvent event) {

        ItemStack item =
                event.getItemDrop().getItemStack();

        if (inventoryService.isMarker(item)) {
            event.setCancelled(true);
        }
    }


    /*
     * ========================================
     * RESTAURAR APARIENCIA DEL MARCADOR
     * ========================================
     */
    private void refreshMarker(Player player) {

        plugin.getServer()
                .getScheduler()
                .runTask(plugin, () -> {

                    if (!player.isOnline()) {
                        return;
                    }

                    /*
                     * Verificar y restaurar los
                     * marcadores si hace falta.
                     */
                    inventoryService.synchronize(player);

                    /*
                     * Sincronizar inventario
                     * del servidor con el cliente.
                     */
                    player.updateInventory();
                });
    }
}
