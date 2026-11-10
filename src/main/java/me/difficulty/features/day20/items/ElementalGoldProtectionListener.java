
package me.difficulty.features.day20.items;

import me.difficulty.core.items.ItemIds;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Crafter;
import org.bukkit.entity.IronGolem;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.block.CrafterCraftEvent;

import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;

import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.EquipmentSlot;

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class ElementalGoldProtectionListener
        implements Listener {

    private final NamespacedKey itemIdKey;
    private final NamespacedKey panicScytheRecipeKey;

    public ElementalGoldProtectionListener(
            JavaPlugin plugin
    ) {
        itemIdKey = new NamespacedKey(
                plugin,
                "custom_item_id"
        );

        panicScytheRecipeKey = new NamespacedKey(
                plugin,
                "panic_scythe_recipe"
        );
    }

    /*
     * ========================================
     * IDENTIFICAR ORO ELEMENTAL
     * ========================================
     *
     * No dependemos de GOLD_INGOT
     * ni de IRON_INGOT.
     *
     * Identificamos exclusivamente
     * nuestro objeto personalizado.
     */
    private boolean isElementalGold(ItemStack item) {

        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return false;
        }

        String id = meta.getPersistentDataContainer()
                .get(
                        itemIdKey,
                        PersistentDataType.STRING
                );

        return ItemIds.ELEMENTAL_GOLD.equals(id);
    }

    /*
     * ========================================
     * BUSCAR ORO ELEMENTAL
     * ========================================
     */
    private boolean containsElementalGold(
            ItemStack[] contents
    ) {

        for (ItemStack item : contents) {

            if (isElementalGold(item)) {
                return true;
            }
        }

        return false;
    }

    /*
     * ========================================
     * INVENTARIOS DE PROCESAMIENTO
     * ========================================
     *
     * Se permite guardar el objeto en
     * cofres, barriles, shulkers, etc.
     *
     * Se impide introducirlo en
     * mecanismos que puedan consumirlo
     * o utilizarlo como ingrediente.
     */
    private boolean isProcessingInventory(
            Inventory inventory
    ) {

        if (inventory == null) {
            return false;
        }

        return switch (inventory.getType()) {

            case ANVIL,
                 SMITHING,
                 GRINDSTONE,
                 BEACON,
                 MERCHANT,
                 CRAFTER,
                 FURNACE,
                 BLAST_FURNACE,
                 SMOKER,
                 BREWING,
                 STONECUTTER,
                 CARTOGRAPHY,
                 LOOM -> true;

            default -> false;
        };
    }

    /*
     * ========================================
     * PROTEGER CRAFTEOS
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareCraft(
            PrepareItemCraftEvent event
    ) {

        if (!containsElementalGold(
                event.getInventory().getMatrix()
        )) {
            return;
        }

        /*
         * La Guadaña del Pánico sí puede
         * utilizar Oro Elemental.
         */
        if (isAllowedRecipe(event.getRecipe())) {
            return;
        }

        /*
         * Todas las demás recetas
         * siguen bloqueadas.
         */
        event.getInventory().setResult(null);
    }

    /*
     * ========================================
     * PROTEGER FABRICACION MANUAL
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCraft(
            CraftItemEvent event
    ) {

        if (!containsElementalGold(
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

        if (containsElementalGold(
                crafter.getInventory().getContents()
        )) {

            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * BLOQUEAR YUNQUE
     * ========================================
     *
     * Sin reparaciones ni cambios
     * de nombre mediante yunque.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareAnvil(
            PrepareAnvilEvent event
    ) {

        if (containsElementalGold(
                event.getInventory().getContents()
        )) {

            event.setResult(null);
        }
    }

    /*
     * ========================================
     * BLOQUEAR MESA DE HERRERÍA
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareSmithing(
            PrepareSmithingEvent event
    ) {

        if (containsElementalGold(
                event.getInventory().getContents()
        )) {

            event.setResult(null);
        }
    }

    /*
     * ========================================
     * BLOQUEAR PIEDRA DE AFILAR
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareGrindstone(
            PrepareGrindstoneEvent event
    ) {

        if (containsElementalGold(
                event.getInventory().getContents()
        )) {

            event.setResult(null);
        }
    }

    /*
     * ========================================
     * BLOQUEAR INSERCIÓN EN MECANISMOS
     * ========================================
     *
     * Protege contra:
     * - Clic normal.
     * - Shift + clic.
     * - Teclas 1-9.
     * - Intercambio con mano secundaria.
     * - Retirada de resultados.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onInventoryClick(
            InventoryClickEvent event
    ) {

        Inventory top =
                event.getView().getTopInventory();

        if (!isProcessingInventory(top)) {
            return;
        }

        /*
         * Shift + clic desde el inventario
         * del jugador hacia el mecanismo.
         */
        if (event.isShiftClick()
                && event.getClickedInventory() != top
                && isElementalGold(
                event.getCurrentItem()
        )) {

            event.setCancelled(true);
            return;
        }

        if (event.getClickedInventory() != top) {
            return;
        }

        /*
         * Si hubiera Oro Elemental dentro,
         * no permitir recoger resultados
         * que puedan consumirlo.
         */
        if (event.getSlotType()
                == InventoryType.SlotType.RESULT
                && containsElementalGold(
                top.getContents()
        )) {

            event.setCancelled(true);
            return;
        }

        /*
         * Colocarlo usando el cursor.
         */
        if (isElementalGold(event.getCursor())) {

            event.setCancelled(true);
            return;
        }

        /*
         * Colocarlo mediante teclas 1-9.
         */
        int hotbarSlot = event.getHotbarButton();

        if (hotbarSlot >= 0
                && isElementalGold(
                event.getWhoClicked()
                        .getInventory()
                        .getItem(hotbarSlot)
        )) {

            event.setCancelled(true);
            return;
        }

        /*
         * Colocarlo con la tecla F.
         */
        if (event.getClick()
                == ClickType.SWAP_OFFHAND
                && isElementalGold(
                event.getWhoClicked()
                        .getInventory()
                        .getItemInOffHand()
        )) {

            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * BLOQUEAR ARRASTRAR EL OBJETO
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onInventoryDrag(
            InventoryDragEvent event
    ) {

        Inventory top =
                event.getView().getTopInventory();

        if (!isProcessingInventory(top)
                || !isElementalGold(
                event.getOldCursor()
        )) {
            return;
        }

        int topSize = top.getSize();

        for (int rawSlot : event.getRawSlots()) {

            if (rawSlot < topSize) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /*
     * ========================================
     * BLOQUEAR TOLVAS EN MECANISMOS
     * ========================================
     *
     * Una tolva puede transportar el
     * Oro Elemental entre cofres.
     *
     * No puede introducirlo en una
     * máquina de procesamiento.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onInventoryMove(
            InventoryMoveItemEvent event
    ) {

        if (!isElementalGold(event.getItem())) {
            return;
        }

        if (isProcessingInventory(
                event.getDestination()
        )) {

            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * BLOQUEAR COMBUSTIBLE
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onFurnaceBurn(
            FurnaceBurnEvent event
    ) {

        if (isElementalGold(event.getFuel())) {
            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * BLOQUEAR FUNDICIÓN
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onFurnaceSmelt(
            FurnaceSmeltEvent event
    ) {

        if (isElementalGold(event.getSource())) {
            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * BLOQUEAR COCCIÓN
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onBlockCook(
            BlockCookEvent event
    ) {

        if (isElementalGold(event.getSource())) {
            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * BLOQUEAR ELABORACIÓN DE POCIONES
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onBrew(
            BrewEvent event
    ) {

        if (isElementalGold(
                event.getContents().getIngredient()
        )) {

            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * BLOQUEAR USO DIRECTO
     * ========================================
     *
     * No interferimos con la apertura
     * de cofres y otros bloques.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(
            PlayerInteractEvent event
    ) {

        if (!isElementalGold(event.getItem())) {
            return;
        }

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR
                && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setUseItemInHand(
                Event.Result.DENY
        );
    }

    /*
     * ========================================
     * IMPEDIR REPARAR GÓLEMS DE HIERRO
     * ========================================
     *
     * El material base es IRON_INGOT.
     * No debe consumirse al reparar
     * un gólem vanilla.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onIronGolemInteract(
            PlayerInteractEntityEvent event
    ) {

        if (!(event.getRightClicked()
                instanceof IronGolem)) {
            return;
        }

        ItemStack held;

        if (event.getHand() == EquipmentSlot.HAND) {

            held = event.getPlayer()
                    .getInventory()
                    .getItemInMainHand();

        } else {

            held = event.getPlayer()
                    .getInventory()
                    .getItemInOffHand();
        }

        if (isElementalGold(held)) {
            event.setCancelled(true);
        }
    }


    /*
     * ========================================
     * RECETA AUTORIZADA PARA ORO ELEMENTAL
     * ========================================
     */
    private boolean isAllowedRecipe(
            Recipe recipe
    ) {

        return recipe instanceof Keyed keyed
                && panicScytheRecipeKey.equals(
                keyed.getKey()
        );
    }
}