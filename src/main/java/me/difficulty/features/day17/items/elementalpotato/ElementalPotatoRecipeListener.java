
package me.difficulty.features.day17.items.elementalpotato;

import me.difficulty.core.day.DayManager;
import me.difficulty.features.day17.items.Day17Material;
import me.difficulty.features.day17.items.Day17Materials;

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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ElementalPotatoRecipeListener
        implements Listener {

    /*
     * Cantidades requeridas por cada casilla.
     *
     * 64  64  64
     *  1  16   1
     * 16  64  16
     */
    private static final int[] REQUIRED = {
            64, 64, 64,
            1, 16, 1,
            16, 64, 16
    };

    private final JavaPlugin plugin;
    private final DayManager dayManager;
    private final Day17Materials materials;
    private final ElementalPotato elementalPotato;
    private final NamespacedKey recipeKey;
    private final NamespacedKey customIdKey;

    /*
     * Evita procesar dos veces el mismo
     * crafteo durante una actualización.
     */
    private final Set<UUID> pending =
            new HashSet<>();

    public ElementalPotatoRecipeListener(
            JavaPlugin plugin,
            DayManager dayManager,
            Day17Materials materials,
            ElementalPotato elementalPotato,
            NamespacedKey recipeKey
    ) {
        this.plugin = plugin;
        this.dayManager = dayManager;
        this.materials = materials;
        this.elementalPotato = elementalPotato;
        this.recipeKey = recipeKey;

        this.customIdKey = new NamespacedKey(
                plugin,
                "custom_item_id"
        );
    }

    /*
     * ========================================
     * IDENTIFICAR NUESTRA RECETA
     * ========================================
     */
    private boolean isOurRecipe(Recipe recipe) {

        return recipe instanceof Keyed keyed
                && recipeKey.equals(keyed.getKey());
    }

    /*
     * ========================================
     * COMPROBAR INGREDIENTES
     * ========================================
     */
    private boolean hasRequiredIngredients(
            ItemStack[] matrix
    ) {

        if (matrix.length != 9) {
            return false;
        }

        return hasCustom(
                matrix[0], materials.driedMeat(), 64
        ) && hasCustom(
                matrix[1], materials.guardianBone(), 64
        ) && hasCustom(
                matrix[2], materials.magicGunpowder(), 64
        ) && hasCustom(
                matrix[3], materials.wardenHeart(), 1
        ) && hasVanillaPotato(matrix[4], 16)
                && hasCustom(
                matrix[5], materials.wardenHeart(), 1
        ) && hasCustom(
                matrix[6], materials.titaniumBlock(), 16
        ) && hasCustom(
                matrix[7], materials.goliathEye(), 64
        ) && hasCustom(
                matrix[8], materials.titaniumBlock(), 16
        );
    }

    private boolean hasCustom(
            ItemStack item,
            Day17Material material,
            int amount
    ) {

        return item != null
                && item.getAmount() >= amount
                && material.matches(item);
    }

    /*
     * La patata venenosa central debe
     * ser vanilla, no un item custom.
     */
    private boolean hasVanillaPotato(
            ItemStack item,
            int amount
    ) {

        if (item == null
                || item.getType() != Material.POISONOUS_POTATO
                || item.getAmount() < amount) {
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

        if (!dayManager.isAtLeast(17)
                || !hasRequiredIngredients(
                event.getInventory().getMatrix()
        )) {

            event.getInventory().setResult(null);
            return;
        }

        event.getInventory().setResult(
                elementalPotato.createItem()
        );
    }


    /*
     * ========================================
     * CRAFTEAR PATATA ELEMENTAL
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
         * Cancelamos el crafteo vanilla.
         * Nosotros consumimos las cantidades.
         */
        event.setCancelled(true);

        if (!dayManager.isAtLeast(17)) {
            return;
        }

        if (!(event.getWhoClicked()
                instanceof Player player)) {
            return;
        }

        ClickType click = event.getClick();

        /*
         * Shift + clic izquierdo o derecho:
         * el resultado va al inventario.
         */
        boolean shiftCraft =
                click == ClickType.SHIFT_LEFT
                        || click == ClickType.SHIFT_RIGHT;

        /*
         * Sin Shift:
         * el resultado va al cursor.
         */
        boolean normalCraft =
                click == ClickType.LEFT
                        || click == ClickType.RIGHT;

        if (!shiftCraft && !normalCraft) {
            return;
        }

        CraftingInventory crafting =
                event.getInventory();

        if (!hasRequiredIngredients(
                crafting.getMatrix()
        )) {
            return;
        }

        UUID uuid = player.getUniqueId();

        if (!pending.add(uuid)) {
            return;
        }

        /*
         * Procesamos el crafteo al siguiente
         * tick para evitar desincronizaciones.
         */
        plugin.getServer()
                .getScheduler()
                .runTask(plugin, () -> {

                    try {

                        if (!player.isOnline()
                                || !dayManager.isAtLeast(17)) {
                            return;
                        }

                        if (player.getOpenInventory()
                                .getTopInventory() != crafting) {
                            return;
                        }

                        ItemStack[] matrix =
                                crafting.getMatrix();

                        if (!hasRequiredIngredients(matrix)) {
                            return;
                        }

                        ItemStack product =
                                elementalPotato.createItem();

                        ItemStack[] newStorage = null;

                        /*
                         * ========================================
                         * SHIFT + CLIC
                         * ========================================
                         */
                        if (shiftCraft) {

                            /*
                             * Preparar copia del inventario.
                             *
                             * No alteramos los objetos
                             * originales todavía.
                             */
                            newStorage = player.getInventory()
                                    .getStorageContents();

                            for (int i = 0;
                                 i < newStorage.length;
                                 i++) {

                                if (newStorage[i] != null) {

                                    newStorage[i] =
                                            newStorage[i].clone();
                                }
                            }

                            /*
                             * Comprobar si cabe la Patata.
                             */
                            if (!insertProduct(
                                    newStorage,
                                    product
                            )) {

                                player.sendMessage(
                                        "§cNo tienes espacio "
                                                + "para la Patata Elemental."
                                );

                                return;
                            }

                        } else {

                            /*
                             * ========================================
                             * CLIC NORMAL
                             * ========================================
                             */
                            ItemStack cursor =
                                    player.getItemOnCursor();

                            if (cursor != null
                                    && !cursor.getType().isAir()) {

                                if (!cursor.isSimilar(product)
                                        || cursor.getAmount()
                                        >= cursor.getMaxStackSize()) {

                                    return;
                                }

                                product.setAmount(
                                        cursor.getAmount() + 1
                                );
                            }
                        }

                        /*
                         * ========================================
                         * DESCONTAR INGREDIENTES
                         * ========================================
                         */
                        for (int i = 0;
                             i < REQUIRED.length;
                             i++) {

                            int remaining =
                                    matrix[i].getAmount()
                                            - REQUIRED[i];

                            if (remaining <= 0) {

                                matrix[i] = null;

                            } else {

                                matrix[i].setAmount(remaining);
                            }
                        }

                        crafting.setMatrix(matrix);

                        /*
                         * ========================================
                         * ENTREGAR PATATA ELEMENTAL
                         * ========================================
                         */
                        if (shiftCraft) {

                            /*
                             * La copia ya contiene
                             * la Patata fabricada.
                             */
                            player.getInventory()
                                    .setStorageContents(newStorage);

                        } else {

                            player.setItemOnCursor(product);
                        }

                        /*
                         * Actualizar resultado.
                         */
                        crafting.setResult(
                                hasRequiredIngredients(
                                        crafting.getMatrix()
                                )
                                        ? elementalPotato.createItem()
                                        : null
                        );

                        player.updateInventory();

                    } finally {

                        pending.remove(uuid);
                    }
                });
    }


    /*
     * ========================================
     * INSERTAR RESULTADO EN INVENTARIO
     * ========================================
     *
     * Trabaja sobre una copia del inventario.
     * Devuelve false si no cabe el resultado.
     */
    private boolean insertProduct(
            ItemStack[] storage,
            ItemStack product
    ) {

        int remaining = product.getAmount();

        /*
         * Primero buscar stacks compatibles.
         */
        for (ItemStack current : storage) {

            if (remaining <= 0) {
                return true;
            }

            if (current == null
                    || !current.isSimilar(product)) {
                continue;
            }

            int max = Math.min(
                    current.getMaxStackSize(),
                    product.getMaxStackSize()
            );

            int available =
                    max - current.getAmount();

            if (available <= 0) {
                continue;
            }

            int inserted =
                    Math.min(remaining, available);

            current.setAmount(
                    current.getAmount() + inserted
            );

            remaining -= inserted;
        }

        /*
         * Buscar espacios vacíos.
         */
        for (int i = 0;
             i < storage.length && remaining > 0;
             i++) {

            ItemStack current = storage[i];

            if (current != null
                    && !current.getType().isAir()) {
                continue;
            }

            int inserted = Math.min(
                    remaining,
                    product.getMaxStackSize()
            );

            ItemStack newItem =
                    product.clone();

            newItem.setAmount(inserted);

            storage[i] = newItem;

            remaining -= inserted;
        }

        return remaining == 0;
    }
}