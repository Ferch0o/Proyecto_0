
package me.difficulty.features.day20.items;

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;
import me.difficulty.core.day.DayManager;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class MultiPearlsRecipe
        implements Listener {

    /*
     * ========================================
     * DIA 20 - MULTI PEARLS
     * ========================================
     *
     * P = Perla Elemental
     * C = Cuerda / Rienda (LEAD)
     *
     * P P P
     * P C P
     * P P P
     *
     * Resultado: 1 Multi Pearls.
     */

    private static final int REQUIRED_DAY = 20;

    private final JavaPlugin plugin;
    private final DayManager dayManager;

    private final ElementalPearl elementalPearl;
    private final MultiPearls multiPearls;

    private final NamespacedKey recipeKey;
    private final NamespacedKey itemIdKey;

    /*
     * Recetas futuras que podran
     * consumir Multi Pearls.
     */
    private final NamespacedKey panicScytheRecipeKey;
    private final NamespacedKey elementalTemplateCopyKey;

    public MultiPearlsRecipe(
            JavaPlugin plugin,
            DayManager dayManager,
            ElementalPearl elementalPearl,
            MultiPearls multiPearls
    ) {

        this.plugin = plugin;
        this.dayManager = dayManager;
        this.elementalPearl = elementalPearl;
        this.multiPearls = multiPearls;

        recipeKey = new NamespacedKey(
                plugin,
                "multi_pearls_recipe"
        );

        itemIdKey = new NamespacedKey(
                plugin,
                "custom_item_id"
        );

        panicScytheRecipeKey = new NamespacedKey(
                plugin,
                "panic_scythe_recipe"
        );

        elementalTemplateCopyKey = new NamespacedKey(
                plugin,
                "elemental_template_copy_recipe"
        );
    }

    /*
     * ========================================
     * REGISTRAR RECETA
     * ========================================
     */
    public void register() {

        ShapedRecipe recipe = new ShapedRecipe(
                recipeKey,
                multiPearls.createItem()
        );

        recipe.shape(
                "PPP",
                "PCP",
                "PPP"
        );


        /*
         * ========================================
         * PERLAS ELEMENTALES OBLIGATORIAS
         * ========================================
         *
         * ExactChoice exige que las perlas
         * coincidan con nuestro item custom.
         *
         * No acepta Ender Pearls vanilla.
         */
        recipe.setIngredient(
                'P',
                new RecipeChoice.ExactChoice(
                        elementalPearl.createItem()
                )
        );

        /*
         * Cuerda para sujetar animales.
         * NO utilizar STRING (hilo).
         */
        recipe.setIngredient(
                'C',
                Material.LEAD
        );

        plugin.getServer().addRecipe(recipe);
    }

    /*
     * ========================================
     * IDENTIFICAR RECETAS
     * ========================================
     */
    private boolean isRecipe(
            Recipe recipe,
            NamespacedKey key
    ) {

        return recipe instanceof Keyed keyed
                && key.equals(keyed.getKey());
    }

    private boolean isMultiPearlsRecipe(
            Recipe recipe
    ) {

        return isRecipe(recipe, recipeKey);
    }

    private boolean isAllowedFutureRecipe(
            Recipe recipe
    ) {

        return isRecipe(recipe, panicScytheRecipeKey)
                || isRecipe(
                recipe,
                elementalTemplateCopyKey
        );
    }

    /*
     * ========================================
     * VERIFICAR CUERDA VANILLA
     * ========================================
     */
    private boolean isVanillaLead(
            ItemStack item
    ) {

        if (item == null
                || item.getType() != Material.LEAD) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return true;
        }

        /*
         * Evitar consumir objetos custom
         * que utilicen LEAD como base.
         */
        return !meta.getPersistentDataContainer().has(
                itemIdKey,
                PersistentDataType.STRING
        );
    }

    /*
     * ========================================
     * VALIDAR LOS NUEVE INGREDIENTES
     * ========================================
     */
    private boolean isValidMatrix(
            ItemStack[] matrix
    ) {

        if (matrix.length != 9) {
            return false;
        }

        for (int slot = 0; slot < 9; slot++) {

            ItemStack item = matrix[slot];

            if (slot == 4) {

                /*
                 * Casilla central:
                 * una cuerda vanilla.
                 */
                if (!isVanillaLead(item)) {
                    return false;
                }

            } else {

                /*
                 * Las otras ocho casillas
                 * deben contener Perlas
                 * Elementales autenticas.
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
     * DETECTAR MULTI PEARLS
     * ========================================
     */
    private boolean containsMultiPearls(
            ItemStack[] items
    ) {

        for (ItemStack item : items) {

            if (multiPearls.isItem(item)) {
                return true;
            }
        }

        return false;
    }

    /*
     * ========================================
     * PREPARAR CRAFTEO
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onPrepareCraft(
            PrepareItemCraftEvent event
    ) {

        Recipe recipe = event.getRecipe();

        /*
         * ========================================
         * VALIDAR MULTI PEARLS
         * ========================================
         */
        if (isMultiPearlsRecipe(recipe)) {

            if (!dayManager.isAtLeast(REQUIRED_DAY)
                    || !isValidMatrix(
                    event.getInventory().getMatrix()
            )) {

                event.getInventory().setResult(null);
            }

            return;
        }

        /*
         * ========================================
         * PROTEGER MULTI PEARLS
         * ========================================
         *
         * No permitir usar nuestro objeto
         * en recetas vanilla.
         */
        if (!containsMultiPearls(
                event.getInventory().getMatrix()
        )) {
            return;
        }

        if (dayManager.isAtLeast(REQUIRED_DAY)
                && isAllowedFutureRecipe(recipe)) {
            return;
        }

        event.getInventory().setResult(null);
    }

    /*
     * ========================================
     * CRAFTEO MANUAL
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCraft(
            CraftItemEvent event
    ) {

        Recipe recipe = event.getRecipe();

        /*
         * Verificar nuevamente antes
         * de consumir los ingredientes.
         */
        if (isMultiPearlsRecipe(recipe)) {

            if (!dayManager.isAtLeast(REQUIRED_DAY)
                    || !isValidMatrix(
                    event.getInventory().getMatrix()
            )) {

                event.setCancelled(true);
            }

            return;
        }

        /*
         * Bloquear recetas no permitidas
         * que consuman Multi Pearls.
         */
        if (!containsMultiPearls(
                event.getInventory().getMatrix()
        )) {
            return;
        }

        if (dayManager.isAtLeast(REQUIRED_DAY)
                && isAllowedFutureRecipe(recipe)) {
            return;
        }

        event.setCancelled(true);
    }

    /*
     * ========================================
     * CRAFTER AUTOMATICO
     * ========================================
     *
     * No habilitado por ahora.
     * Impedir tambien que las perlas
     * vanilla puedan fabricar Multi Pearls
     * mediante un Crafter.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCrafterCraft(
            CrafterCraftEvent event
    ) {

        if (isMultiPearlsRecipe(event.getRecipe())) {

            event.setCancelled(true);
            return;
        }

        if (!(event.getBlock().getState()
                instanceof org.bukkit.block.Crafter crafter)) {
            return;
        }

        if (containsMultiPearls(
                crafter.getInventory().getContents()
        )) {

            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * MULTI PEARLS - LIBRO DE RECETAS
     * ========================================
     *
     * Cuando se selecciona la receta,
     * Minecraft coloca la mayor cantidad
     * posible de ingredientes.
     *
     * Esto permite fabricar varias
     * Multi Pearls mediante clics
     * izquierdos normales.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onRecipeBookClick(
            PlayerRecipeBookClickEvent event
    ) {

        /*
         * Solo nuestra receta.
         */
        if (!recipeKey.equals(event.getRecipe())) {
            return;
        }

        /*
         * No permitir antes del dia 20.
         */
        if (!dayManager.isAtLeast(REQUIRED_DAY)) {

            event.setCancelled(true);
            return;
        }

        /*
         * Hacer que Minecraft intente
         * colocar el maximo posible
         * de ingredientes en la mesa.
         */
        event.setMakeAll(true);
    }

    /*
     * ========================================
     * CLAVE DE RECETA
     * ========================================
     */
    public NamespacedKey getRecipeKey() {
        return recipeKey;
    }
}