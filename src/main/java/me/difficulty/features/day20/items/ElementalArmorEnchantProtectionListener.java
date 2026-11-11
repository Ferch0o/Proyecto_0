
package me.difficulty.features.day20.items;

import org.bukkit.enchantments.Enchantment;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class ElementalArmorEnchantProtectionListener
        implements Listener {

    private final Collection<ElementalArmorPiece> pieces;

    public ElementalArmorEnchantProtectionListener(
            ElementalArmorPiece helmet,
            ElementalArmorPiece chestplate,
            ElementalArmorPiece leggings,
            ElementalArmorPiece boots
    ) {

        pieces = List.of(
                helmet,
                chestplate,
                leggings,
                boots
        );
    }

    /*
     * ========================================
     * IDENTIFICAR ARMADURA ELEMENTAL
     * ========================================
     */
    private boolean isElementalArmor(
            ItemStack item
    ) {

        if (item == null) {
            return false;
        }

        for (ElementalArmorPiece piece : pieces) {

            if (piece.isItem(item)) {
                return true;
            }
        }

        return false;
    }

    /*
     * ========================================
     * ENCANTAMIENTOS PROHIBIDOS
     * ========================================
     */
    private boolean hasForbiddenEnchant(
            ItemStack item
    ) {

        return item != null
                && (item.containsEnchantment(
                Enchantment.MENDING
        )
                || item.containsEnchantment(
                Enchantment.UNBREAKING
        ));
    }

    /*
     * ========================================
     * YUNQUE - PREPARAR RESULTADO
     * ========================================
     *
     * Impedir combinar Armadura Elemental
     * con libros que agreguen Reparación
     * o Irrompibilidad.
     *
     * Otros encantamientos son permitidos.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareAnvil(
            PrepareAnvilEvent event
    ) {

        Inventory inventory =
                event.getInventory();

        ItemStack first = inventory.getItem(0);
        ItemStack second = inventory.getItem(1);

        if (!isElementalArmor(first)
                && !isElementalArmor(second)) {
            return;
        }

        if (hasForbiddenEnchant(event.getResult())) {

            /*
             * No mostrar resultado:
             * no se consumen niveles ni libro.
             */
            event.setResult(null);
        }
    }

    /*
     * ========================================
     * SEGUNDA VALIDACION DEL YUNQUE
     * ========================================
     *
     * Evitar retirar un resultado prohibido
     * si otro plugin cambia la vista.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onAnvilClick(
            InventoryClickEvent event
    ) {

        if (event.getView().getType()
                != InventoryType.ANVIL) {
            return;
        }

        if (event.getClickedInventory()
                != event.getView().getTopInventory()) {
            return;
        }

        /*
         * En el yunque:
         * 0 = primer material.
         * 1 = segundo material.
         * 2 = resultado.
         */
        if (event.getRawSlot() != 2) {
            return;
        }

        Inventory inventory =
                event.getView().getTopInventory();

        if (!isElementalArmor(
                inventory.getItem(0)
        ) && !isElementalArmor(
                inventory.getItem(1)
        )) {
            return;
        }

        if (hasForbiddenEnchant(
                event.getCurrentItem()
        )) {

            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * MESA DE ENCANTAMIENTOS
     * ========================================
     *
     * Impedir que la mesa agregue
     * Irrompibilidad a una pieza elemental.
     *
     * Reparación tampoco será aceptada.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onEnchant(
            EnchantItemEvent event
    ) {

        if (!isElementalArmor(event.getItem())) {
            return;
        }

        Map<Enchantment, Integer> enchants =
                event.getEnchantsToAdd();

        /*
         * Retirar únicamente los dos
         * encantamientos prohibidos.
         */
        enchants.remove(Enchantment.MENDING);
        enchants.remove(Enchantment.UNBREAKING);

        /*
         * Si la mesa solamente ofrecía
         * encantamientos prohibidos,
         * cancelar para no gastar niveles.
         */
        if (enchants.isEmpty()) {

            event.setCancelled(true);
        }
    }
}