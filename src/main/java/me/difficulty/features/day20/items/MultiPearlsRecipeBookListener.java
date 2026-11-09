
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

import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Predicate;

public final class MultiPearlsRecipeBookListener
        implements Listener {

    /*
     * ========================================
     * DIA 20 - LIBRO DE RECETAS
     * ========================================
     *
     * P = Perla Elemental
     * C = Cuerda vanilla
     *
     * P P P
     * P C P
     * P P P
     *
     * Clic normal:
     * Agrega ingredientes para 1 receta.
     *
     * Shift + clic:
     * Agrega el maximo permitido.
     */

    private static final int REQUIRED_DAY = 20;

    private final DayManager dayManager;
    private final ElementalPearl elementalPearl;
    private final MultiPearls multiPearls;
    private final NamespacedKey recipeKey;

    private final ItemStack pearlTemplate;
    private final ItemStack leadTemplate;

    private final int maxCrafts;

    public MultiPearlsRecipeBookListener(
            JavaPlugin plugin,
            DayManager dayManager,
            ElementalPearl elementalPearl,
            MultiPearls multiPearls,
            NamespacedKey recipeKey
    ) {

        this.dayManager = dayManager;
        this.elementalPearl = elementalPearl;
        this.multiPearls = multiPearls;
        this.recipeKey = recipeKey;

        /*
         * Plantillas autenticas de ingredientes.
         */
        this.pearlTemplate =
                elementalPearl.createItem();

        this.leadTemplate =
                new ItemStack(Material.LEAD);

        /*
         * El limite depende de los stacks.
         *
         * Ender Pearl: normalmente 16.
         * Cuerda: normalmente 64.
         *
         * Limite final: 16 fabricaciones.
         */
        this.maxCrafts = Math.min(
                pearlTemplate.getMaxStackSize(),
                leadTemplate.getMaxStackSize()
        );
    }

    /*
     * ========================================
     * CLIC EN EL LIBRO DE RECETAS
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onRecipeBookClick(
            PlayerRecipeBookClickEvent event
    ) {

        /*
         * Solo Multi Pearls.
         */
        if (!recipeKey.equals(event.getRecipe())) {
            return;
        }

        /*
         * Cancelar el llenado automatico.
         * Nosotros controlaremos las cantidades.
         */
        event.setCancelled(true);

        if (!dayManager.isAtLeast(REQUIRED_DAY)) {
            return;
        }

        Player player = event.getPlayer();

        /*
         * Exigir mesa de crafteo 3x3.
         */
        if (player.getOpenInventory().getType()
                != InventoryType.WORKBENCH) {
            return;
        }

        if (!(player.getOpenInventory()
                .getTopInventory()
                instanceof CraftingInventory crafting)) {
            return;
        }

        /*
         * ========================================
         * COMPROBAR LA MESA ACTUAL
         * ========================================
         */
        int loadedCrafts = getLoadedCrafts(
                crafting.getMatrix()
        );

        if (loadedCrafts < 0) {

            player.sendMessage(
                    "§cLa mesa contiene ingredientes "
                            + "incorrectos o cantidades desiguales."
            );

            return;
        }

        /*
         * Espacio para mas fabricaciones.
         */
        int availableSpace =
                maxCrafts - loadedCrafts;

        if (availableSpace <= 0) {

            player.sendMessage(
                    "§cLa mesa ya contiene el máximo "
                            + "de Multi Pearls posibles."
            );

            return;
        }

        /*
         * ========================================
         * COMPROBAR INVENTARIO
         * ========================================
         */
        PlayerInventory inventory =
                player.getInventory();

        ItemStack[] storage =
                inventory.getStorageContents();

        int availablePearls =
                countItems(
                        storage,
                        this::isElementalPearl
                );

        int availableLeads =
                countItems(
                        storage,
                        this::isVanillaLead
                );

        /*
         * Cada fabricacion requiere:
         *
         * 8 Perlas Elementales
         * 1 Cuerda
         */
        int possibleCrafts = Math.min(
                availablePearls / 8,
                availableLeads
        );

        possibleCrafts = Math.min(
                possibleCrafts,
                availableSpace
        );

        /*
         * ========================================
         * DETERMINAR CANTIDAD
         * ========================================
         */
        int craftsToAdd;

        if (event.isMakeAll()) {

            /*
             * Shift + clic:
             * cantidad maxima.
             */
            craftsToAdd = possibleCrafts;

        } else {

            /*
             * Clic normal:
             * una fabricacion mas.
             */
            craftsToAdd = 1;
        }

        if (craftsToAdd <= 0
                || possibleCrafts < craftsToAdd) {

            player.sendMessage(
                    "§cNo tienes suficientes "
                            + "Perlas Elementales o cuerdas."
            );

            return;
        }

        /*
         * ========================================
         * COPIAR INVENTARIO
         * ========================================
         *
         * Todavia no modificamos el
         * inventario original del jugador.
         */
        ItemStack[] copy = storage.clone();

        for (int i = 0; i < copy.length; i++) {

            if (copy[i] != null) {
                copy[i] = copy[i].clone();
            }
        }

        /*
         * ========================================
         * RETIRAR MATERIALES DE LA COPIA
         * ========================================
         */
        boolean removedPearls = removeItems(
                copy,
                this::isElementalPearl,
                craftsToAdd * 8
        );

        boolean removedLeads = removeItems(
                copy,
                this::isVanillaLead,
                craftsToAdd
        );

        /*
         * Si no se pudieron retirar
         * todos los ingredientes,
         * no modificar nada.
         */
        if (!removedPearls || !removedLeads) {
            return;
        }

        /*
         * ========================================
         * ACTUALIZAR MESA
         * ========================================
         */
        int totalCrafts =
                loadedCrafts + craftsToAdd;

        ItemStack[] newMatrix =
                createMatrix(totalCrafts);

        /*
         * Los materiales pasan del inventario
         * del jugador a la mesa.
         *
         * Todavia no se fabrica el resultado.
         */
        inventory.setStorageContents(copy);

        crafting.setMatrix(newMatrix);

        crafting.setResult(
                multiPearls.createItem()
        );

        player.updateInventory();
    }

    /*
     * ========================================
     * IDENTIFICAR PERLA ELEMENTAL
     * ========================================
     */
    private boolean isElementalPearl(
            ItemStack item
    ) {

        return item != null
                && elementalPearl.isItem(item)
                && item.isSimilar(pearlTemplate);
    }

    /*
     * ========================================
     * IDENTIFICAR CUERDA VANILLA
     * ========================================
     */
    private boolean isVanillaLead(
            ItemStack item
    ) {

        return item != null
                && item.isSimilar(leadTemplate);
    }

    /*
     * ========================================
     * CONTAR INGREDIENTES EN INVENTARIO
     * ========================================
     */
    private int countItems(
            ItemStack[] contents,
            Predicate<ItemStack> condition
    ) {

        int total = 0;

        for (ItemStack item : contents) {

            if (item != null
                    && condition.test(item)) {

                total += item.getAmount();
            }
        }

        return total;
    }

    /*
     * ========================================
     * QUITAR INGREDIENTES DE UNA COPIA
     * ========================================
     */
    private boolean removeItems(
            ItemStack[] contents,
            Predicate<ItemStack> condition,
            int amount
    ) {

        int remaining = amount;

        for (int i = 0;
             i < contents.length && remaining > 0;
             i++) {

            ItemStack item = contents[i];

            if (item == null
                    || !condition.test(item)) {
                continue;
            }

            int removed = Math.min(
                    remaining,
                    item.getAmount()
            );

            int left =
                    item.getAmount() - removed;

            if (left <= 0) {

                contents[i] = null;

            } else {

                item.setAmount(left);
            }

            remaining -= removed;
        }

        return remaining == 0;
    }

    /*
     * ========================================
     * OBTENER CRAFTEOS YA COLOCADOS
     * ========================================
     *
     * 0: mesa vacia.
     * 1-16: fabricaciones preparadas.
     * -1: matriz invalida.
     */
    private int getLoadedCrafts(
            ItemStack[] matrix
    ) {

        if (matrix.length != 9) {
            return -1;
        }

        /*
         * Comprobar si esta vacia.
         */
        boolean empty = true;

        for (ItemStack item : matrix) {

            if (item != null
                    && !item.getType().isAir()) {

                empty = false;
                break;
            }
        }

        if (empty) {
            return 0;
        }

        /*
         * Comprobar ingrediente central.
         */
        ItemStack center = matrix[4];

        if (!isVanillaLead(center)) {
            return -1;
        }

        int amount = center.getAmount();

        if (amount < 1
                || amount > maxCrafts) {
            return -1;
        }

        /*
         * Las ocho casillas deben
         * tener exactamente la misma
         * cantidad de Perlas Elementales.
         */
        for (int slot = 0; slot < 9; slot++) {

            if (slot == 4) {
                continue;
            }

            ItemStack item = matrix[slot];

            if (!isElementalPearl(item)
                    || item.getAmount() != amount) {

                return -1;
            }
        }

        return amount;
    }

    /*
     * ========================================
     * CONSTRUIR MATRIZ DEL CRAFTEO
     * ========================================
     */
    private ItemStack[] createMatrix(
            int crafts
    ) {

        ItemStack[] matrix =
                new ItemStack[9];

        for (int slot = 0; slot < 9; slot++) {

            if (slot == 4) {

                ItemStack lead =
                        leadTemplate.clone();

                lead.setAmount(crafts);

                matrix[slot] = lead;

            } else {

                ItemStack pearl =
                        pearlTemplate.clone();

                pearl.setAmount(crafts);

                matrix[slot] = pearl;
            }
        }

        return matrix;
    }
}