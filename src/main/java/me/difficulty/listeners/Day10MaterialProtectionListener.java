package me.difficulty.listeners;

import me.difficulty.items.day10.CorruptedInk;
import me.difficulty.items.day10.MinotaurHorn;

import org.bukkit.event.Event;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

import org.bukkit.block.Crafter;
import org.bukkit.block.Sign;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.CrafterCraftEvent;

import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import org.bukkit.event.player.PlayerInteractEvent;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;


public class Day10MaterialProtectionListener
        implements Listener {


    private final CorruptedInk corruptedInk;

    private final MinotaurHorn minotaurHorn;


    /*
     * ========================================
     * ÚNICA RECETA PERMITIDA
     * ========================================
     *
     * Tinta Corrupta y Cuerno de Minotauro
     * solamente pueden utilizarse en:
     *
     * explosive_crossbow_recipe
     */

    private final NamespacedKey allowedRecipeKey;


    public Day10MaterialProtectionListener(
            NamespacedKey allowedRecipeKey,
            CorruptedInk corruptedInk,
            MinotaurHorn minotaurHorn
    ) {

        this.allowedRecipeKey =
                allowedRecipeKey;

        this.corruptedInk =
                corruptedInk;

        this.minotaurHorn =
                minotaurHorn;
    }


    /*
     * ========================================
     * BLOQUEAR USO DIRECTO DEL ITEM
     * ========================================
     */

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onInteract(
            PlayerInteractEvent event
    ) {


        /*
         * Solo clicks derechos.
         */

        if (event.getAction()
                != Action.RIGHT_CLICK_BLOCK

                &&

                event.getAction()
                        != Action.RIGHT_CLICK_AIR) {

            return;
        }


        ItemStack item =
                event.getItem();


        /*
         * ========================================
         * TINTA CORRUPTA + CARTELES
         * ========================================
         *
         * Impide utilizarla como
         * Glow Ink Sac vanilla.
         */

        if (corruptedInk.isCorruptedInk(item)
                && event.getAction()
                == Action.RIGHT_CLICK_BLOCK
                && event.getClickedBlock()
                != null
                && event.getClickedBlock()
                .getState()
                instanceof Sign) {


            event.setUseInteractedBlock(
                    Event.Result.DENY
            );


            event.setUseItemInHand(
                    Event.Result.DENY
            );


            event.setCancelled(true);

            return;
        }


        /*
         * ========================================
         * OTROS USOS VANILLA
         * ========================================
         */

        if (!isProtectedMaterial(item)) {
            return;
        }


        event.setUseItemInHand(
                Event.Result.DENY
        );
    }


    /*
     * ========================================
     * BLOQUEAR COLOCACIÓN
     * ========================================
     */

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onBlockPlace(
            BlockPlaceEvent event
    ) {


        if (!isProtectedMaterial(
                event.getItemInHand()
        )) {

            return;
        }


        event.setCancelled(true);
    }


    /*
     * ========================================
     * BLOQUEAR RECETAS
     * ========================================
     */

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onPrepareCraft(
            PrepareItemCraftEvent event
    ) {


        if (!containsProtectedMaterial(
                event.getInventory()
                        .getMatrix()
        )) {

            return;
        }


        Recipe recipe =
                event.getRecipe();


        if (recipe == null) {
            return;
        }


        /*
         * Únicamente permitimos la receta
         * de la Ballesta Explosiva.
         */

        if (isAllowedRecipe(recipe)) {
            return;
        }


        /*
         * Cualquier otra receta queda
         * completamente bloqueada.
         */

        event.getInventory()
                .setResult(null);
    }


    /*
     * ========================================
     * SEGUNDA PROTECCIÓN DEL CRAFTEO
     * ========================================
     */

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCraft(
            CraftItemEvent event
    ) {


        if (!containsProtectedMaterial(
                event.getInventory()
                        .getMatrix()
        )) {

            return;
        }


        /*
         * Ballesta Explosiva:
         * permitida.
         */

        if (isAllowedRecipe(
                event.getRecipe()
        )) {

            return;
        }


        /*
         * Cualquier otra receta:
         * bloqueada.
         */

        event.setCancelled(true);
    }


    /*
     * ========================================
     * CRAFTER AUTOMÁTICO
     * ========================================
     */

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCrafterCraft(
            CrafterCraftEvent event
    ) {


        /*
         * Permitimos exclusivamente
         * la receta de la Ballesta.
         */

        if (isAllowedRecipe(
                event.getRecipe()
        )) {

            return;
        }


        if (!(event.getBlock()
                .getState()
                instanceof Crafter crafter)) {

            return;
        }


        if (!containsProtectedMaterial(
                crafter.getInventory()
                        .getContents()
        )) {

            return;
        }


        event.setCancelled(true);
    }


    /*
     * ========================================
     * FUNDICIÓN
     * ========================================
     */

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onSmelt(
            FurnaceSmeltEvent event
    ) {


        if (!isProtectedMaterial(
                event.getSource()
        )) {

            return;
        }


        event.setCancelled(true);
    }


    /*
     * ========================================
     * ¿ES MATERIAL CUSTOM PROTEGIDO?
     * ========================================
     */

    private boolean isProtectedMaterial(
            ItemStack item
    ) {


        if (item == null
                || item.getType().isAir()) {

            return false;
        }


        return corruptedInk
                .isCorruptedInk(item)

                ||

                minotaurHorn
                        .isMinotaurHorn(item);
    }


    /*
     * ========================================
     * BUSCAR MATERIAL CUSTOM
     * ========================================
     */

    private boolean containsProtectedMaterial(
            ItemStack[] contents
    ) {


        if (contents == null) {
            return false;
        }


        for (ItemStack item :
                contents) {


            if (isProtectedMaterial(item)) {

                return true;
            }
        }


        return false;
    }


    /*
     * ========================================
     * ¿ES LA RECETA PERMITIDA?
     * ========================================
     */

    private boolean isAllowedRecipe(
            Recipe recipe
    ) {


        if (!(recipe instanceof Keyed keyed)) {
            return false;
        }


        return allowedRecipeKey.equals(
                keyed.getKey()
        );
    }
}