
package me.difficulty.features.day20.items;

import me.difficulty.core.day.DayManager;
import me.difficulty.features.day17.items.Day17Material;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ElementalIngotRecipe
        implements Listener {

    private static final int REQUIRED_DAY = 20;

    /*
     * Hierro   Cobre    Oro
     * Netherite Titanio Diamante
     * Oro      Cobre    Hierro
     */
    private static final int[] REQUIRED = {
            64, 64, 64,
            1, 20, 10,
            64, 64, 64
    };

    private final JavaPlugin plugin;
    private final DayManager dayManager;
    private final Day17Material titanium;
    private final ElementalIngot elementalIngot;

    private final NamespacedKey recipeKey;
    private final ItemStack titaniumTemplate;

    private final Set<UUID> pending =
            new HashSet<>();

    public ElementalIngotRecipe(
            JavaPlugin plugin,
            DayManager dayManager,
            Day17Material titanium,
            ElementalIngot elementalIngot
    ) {

        this.plugin = plugin;
        this.dayManager = dayManager;
        this.titanium = titanium;
        this.elementalIngot = elementalIngot;

        recipeKey = new NamespacedKey(
                plugin,
                "elemental_ingot_recipe"
        );

        titaniumTemplate = titanium.createItem();
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
                elementalIngot.createItem()
        );

        recipe.shape(
                "ICG",
                "NTD",
                "GCI"
        );

        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('C', Material.COPPER_INGOT);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('D', Material.DIAMOND);

        /*
         * Solo Bloque de Titanio auténtico.
         * No acepta bloque de hierro vanilla.
         */
        recipe.setIngredient(
                'T',
                new RecipeChoice.ExactChoice(
                        titaniumTemplate
                )
        );

        plugin.getServer().addRecipe(recipe);
    }

    private boolean isOurRecipe(Recipe recipe) {

        return recipe instanceof Keyed keyed
                && recipeKey.equals(keyed.getKey());
    }

    /*
     * ========================================
     * VALIDAR MATERIALES VANILLA
     * ========================================
     */
    private boolean isVanilla(
            ItemStack item,
            Material type,
            int amount
    ) {

        return item != null
                && item.getAmount() >= amount
                && item.isSimilar(new ItemStack(type));
    }

    /*
     * ========================================
     * VALIDAR TITANIO
     * ========================================
     */
    private boolean isTitanium(
            ItemStack item,
            int amount
    ) {

        return item != null
                && item.getAmount() >= amount
                && titanium.matches(item)
                && item.isSimilar(titaniumTemplate);
    }

    /*
     * ========================================
     * VALIDAR MATRIZ
     * ========================================
     */
    private boolean hasIngredients(
            ItemStack[] matrix
    ) {

        if (matrix == null || matrix.length != 9) {
            return false;
        }

        return isVanilla(
                matrix[0], Material.IRON_INGOT, 64
        ) && isVanilla(
                matrix[1], Material.COPPER_INGOT, 64
        ) && isVanilla(
                matrix[2], Material.GOLD_INGOT, 64
        ) && isVanilla(
                matrix[3], Material.NETHERITE_INGOT, 1
        ) && isTitanium(
                matrix[4], 20
        ) && isVanilla(
                matrix[5], Material.DIAMOND, 10
        ) && isVanilla(
                matrix[6], Material.GOLD_INGOT, 64
        ) && isVanilla(
                matrix[7], Material.COPPER_INGOT, 64
        ) && isVanilla(
                matrix[8], Material.IRON_INGOT, 64
        );
    }

    /*
     * ========================================
     * MOSTRAR RESULTADO
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepare(
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
                elementalIngot.createItem()
        );
    }

    /*
     * ========================================
     * FABRICAR LINGOTE
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
         * Impedir consumo vanilla de
         * una unidad por casilla.
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

        /*
         * Procesamiento al siguiente tick.
         */
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
                                elementalIngot.createItem();

                        ItemStack[] storageCopy = null;

                        /*
                         * ========================================
                         * SHIFT + CLIC EN RESULTADO
                         * ========================================
                         */
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

                            if (!insertProduct(
                                    storageCopy,
                                    product
                            )) {

                                player.sendMessage(
                                        "§cNo tienes espacio "
                                                + "para el Lingote Elemental."
                                );

                                return;
                            }

                        } else {

                            /*
                             * ========================================
                             * CLIC NORMAL EN RESULTADO
                             * ========================================
                             */
                            ItemStack cursor =
                                    player.getItemOnCursor();

                            if (cursor != null
                                    && !cursor.getType().isAir()) {

                                if (!cursor.isSimilar(product)
                                        || cursor.getAmount()
                                        >= product.getMaxStackSize()) {
                                    return;
                                }

                                product.setAmount(
                                        cursor.getAmount() + 1
                                );
                            }
                        }

                        /*
                         * ========================================
                         * CONSUMIR INGREDIENTES EXACTOS
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
                                    .setStorageContents(storageCopy);

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

    /*
     * ========================================
     * INSERTAR RESULTADO EN INVENTARIO
     * ========================================
     */
    private boolean insertProduct(
            ItemStack[] storage,
            ItemStack product
    ) {

        for (ItemStack item : storage) {

            if (item == null
                    || !item.isSimilar(product)) {
                continue;
            }

            int max = item.getMaxStackSize();

            if (item.getAmount() < max) {

                item.setAmount(item.getAmount() + 1);
                return true;
            }
        }

        for (int i = 0; i < storage.length; i++) {

            if (storage[i] == null
                    || storage[i].getType().isAir()) {

                storage[i] = product.clone();
                return true;
            }
        }

        return false;
    }

    /*
     * ========================================
     * IMPEDIR CRAFTER AUTOMATICO
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCrafter(
            CrafterCraftEvent event
    ) {

        if (isOurRecipe(event.getRecipe())) {
            event.setCancelled(true);
        }
    }
}
