
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
     * DIA 20 - MULTI PEARLS
     * LIBRO DE RECETAS
     * ========================================
     *
     * Clic normal:
     * Prepara una fabricacion.
     *
     * Shift + clic:
     * Prepara el maximo posible.
     *
     * No acumulamos clics sobre
     * una receta ya colocada.
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

        pearlTemplate = elementalPearl.createItem();
        leadTemplate = new ItemStack(Material.LEAD);

        /*
         * Respetar el limite real de
         * cada stack de ingredientes.
         */
        maxCrafts = Math.min(
                pearlTemplate.getMaxStackSize(),
                leadTemplate.getMaxStackSize()
        );
    }

    /*
     * ========================================
     * SELECCIONAR RECETA
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRecipeBookClick(
            PlayerRecipeBookClickEvent event
    ) {

        /*
         * Ignorar cualquier otra receta.
         */
        if (!recipeKey.equals(event.getRecipe())) {
            return;
        }

        /*
         * Cancelar el llenado automatico
         * de Minecraft.
         *
         * Asi controlamos los ingredientes
         * personalizados y sus cantidades.
         */
        event.setCancelled(true);

        if (!dayManager.isAtLeast(REQUIRED_DAY)) {
            return;
        }

        Player player = event.getPlayer();

        /*
         * Solo mesa de crafteo 3x3.
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

        ItemStack[] currentMatrix =
                crafting.getMatrix();

        if (currentMatrix.length != 9) {
            return;
        }

        /*
         * ========================================
         * EXIGIR MESA VACIA
         * ========================================
         *
         * No agregar ingredientes sobre
         * recetas previamente colocadas.
         *
         * Tampoco sobrescribir objetos
         * que el jugador puso manualmente.
         */
        for (ItemStack item : currentMatrix) {

            if (item != null
                    && !item.getType().isAir()) {

                return;
            }
        }

        /*
         * ========================================
         * CONTAR MATERIALES DISPONIBLES
         * ========================================
         */
        PlayerInventory inventory =
                player.getInventory();

        ItemStack[] storage =
                inventory.getStorageContents();

        int availablePearls = count(
                storage,
                this::isValidPearl
        );

        int availableLeads = count(
                storage,
                this::isValidLead
        );

        /*
         * Cada Multi Pearls necesita:
         * 8 Perlas Elementales + 1 cuerda.
         */
        int possibleCrafts = Math.min(
                availablePearls / 8,
                availableLeads
        );

        possibleCrafts = Math.min(
                possibleCrafts,
                maxCrafts
        );

        /*
         * ========================================
         * CANTIDAD SEGUN EL CLIC
         * ========================================
         */
        int crafts = event.isMakeAll()
                ? possibleCrafts
                : 1;

        if (crafts <= 0
                || possibleCrafts < crafts) {

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
         * Evitar cambios parciales si
         * hay un problema al descontar.
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
         * RETIRAR INGREDIENTES DE LA COPIA
         * ========================================
         */
        if (!take(
                copy,
                this::isValidPearl,
                crafts * 8
        )) {
            return;
        }

        if (!take(
                copy,
                this::isValidLead,
                crafts
        )) {
            return;
        }

        /*
         * ========================================
         * CONSTRUIR MESA
         * ========================================
         *
         * Colocamos la cantidad elegida
         * por cada casilla.
         */
        ItemStack[] matrix =
                createMatrix(crafts);

        /*
         * Los ingredientes pasan del
         * inventario a la mesa.
         *
         * Todavia no se fabrica
         * ninguna Multi Pearls.
         */
        inventory.setStorageContents(copy);

        crafting.setMatrix(matrix);

        crafting.setResult(
                multiPearls.createItem()
        );
    }

    /*
     * ========================================
     * PERLA ELEMENTAL AUTENTICA
     * ========================================
     *
     * ExactChoice debe poder reconocer
     * los objetos que transferimos.
     */
    private boolean isValidPearl(
            ItemStack item
    ) {

        return item != null
                && elementalPearl.isItem(item)
                && item.isSimilar(pearlTemplate);
    }

    /*
     * ========================================
     * CUERDA VANILLA
     * ========================================
     */
    private boolean isValidLead(
            ItemStack item
    ) {

        return item != null
                && item.isSimilar(leadTemplate);
    }

    /*
     * ========================================
     * CONTAR OBJETOS
     * ========================================
     */
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

    /*
     * ========================================
     * DESCONTAR INGREDIENTES
     * ========================================
     */
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
     * CREAR MATRIZ
     * ========================================
     */
    private ItemStack[] createMatrix(
            int crafts
    ) {

        ItemStack[] matrix =
                new ItemStack[9];

        for (int slot = 0; slot < 9; slot++) {

            ItemStack item;

            if (slot == 4) {

                /*
                 * Cuerda central.
                 */
                item = leadTemplate.clone();

            } else {

                /*
                 * Perlas Elementales.
                 */
                item = pearlTemplate.clone();
            }

            item.setAmount(crafts);

            matrix[slot] = item;
        }

        return matrix;
    }
}