
package me.difficulty.features.day20.items;

import me.difficulty.core.day.DayManager;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class PanicScytheRecipe
        implements Listener {

    private static final int REQUIRED_DAY = 20;

    /*
     * Bo  Ed  Bo
     * Oe  Mp  Oe
     * Bo  Hd  Bo
     */
    private static final int[] REQUIRED = {
            64, 1, 64,
            10, 1, 10,
            64, 1, 64
    };

    private final JavaPlugin plugin;
    private final DayManager dayManager;
    private final ElementalGold elementalGold;
    private final MultiPearls multiPearls;
    private final PanicScythe panicScythe;

    private final NamespacedKey recipeKey;
    private final NamespacedKey customIdKey;

    private final ItemStack goldTemplate;
    private final ItemStack pearlTemplate;

    private final Set<UUID> pending =
            new HashSet<>();

    public PanicScytheRecipe(
            JavaPlugin plugin,
            DayManager dayManager,
            ElementalGold elementalGold,
            MultiPearls multiPearls,
            PanicScythe panicScythe
    ) {

        this.plugin = plugin;
        this.dayManager = dayManager;
        this.elementalGold = elementalGold;
        this.multiPearls = multiPearls;
        this.panicScythe = panicScythe;

        recipeKey = new NamespacedKey(
                plugin,
                "panic_scythe_recipe"
        );

        customIdKey = new NamespacedKey(
                plugin,
                "custom_item_id"
        );

        goldTemplate = elementalGold.createItem();
        pearlTemplate = multiPearls.createItem();
    }

    public NamespacedKey getRecipeKey() {
        return recipeKey;
    }

    /*
     * ========================================
     * REGISTRAR RECETA
     * ========================================
     */
    public void register() {

        ShapedRecipe recipe = new ShapedRecipe(
                recipeKey,
                panicScythe.createItem()
        );

        recipe.shape(
                "BSB",
                "GPG",
                "BAB"
        );

        recipe.setIngredient(
                'B',
                Material.GOLD_BLOCK
        );

        recipe.setIngredient(
                'S',
                Material.DIAMOND_SWORD
        );

        recipe.setIngredient(
                'G',
                new RecipeChoice.ExactChoice(
                        goldTemplate
                )
        );

        recipe.setIngredient(
                'P',
                new RecipeChoice.ExactChoice(
                        pearlTemplate
                )
        );

        recipe.setIngredient(
                'A',
                Material.DIAMOND_AXE
        );

        plugin.getServer().addRecipe(recipe);
    }

    private boolean isOurRecipe(Recipe recipe) {

        return recipe instanceof Keyed keyed
                && recipeKey.equals(keyed.getKey());
    }

    /*
     * ========================================
     * INGREDIENTES
     * ========================================
     */
    private boolean isPlainGoldBlock(
            ItemStack item,
            int amount
    ) {

        return item != null
                && item.getAmount() >= amount
                && item.isSimilar(
                new ItemStack(Material.GOLD_BLOCK)
        );
    }

    private boolean isVanillaTool(
            ItemStack item,
            Material material
    ) {

        if (item == null
                || item.getType() != material
                || item.getAmount() < 1) {
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

    private boolean isElementalGold(
            ItemStack item,
            int amount
    ) {

        return item != null
                && item.getAmount() >= amount
                && elementalGold.isItem(item)
                && item.isSimilar(goldTemplate);
    }

    private boolean isMultiPearls(
            ItemStack item
    ) {

        return item != null
                && item.getAmount() >= 1
                && multiPearls.isItem(item)
                && item.isSimilar(pearlTemplate);
    }

    private boolean hasIngredients(
            ItemStack[] matrix
    ) {

        if (matrix == null
                || matrix.length != 9) {
            return false;
        }

        return isPlainGoldBlock(matrix[0], 64)
                && isVanillaTool(
                matrix[1],
                Material.DIAMOND_SWORD
        )
                && isPlainGoldBlock(matrix[2], 64)
                && isElementalGold(matrix[3], 10)
                && isMultiPearls(matrix[4])
                && isElementalGold(matrix[5], 10)
                && isPlainGoldBlock(matrix[6], 64)
                && isVanillaTool(
                matrix[7],
                Material.DIAMOND_AXE
        )
                && isPlainGoldBlock(matrix[8], 64);
    }

    /*
     * ========================================
     * MOSTRAR RESULTADO
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareCraft(
            PrepareItemCraftEvent event
    ) {

        if (!isOurRecipe(event.getRecipe())) {
            return;
        }

        if (!dayManager.isAtLeast(REQUIRED_DAY)
                || !hasIngredients(
                event.getInventory().getMatrix()
        )) {

            event.getInventory().setResult(null);
            return;
        }

        event.getInventory().setResult(
                panicScythe.createItem()
        );
    }

    /*
     * ========================================
     * FABRICAR GUADAÑA
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCraft(CraftItemEvent event) {

        if (!isOurRecipe(event.getRecipe())) {
            return;
        }

        /*
         * No dejar que Minecraft retire
         * una sola unidad por casilla.
         */
        event.setCancelled(true);

        if (!dayManager.isAtLeast(REQUIRED_DAY)) {
            return;
        }

        if (!(event.getWhoClicked()
                instanceof Player player)) {
            return;
        }

        ClickType click = event.getClick();

        boolean shift =
                click == ClickType.SHIFT_LEFT
                        || click == ClickType.SHIFT_RIGHT;

        boolean normal =
                click == ClickType.LEFT
                        || click == ClickType.RIGHT;

        if (!shift && !normal) {
            return;
        }

        CraftingInventory crafting =
                event.getInventory();

        if (!hasIngredients(crafting.getMatrix())) {
            return;
        }

        UUID uuid = player.getUniqueId();

        if (!pending.add(uuid)) {
            return;
        }

        plugin.getServer().getScheduler().runTask(
                plugin,
                () -> {

                    try {

                        if (!player.isOnline()
                                || !dayManager.isAtLeast(
                                REQUIRED_DAY
                        )) {
                            return;
                        }

                        if (player.getOpenInventory()
                                .getTopInventory() != crafting) {
                            return;
                        }

                        ItemStack[] matrix =
                                crafting.getMatrix();

                        if (!hasIngredients(matrix)) {
                            return;
                        }

                        ItemStack product =
                                panicScythe.createItem();

                        /*
                         * ========================================
                         * COMPROBAR DESTINO DEL RESULTADO
                         * ========================================
                         */
                        ItemStack[] storageCopy = null;

                        if (shift) {

                            storageCopy = player.getInventory()
                                    .getStorageContents();

                            for (int i = 0;
                                 i < storageCopy.length;
                                 i++) {

                                if (storageCopy[i] != null) {
                                    storageCopy[i] =
                                            storageCopy[i].clone();
                                }
                            }

                            boolean inserted = false;

                            for (int i = 0;
                                 i < storageCopy.length;
                                 i++) {

                                if (storageCopy[i] == null
                                        || storageCopy[i]
                                        .getType()
                                        .isAir()) {

                                    storageCopy[i] =
                                            product.clone();

                                    inserted = true;
                                    break;
                                }
                            }

                            if (!inserted) {

                                player.sendMessage(
                                        "§cNo tienes espacio "
                                                + "para la Guadaña del Pánico."
                                );

                                return;
                            }

                        } else {

                            ItemStack cursor =
                                    player.getItemOnCursor();

                            if (cursor != null
                                    && !cursor.getType().isAir()) {
                                return;
                            }
                        }

                        /*
                         * ========================================
                         * CONSUMIR CANTIDADES EXACTAS
                         * ========================================
                         */
                        for (int i = 0;
                             i < REQUIRED.length;
                             i++) {

                            ItemStack ingredient = matrix[i];

                            int remaining =
                                    ingredient.getAmount()
                                            - REQUIRED[i];

                            if (remaining <= 0) {
                                matrix[i] = null;
                            } else {
                                ingredient.setAmount(remaining);
                            }
                        }

                        crafting.setMatrix(matrix);

                        /*
                         * ========================================
                         * ENTREGAR RESULTADO
                         * ========================================
                         */
                        if (shift) {

                            player.getInventory()
                                    .setStorageContents(
                                            storageCopy
                                    );

                        } else {

                            player.setItemOnCursor(product);
                        }

                        crafting.setResult(null);

                        player.updateInventory();

                    } finally {

                        pending.remove(uuid);
                    }
                }
        );
    }
}