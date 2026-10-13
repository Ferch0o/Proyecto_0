package me.difficulty.listeners;

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;
import me.difficulty.day.DayManager;
import me.difficulty.items.CorruptedStar;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class CorruptedStarRecipeBookListener implements Listener {

    private final NamespacedKey recipeKey;
    private final CorruptedStar corruptedStar;
    private final DayManager dayManager;

    public CorruptedStarRecipeBookListener(
            NamespacedKey recipeKey,
            CorruptedStar corruptedStar,
            DayManager dayManager
    ) {
        this.recipeKey = recipeKey;
        this.corruptedStar = corruptedStar;
        this.dayManager = dayManager;
    }

    @EventHandler
    public void onRecipeBookClick(PlayerRecipeBookClickEvent event) {

        // Solo nos interesa nuestra receta
        if (!event.getRecipe().equals(recipeKey)) {
            return;
        }

        // Evitamos que Minecraft coloque 1 de cada material
        event.setCancelled(true);

        Player player = event.getPlayer();

        // La receta todavía no puede utilizarse
        if (!dayManager.isAtLeast(7)) {
            player.sendMessage(
                    ChatColor.RED
                            + "La Estrella Corrupta no está disponible antes del Día 7."
            );
            return;
        }

        // Comprobamos que tenga abierta una mesa de crafteo
        if (!(player.getOpenInventory().getTopInventory()
                instanceof CraftingInventory craftingInventory)) {

            return;
        }

        // Por seguridad, de momento exigimos que la mesa esté vacía
        if (!isMatrixEmpty(craftingInventory.getMatrix())) {

            player.sendMessage(
                    ChatColor.RED
                            + "Vacía la mesa de crafteo antes de usar esta receta."
            );
            return;
        }

        /*
         * Si hace clic normal:
         * colocamos materiales para 1 receta.
         *
         * Si usa la opción de fabricar el máximo:
         * calculamos cuántas recetas caben.
         */
        int crafts;

        if (event.isMakeAll()) {
            crafts = calculateMaximumCrafts(player);
        } else {
            crafts = 1;
        }

        if (crafts <= 0 || !hasMaterials(player, crafts)) {

            player.sendMessage(
                    ChatColor.RED
                            + "No tienes suficientes materiales para fabricar la Estrella Corrupta."
            );

            return;
        }

        // Quitamos los materiales del inventario
        removeMaterials(player, crafts);

        // Los colocamos manualmente en la mesa
        ItemStack[] matrix = createMatrix(crafts);

        craftingInventory.setMatrix(matrix);

        // Mostramos inmediatamente el resultado
        craftingInventory.setResult(
                corruptedStar.createItem()
        );
    }

    private boolean isMatrixEmpty(ItemStack[] matrix) {

        for (ItemStack item : matrix) {

            if (item != null && !item.getType().isAir()) {
                return false;
            }
        }

        return true;
    }

    private boolean hasMaterials(Player player, int crafts) {

        return countMaterial(player, Material.DIAMOND)
                >= 20 * crafts

                && countMaterial(player, Material.ECHO_SHARD)
                >= 6 * crafts

                && countMaterial(player, Material.RABBIT_FOOT)
                >= 3 * crafts

                && countMaterial(player, Material.NETHER_STAR)
                >= crafts

                && countMaterial(player, Material.FERMENTED_SPIDER_EYE)
                >= 32 * crafts;
    }

    private int calculateMaximumCrafts(Player player) {

        int diamonds =
                countMaterial(player, Material.DIAMOND) / 20;

        int echoShards =
                countMaterial(player, Material.ECHO_SHARD) / 6;

        int rabbitFeet =
                countMaterial(player, Material.RABBIT_FOOT) / 3;

        int netherStars =
                countMaterial(player, Material.NETHER_STAR);

        int spiderEyes =
                countMaterial(
                        player,
                        Material.FERMENTED_SPIDER_EYE
                ) / 32;

        int crafts = Math.min(
                diamonds,
                Math.min(
                        echoShards,
                        Math.min(
                                rabbitFeet,
                                Math.min(
                                        netherStars,
                                        spiderEyes
                                )
                        )
                )
        );

        /*
         * Cada receta necesita 32 ojos en una sola casilla.
         *
         * Una casilla soporta máximo 64.
         *
         * Por eso como máximo podemos colocar
         * ingredientes para 2 recetas simultáneamente.
         */
        return Math.min(crafts, 2);
    }

    private int countMaterial(
            Player player,
            Material material
    ) {

        int amount = 0;

        for (ItemStack item :
                player.getInventory().getStorageContents()) {

            if (item != null
                    && item.getType() == material) {

                amount += item.getAmount();
            }
        }

        return amount;
    }

    private void removeMaterials(
            Player player,
            int crafts
    ) {

        removeMaterial(
                player,
                Material.DIAMOND,
                20 * crafts
        );

        removeMaterial(
                player,
                Material.ECHO_SHARD,
                6 * crafts
        );

        removeMaterial(
                player,
                Material.RABBIT_FOOT,
                3 * crafts
        );

        removeMaterial(
                player,
                Material.NETHER_STAR,
                crafts
        );

        removeMaterial(
                player,
                Material.FERMENTED_SPIDER_EYE,
                32 * crafts
        );
    }

    private void removeMaterial(
            Player player,
            Material material,
            int amount
    ) {

        PlayerInventory inventory =
                player.getInventory();

        for (int slot = 0;
             slot < inventory.getStorageContents().length;
             slot++) {

            ItemStack item = inventory.getItem(slot);

            if (item == null
                    || item.getType() != material) {
                continue;
            }

            if (item.getAmount() <= amount) {

                amount -= item.getAmount();
                inventory.setItem(slot, null);

            } else {

                item.setAmount(
                        item.getAmount() - amount
                );

                amount = 0;
            }

            if (amount <= 0) {
                return;
            }
        }
    }

    private ItemStack[] createMatrix(int crafts) {

        ItemStack[] matrix =
                new ItemStack[9];

        matrix[0] =
                new ItemStack(
                        Material.DIAMOND,
                        5 * crafts
                );

        matrix[1] =
                new ItemStack(
                        Material.ECHO_SHARD,
                        3 * crafts
                );

        matrix[2] =
                new ItemStack(
                        Material.DIAMOND,
                        5 * crafts
                );

        matrix[3] =
                new ItemStack(
                        Material.RABBIT_FOOT,
                        3 * crafts
                );

        matrix[4] =
                new ItemStack(
                        Material.NETHER_STAR,
                        crafts
                );

        matrix[5] =
                new ItemStack(
                        Material.FERMENTED_SPIDER_EYE,
                        32 * crafts
                );

        matrix[6] =
                new ItemStack(
                        Material.DIAMOND,
                        5 * crafts
                );

        matrix[7] =
                new ItemStack(
                        Material.ECHO_SHARD,
                        3 * crafts
                );

        matrix[8] =
                new ItemStack(
                        Material.DIAMOND,
                        5 * crafts
                );

        return matrix;
    }
}