
package me.difficulty.features.day20.items;

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;

import me.difficulty.core.day.DayManager;

import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;

import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class ElementalTemplateRecipeBookListener
        implements Listener {

    private static final int REQUIRED_DAY = 20;

    private final DayManager dayManager;
    private final ElementalTemplateRecipe recipes;

    public ElementalTemplateRecipeBookListener(
            DayManager dayManager,
            ElementalTemplateRecipe recipes
    ) {

        this.dayManager = dayManager;
        this.recipes = recipes;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRecipeBookClick(
            PlayerRecipeBookClickEvent event
    ) {

        ElementalTemplateRecipe.Definition definition =
                recipes.findDefinition(
                        event.getRecipe()
                );

        if (definition == null) {
            return;
        }

        /*
         * Nosotros controlamos las cantidades
         * especiales de ambas recetas.
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

        ItemStack[] matrix =
                crafting.getMatrix();

        if (matrix.length != 9) {
            return;
        }

        /*
         * No acumular clics repetidos.
         * La mesa debe estar vacía.
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

        int possible =
                recipes.possibleFromInventory(
                        storage,
                        definition
                );

        /*
         * Clic normal: una fabricación.
         * Shift + clic: máximo posible.
         */
        int crafts = event.isMakeAll()
                ? possible
                : 1;

        if (crafts <= 0 || crafts > possible) {

            player.sendMessage(
                    "§cNo tienes materiales suficientes "
                            + "para esta receta de Plantilla Elemental."
            );

            return;
        }

        /*
         * Copiar inventario antes de
         * retirar materiales.
         */
        ItemStack[] copy =
                new ItemStack[storage.length];

        for (int i = 0; i < storage.length; i++) {

            copy[i] = storage[i] == null
                    ? null
                    : storage[i].clone();
        }

        /*
         * Descontar solamente en la copia.
         */
        if (!recipes.takeMaterials(
                copy,
                definition,
                crafts
        )) {
            return;
        }

        /*
         * Trasladar los materiales
         * del inventario a la mesa.
         *
         * Todavía no fabricar resultados.
         */
        inventory.setStorageContents(copy);

        crafting.setMatrix(
                recipes.createMatrix(
                        definition,
                        crafts
                )
        );

        crafting.setResult(
                recipes.createResult(definition)
        );
    }
}