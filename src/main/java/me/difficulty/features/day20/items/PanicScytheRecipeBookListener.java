
package me.difficulty.features.day20.items;

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;

import me.difficulty.core.day.DayManager;

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

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Predicate;

public final class PanicScytheRecipeBookListener
        implements Listener {

    private static final int REQUIRED_DAY = 20;

    private final DayManager dayManager;
    private final ElementalGold elementalGold;
    private final MultiPearls multiPearls;
    private final PanicScythe panicScythe;

    private final NamespacedKey recipeKey;
    private final NamespacedKey customIdKey;

    private final ItemStack elementalGoldTemplate;
    private final ItemStack multiPearlsTemplate;

    public PanicScytheRecipeBookListener(
            JavaPlugin plugin,
            DayManager dayManager,
            ElementalGold elementalGold,
            MultiPearls multiPearls,
            PanicScythe panicScythe,
            NamespacedKey recipeKey
    ) {

        this.dayManager = dayManager;
        this.elementalGold = elementalGold;
        this.multiPearls = multiPearls;
        this.panicScythe = panicScythe;
        this.recipeKey = recipeKey;

        customIdKey = new NamespacedKey(
                plugin,
                "custom_item_id"
        );

        elementalGoldTemplate =
                elementalGold.createItem();

        multiPearlsTemplate =
                multiPearls.createItem();
    }

    /*
     * ========================================
     * LIBRO DE RECETAS
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRecipeBookClick(
            PlayerRecipeBookClickEvent event
    ) {

        if (!recipeKey.equals(event.getRecipe())) {
            return;
        }

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

        ItemStack[] matrix =
                crafting.getMatrix();

        if (matrix.length != 9) {
            return;
        }

        /*
         * Solo preparar con la mesa vacía.
         * No sobrescribir materiales.
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
         * REQUISITOS TOTALES
         * ========================================
         */
        if (count(storage, this::isGoldBlock) < 256
                || count(
                storage,
                this::isElementalGold
        ) < 20
                || count(
                storage,
                this::isMultiPearls
        ) < 1
                || count(
                storage,
                item -> isVanillaTool(
                        item,
                        Material.DIAMOND_SWORD
                )
        ) < 1
                || count(
                storage,
                item -> isVanillaTool(
                        item,
                        Material.DIAMOND_AXE
                )
        ) < 1) {

            player.sendMessage(
                    "§cNo tienes todos los materiales "
                            + "para fabricar la Guadaña del Pánico."
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
         * TRANSFERIR MATERIALES
         * ========================================
         */
        if (!take(
                copy,
                this::isGoldBlock,
                256
        )) {
            return;
        }

        if (!take(
                copy,
                this::isElementalGold,
                20
        )) {
            return;
        }

        if (!take(
                copy,
                this::isMultiPearls,
                1
        )) {
            return;
        }

        ItemStack sword = takeOne(
                copy,
                item -> isVanillaTool(
                        item,
                        Material.DIAMOND_SWORD
                )
        );

        ItemStack axe = takeOne(
                copy,
                item -> isVanillaTool(
                        item,
                        Material.DIAMOND_AXE
                )
        );

        if (sword == null || axe == null) {
            return;
        }

        /*
         * ========================================
         * CONSTRUIR MATRIZ
         * ========================================
         */
        ItemStack[] newMatrix = {

                new ItemStack(Material.GOLD_BLOCK, 64),
                sword,
                new ItemStack(Material.GOLD_BLOCK, 64),

                stack(elementalGoldTemplate, 10),
                stack(multiPearlsTemplate, 1),
                stack(elementalGoldTemplate, 10),

                new ItemStack(Material.GOLD_BLOCK, 64),
                axe,
                new ItemStack(Material.GOLD_BLOCK, 64)
        };

        /*
         * Clic normal y Shift + clic:
         * una sola fabricación.
         */
        inventory.setStorageContents(copy);

        crafting.setMatrix(newMatrix);

        crafting.setResult(
                panicScythe.createItem()
        );
    }

    private ItemStack stack(
            ItemStack template,
            int amount
    ) {

        ItemStack item = template.clone();
        item.setAmount(amount);
        return item;
    }

    private boolean isGoldBlock(
            ItemStack item
    ) {

        return item != null
                && item.isSimilar(
                new ItemStack(Material.GOLD_BLOCK)
        );
    }

    private boolean isElementalGold(
            ItemStack item
    ) {

        return item != null
                && elementalGold.isItem(item)
                && item.isSimilar(elementalGoldTemplate);
    }

    private boolean isMultiPearls(
            ItemStack item
    ) {

        return item != null
                && multiPearls.isItem(item)
                && item.isSimilar(multiPearlsTemplate);
    }

    private boolean isVanillaTool(
            ItemStack item,
            Material type
    ) {

        if (item == null
                || item.getType() != type) {
            return false;
        }

        return !item.hasItemMeta()
                || !item.getItemMeta()
                .getPersistentDataContainer()
                .has(
                        customIdKey,
                        PersistentDataType.STRING
                );
    }

    private int count(
            ItemStack[] storage,
            Predicate<ItemStack> predicate
    ) {

        int total = 0;

        for (ItemStack item : storage) {

            if (item != null
                    && predicate.test(item)) {

                total += item.getAmount();
            }
        }

        return total;
    }

    private boolean take(
            ItemStack[] storage,
            Predicate<ItemStack> predicate,
            int amount
    ) {

        int remaining = amount;

        for (int i = 0;
             i < storage.length && remaining > 0;
             i++) {

            ItemStack item = storage[i];

            if (item == null
                    || !predicate.test(item)) {
                continue;
            }

            int removed = Math.min(
                    remaining,
                    item.getAmount()
            );

            int left =
                    item.getAmount() - removed;

            if (left <= 0) {

                storage[i] = null;

            } else {

                item.setAmount(left);
            }

            remaining -= removed;
        }

        return remaining == 0;
    }

    private ItemStack takeOne(
            ItemStack[] storage,
            Predicate<ItemStack> predicate
    ) {

        for (int i = 0; i < storage.length; i++) {

            ItemStack item = storage[i];

            if (item == null
                    || !predicate.test(item)
                    || item.getAmount() < 1) {
                continue;
            }

            ItemStack extracted = item.clone();
            extracted.setAmount(1);

            int left = item.getAmount() - 1;

            if (left <= 0) {
                storage[i] = null;
            } else {
                item.setAmount(left);
            }

            return extracted;
        }

        return null;
    }
}
