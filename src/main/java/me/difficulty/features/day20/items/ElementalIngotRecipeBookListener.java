
package me.difficulty.features.day20.items;

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;

import me.difficulty.core.day.DayManager;
import me.difficulty.features.day17.items.Day17Material;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;

import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.function.Predicate;

public final class ElementalIngotRecipeBookListener
        implements Listener {

    private static final int REQUIRED_DAY = 20;

    private final DayManager dayManager;
    private final Day17Material titanium;
    private final ElementalIngot elementalIngot;
    private final NamespacedKey recipeKey;

    private final ItemStack titaniumTemplate;

    public ElementalIngotRecipeBookListener(
            DayManager dayManager,
            Day17Material titanium,
            ElementalIngot elementalIngot,
            NamespacedKey recipeKey
    ) {

        this.dayManager = dayManager;
        this.titanium = titanium;
        this.elementalIngot = elementalIngot;
        this.recipeKey = recipeKey;

        titaniumTemplate = titanium.createItem();
    }

    /*
     * ========================================
     * CLIC EN EL LIBRO DE RECETAS
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRecipeBookClick(
            PlayerRecipeBookClickEvent event
    ) {

        if (!recipeKey.equals(event.getRecipe())) {
            return;
        }

        /*
         * Nosotros colocamos las cantidades
         * especiales de cada ingrediente.
         */
        event.setCancelled(true);

        if (!dayManager.isAtLeast(REQUIRED_DAY)) {
            return;
        }

        Player player = event.getPlayer();

        if (player.getOpenInventory().getType()
                != InventoryType.WORKBENCH) {
            return;
        }

        if (!(player.getOpenInventory()
                .getTopInventory()
                instanceof CraftingInventory crafting)) {
            return;
        }

        ItemStack[] matrix = crafting.getMatrix();

        if (matrix.length != 9) {
            return;
        }

        /*
         * No modificar una mesa ocupada.
         */
        for (ItemStack item : matrix) {

            if (item != null
                    && !item.getType().isAir()) {
                return;
            }
        }

        PlayerInventory inventory =
                player.getInventory();

        ItemStack[] storage =
                inventory.getStorageContents();

        /*
         * ========================================
         * COMPROBAR TOTALES
         * ========================================
         */
        if (count(
                storage,
                item -> isVanilla(item, Material.IRON_INGOT)
        ) < 128
                || count(
                storage,
                item -> isVanilla(item, Material.COPPER_INGOT)
        ) < 128
                || count(
                storage,
                item -> isVanilla(item, Material.GOLD_INGOT)
        ) < 128
                || count(
                storage,
                item -> isVanilla(item, Material.NETHERITE_INGOT)
        ) < 1
                || count(
                storage,
                item -> isVanilla(item, Material.DIAMOND)
        ) < 10
                || count(
                storage,
                this::isTitanium
        ) < 20) {

            player.sendMessage(
                    "§cNo tienes todos los materiales "
                            + "para fabricar el Lingote Elemental."
            );

            return;
        }

        /*
         * ========================================
         * COPIAR INVENTARIO
         * ========================================
         */
        ItemStack[] copy =
                new ItemStack[storage.length];

        for (int i = 0; i < storage.length; i++) {

            copy[i] = storage[i] == null
                    ? null
                    : storage[i].clone();
        }

        /*
         * ========================================
         * DESCONTAR EN LA COPIA
         * ========================================
         */
        if (!take(
                copy,
                item -> isVanilla(item, Material.IRON_INGOT),
                128
        )) {
            return;
        }

        if (!take(
                copy,
                item -> isVanilla(item, Material.COPPER_INGOT),
                128
        )) {
            return;
        }

        if (!take(
                copy,
                item -> isVanilla(item, Material.GOLD_INGOT),
                128
        )) {
            return;
        }

        if (!take(
                copy,
                item -> isVanilla(item, Material.NETHERITE_INGOT),
                1
        )) {
            return;
        }

        if (!take(
                copy,
                item -> isVanilla(item, Material.DIAMOND),
                10
        )) {
            return;
        }

        if (!take(
                copy,
                this::isTitanium,
                20
        )) {
            return;
        }

        /*
         * ========================================
         * CONSTRUIR RECETA COMPLETA
         * ========================================
         */
        ItemStack[] newMatrix = {

                new ItemStack(Material.IRON_INGOT, 64),
                new ItemStack(Material.COPPER_INGOT, 64),
                new ItemStack(Material.GOLD_INGOT, 64),

                new ItemStack(Material.NETHERITE_INGOT, 1),
                titaniumStack(20),
                new ItemStack(Material.DIAMOND, 10),

                new ItemStack(Material.GOLD_INGOT, 64),
                new ItemStack(Material.COPPER_INGOT, 64),
                new ItemStack(Material.IRON_INGOT, 64)
        };

        /*
         * Tanto clic normal como Shift + clic
         * colocan una sola fabricacion.
         */
        inventory.setStorageContents(copy);

        crafting.setMatrix(newMatrix);

        crafting.setResult(
                elementalIngot.createItem()
        );
    }

    private ItemStack titaniumStack(int amount) {

        ItemStack item = titaniumTemplate.clone();
        item.setAmount(amount);
        return item;
    }

    private boolean isVanilla(
            ItemStack item,
            Material material
    ) {

        return item != null
                && item.isSimilar(new ItemStack(material));
    }

    private boolean isTitanium(ItemStack item) {

        return item != null
                && titanium.matches(item)
                && item.isSimilar(titaniumTemplate);
    }

    private int count(
            ItemStack[] contents,
            Predicate<ItemStack> predicate
    ) {

        int total = 0;

        for (ItemStack item : contents) {

            if (item != null
                    && predicate.test(item)) {

                total += item.getAmount();
            }
        }

        return total;
    }

    private boolean take(
            ItemStack[] contents,
            Predicate<ItemStack> predicate,
            int amount
    ) {

        int remaining = amount;

        for (int i = 0;
             i < contents.length && remaining > 0;
             i++) {

            ItemStack item = contents[i];

            if (item == null
                    || !predicate.test(item)) {
                continue;
            }

            int removed = Math.min(
                    remaining,
                    item.getAmount()
            );

            int left = item.getAmount() - removed;

            if (left <= 0) {
                contents[i] = null;
            } else {
                item.setAmount(left);
            }

            remaining -= removed;
        }

        return remaining == 0;
    }
}
