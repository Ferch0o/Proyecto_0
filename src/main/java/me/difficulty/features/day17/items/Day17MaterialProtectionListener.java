package me.difficulty.features.day17.items;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

import org.bukkit.block.Crafter;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.CrafterCraftEvent;

import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public final class Day17MaterialProtectionListener
        implements Listener {

    private final Day17Materials materials;
    private final NamespacedKey recipeKey;

    public Day17MaterialProtectionListener(
            Day17Materials materials,
            NamespacedKey recipeKey
    ) {
        this.materials = materials;
        this.recipeKey = recipeKey;
    }

    /*
     * ========================================
     * IDENTIFICAR MATERIALES RESTRINGIDOS
     * ========================================
     *
     * Unicamente:
     *
     * - Bloque de Titanio.
     * - Corazon de Warden.
     *
     * Los otros cuatro materiales
     * conservan sus usos vanilla.
     */
    private boolean isRestricted(ItemStack item) {

        if (item == null) {
            return false;
        }

        return materials.titaniumBlock().matches(item)
                || materials.wardenHeart().matches(item);
    }

    /*
     * ========================================
     * BUSCAR MATERIALES RESTRINGIDOS
     * ========================================
     */
    private boolean containsRestricted(
            ItemStack[] contents
    ) {

        for (ItemStack item : contents) {

            if (isRestricted(item)) {
                return true;
            }
        }

        return false;
    }

    /*
     * ========================================
     * RECETA PERMITIDA
     * ========================================
     *
     * La Patata Elemental es la unica
     * receta que puede utilizar los
     * materiales restringidos.
     */
    private boolean isAllowedRecipe(Recipe recipe) {

        return recipe instanceof Keyed keyed
                && recipeKey.equals(keyed.getKey());
    }

    /*
     * ========================================
     * BLOQUEAR RESULTADOS DE OTRAS RECETAS
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepare(
            PrepareItemCraftEvent event
    ) {

        if (!containsRestricted(
                event.getInventory().getMatrix()
        )) {
            return;
        }

        /*
         * Permitir solamente la receta
         * de la Patata Elemental.
         */
        if (isAllowedRecipe(event.getRecipe())) {
            return;
        }

        event.getInventory().setResult(null);
    }

    /*
     * ========================================
     * IMPEDIR CRAFTEOS VANILLA
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCraft(
            CraftItemEvent event
    ) {

        if (!containsRestricted(
                event.getInventory().getMatrix()
        )) {
            return;
        }

        /*
         * Dejar que la receta especial
         * gestione el consumo exacto.
         */
        if (isAllowedRecipe(event.getRecipe())) {
            return;
        }

        event.setCancelled(true);
    }

    /*
     * ========================================
     * PROTEGER CONTRA EL CRAFTER AUTOMATICO
     * ========================================
     *
     * El Crafter vanilla consume una
     * unidad por casilla.
     *
     * Nuestra Patata requiere cantidades
     * mayores, por lo que no permitimos
     * fabricarla automaticamente.
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCrafter(
            CrafterCraftEvent event
    ) {

        if (!(event.getBlock().getState()
                instanceof Crafter crafter)) {
            return;
        }

        if (containsRestricted(
                crafter.getInventory().getContents()
        )) {
            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * IMPEDIR COLOCAR BLOQUE DE TITANIO
     * ========================================
     */
    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onPlace(
            BlockPlaceEvent event
    ) {

        if (isRestricted(
                event.getItemInHand()
        )) {
            event.setCancelled(true);
        }
    }

    /*
     * ========================================
     * IMPEDIR USOS DIRECTOS VANILLA
     * ========================================
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(
            PlayerInteractEvent event
    ) {

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR
                && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!isRestricted(event.getItem())) {
            return;
        }

        /*
         * Bloquear el uso del item,
         * pero permitir la interaccion
         * normal con cofres, puertas, etc.
         */
        event.setUseItemInHand(
                Event.Result.DENY
        );
    }
}