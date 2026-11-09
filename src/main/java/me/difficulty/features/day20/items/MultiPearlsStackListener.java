
package me.difficulty.features.day20.items;

import me.difficulty.core.day.DayManager;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class MultiPearlsStackListener
        implements Listener {

    /*
     * ========================================
     * DIA 20 - APILAMIENTO MULTI PEARLS
     * ========================================
     *
     * Controla los clics izquierdos normales.
     *
     * Shift + clic conserva el sistema vanilla.
     */

    private static final int REQUIRED_DAY = 20;

    private final JavaPlugin plugin;
    private final DayManager dayManager;

    private final ElementalPearl elementalPearl;
    private final MultiPearls multiPearls;

    /*
     * Evitar procesar varios clics
     * simultaneos del mismo jugador.
     */
    private final Set<UUID> pending =
            new HashSet<>();

    public MultiPearlsStackListener(
            JavaPlugin plugin,
            DayManager dayManager,
            ElementalPearl elementalPearl,
            MultiPearls multiPearls
    ) {
        this.plugin = plugin;
        this.dayManager = dayManager;
        this.elementalPearl = elementalPearl;
        this.multiPearls = multiPearls;
    }

    /*
     * ========================================
     * CLIC IZQUIERDO EN RESULTADO
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onResultClick(
            InventoryClickEvent event
    ) {

        if (!dayManager.isAtLeast(REQUIRED_DAY)) {
            return;
        }

        /*
         * Solo clic izquierdo normal.
         *
         * No modificar Shift + clic.
         */
        if (event.getClick() != ClickType.LEFT) {
            return;
        }

        /*
         * Solo mesa de crafteo 3x3.
         */
        if (event.getView().getType()
                != InventoryType.WORKBENCH) {
            return;
        }

        /*
         * La ranura 0 es el resultado.
         */
        if (event.getRawSlot() != 0) {
            return;
        }

        if (!(event.getWhoClicked()
                instanceof Player player)) {
            return;
        }

        if (!(event.getView().getTopInventory()
                instanceof CraftingInventory inventory)) {
            return;
        }

        /*
         * Solo nuestra receta.
         */
        if (!isValidMatrix(inventory.getMatrix())) {
            return;
        }

        /*
         * Comprobar el cursor.
         */
        ItemStack cursor = event.getCursor();

        if (cursor != null
                && !cursor.getType().isAir()) {

            /*
             * No mezclar Multi Pearls con
             * objetos distintos.
             */
            if (!multiPearls.isItem(cursor)) {
                event.setCancelled(true);
                return;
            }

            /*
             * Respetar el maximo
             * permitido por el objeto.
             */
            if (cursor.getAmount()
                    >= cursor.getMaxStackSize()) {

                event.setCancelled(true);
                return;
            }
        }

        /*
         * Cancelar el procesamiento vanilla.
         *
         * Nosotros entregaremos el resultado
         * y consumiremos los ingredientes.
         */
        event.setCancelled(true);

        UUID playerId = player.getUniqueId();

        if (!pending.add(playerId)) {
            return;
        }

        /*
         * Procesar en el siguiente tick,
         * evitando conflictos con la
         * actualizacion del inventario.
         */
        plugin.getServer().getScheduler().runTask(
                plugin,
                () -> {

                    try {
                        craftOne(
                                player,
                                inventory
                        );
                    } finally {
                        pending.remove(playerId);
                    }
                }
        );
    }

    /*
     * ========================================
     * VALIDAR RECETA
     * ========================================
     *
     * P P P
     * P C P
     * P P P
     *
     * P = Perla Elemental autentica
     * C = Cuerda (LEAD)
     */
    private boolean isValidMatrix(
            ItemStack[] matrix
    ) {

        if (matrix.length != 9) {
            return false;
        }

        for (int slot = 0; slot < 9; slot++) {

            ItemStack item = matrix[slot];

            if (item == null
                    || item.getType().isAir()) {
                return false;
            }

            if (slot == 4) {

                /*
                 * La cuerda debe ser vanilla.
                 */
                if (item.getType() != Material.LEAD
                        || item.hasItemMeta()
                        && item.getItemMeta()
                        .getPersistentDataContainer()
                        .has(
                                new org.bukkit.NamespacedKey(
                                        plugin,
                                        "custom_item_id"
                                ),
                                org.bukkit.persistence
                                        .PersistentDataType.STRING
                        )) {
                    return false;
                }

            } else {

                /*
                 * Ocho Perlas Elementales.
                 *
                 * No aceptar perlas vanilla.
                 */
                if (!elementalPearl.isItem(item)) {
                    return false;
                }
            }
        }

        return true;
    }

    /*
     * ========================================
     * FABRICAR UNA MULTI PEARLS
     * ========================================
     */
    private void craftOne(
            Player player,
            CraftingInventory inventory
    ) {

        if (!player.isOnline()
                || !dayManager.isAtLeast(REQUIRED_DAY)) {
            return;
        }

        /*
         * Evitar modificar una mesa
         * que el jugador ya cerro.
         */
        InventoryView view =
                player.getOpenInventory();

        if (view.getType() != InventoryType.WORKBENCH
                || view.getTopInventory() != inventory) {
            return;
        }

        /*
         * Verificar nuevamente los ingredientes.
         */
        ItemStack[] matrix = inventory.getMatrix();

        if (!isValidMatrix(matrix)) {
            return;
        }

        /*
         * Comprobar nuevamente el cursor.
         */
        ItemStack cursor =
                player.getItemOnCursor();

        ItemStack result;

        if (cursor == null
                || cursor.getType().isAir()) {

            result = multiPearls.createItem();
            result.setAmount(1);

        } else {

            if (!multiPearls.isItem(cursor)) {
                return;
            }

            if (cursor.getAmount()
                    >= cursor.getMaxStackSize()) {
                return;
            }

            /*
             * Acumular sobre el mismo objeto.
             */
            result = cursor.clone();
            result.setAmount(
                    cursor.getAmount() + 1
            );
        }

        /*
         * ========================================
         * CONSUMIR INGREDIENTES
         * ========================================
         *
         * Una unidad por casilla.
         * 8 Perlas Elementales + 1 cuerda.
         */
        for (int slot = 0; slot < 9; slot++) {

            ItemStack ingredient = matrix[slot];

            if (ingredient.getAmount() <= 1) {

                matrix[slot] = null;

            } else {

                ItemStack reduced =
                        ingredient.clone();

                reduced.setAmount(
                        ingredient.getAmount() - 1
                );

                matrix[slot] = reduced;
            }
        }

        /*
         * Actualizar ingredientes.
         */
        inventory.setMatrix(matrix);

        /*
         * Entregar resultado al cursor.
         */
        player.setItemOnCursor(result);

        /*
         * Sincronizar con el cliente.
         */
        player.updateInventory();
    }
}