package me.difficulty.listeners;

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;
import me.difficulty.day.DayManager;
import me.difficulty.items.day7.CorruptedStar;
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
        ItemStack[] currentMatrix =
                craftingInventory.getMatrix();

        /*
         * Devuelve:
         *
         * 0  = mesa vacía
         * 1  = ingredientes para 1 crafteo
         * 2  = ingredientes para 2 crafteos
         * -1 = hay otros materiales o cantidades incompatibles
         */
        int loadedCrafts =
                getLoadedCrafts(currentMatrix);

        if (loadedCrafts == -1) {

            player.sendMessage(
                    ChatColor.RED
                            + "La mesa contiene materiales que no pertenecen a esta receta."
            );

            return;
        }

        /*
         * Solo caben 2 recetas simultáneamente,
         * porque cada una necesita 32 ojos fermentados.
         *
         * 32 x 2 = 64
         */
        int availableSpace =
                2 - loadedCrafts;

        if (availableSpace <= 0) {

            player.sendMessage(
                    ChatColor.RED
                            + "La mesa ya contiene la cantidad máxima de esta receta."
            );

            return;
        }

        int craftsToAdd;

        if (event.isMakeAll()) {

            /*
             * Shift + clic:
             * añadir todo lo posible.
             */
            craftsToAdd =
                    Math.min(
                            availableSpace,
                            calculateMaximumCrafts(player)
                    );

        } else {

            /*
             * Clic normal:
             * añadir una receta más.
             */
            craftsToAdd = 1;
        }

        if (craftsToAdd <= 0
                || !hasMaterials(player, craftsToAdd)) {

            player.sendMessage(
                    ChatColor.RED
                            + "No tienes suficientes materiales para fabricar otra Estrella Corrupta."
            );

            return;
        }

        /*
         * Quitamos solamente los materiales
         * que vamos a añadir ahora.
         */
        removeMaterials(
                player,
                craftsToAdd
        );

        /*
         * Cantidad total que quedará en la mesa.
         */
        int totalCrafts =
                loadedCrafts + craftsToAdd;

        /*
         * Reconstruimos la matriz con la cantidad total.
         */
        ItemStack[] newMatrix =
                createMatrix(totalCrafts);

        craftingInventory.setMatrix(
                newMatrix
        );

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

    private boolean hasExactItem(
            ItemStack item,
            Material material,
            int amount
    ) {

        return item != null
                && item.getType() == material
                && item.getAmount() == amount;
    }

    private boolean isVanillaNetherStar(
            ItemStack item,
            int amount
    ) {

        if (item == null) {
            return false;
        }

        if (item.getType() != Material.NETHER_STAR) {
            return false;
        }

        if (item.getAmount() != amount) {
            return false;
        }

        return !corruptedStar.isCorruptedStar(item);
    }

    private int getLoadedCrafts(
            ItemStack[] matrix
    ) {

        /*
         * Mesa vacía.
         */
        if (isMatrixEmpty(matrix)) {
            return 0;
        }

        if (matrix.length < 9) {
            return -1;
        }

        /*
         * Probamos si contiene exactamente
         * ingredientes para 1 o 2 recetas.
         */
        for (int crafts = 1;
             crafts <= 2;
             crafts++) {

            boolean matches =

                    hasExactItem(
                            matrix[0],
                            Material.DIAMOND,
                            5 * crafts
                    )

                            && hasExactItem(
                            matrix[1],
                            Material.ECHO_SHARD,
                            3 * crafts
                    )

                            && hasExactItem(
                            matrix[2],
                            Material.DIAMOND,
                            5 * crafts
                    )

                            && hasExactItem(
                            matrix[3],
                            Material.RABBIT_FOOT,
                            3 * crafts
                    )

                            && isVanillaNetherStar(
                            matrix[4],
                            crafts
                    )

                            && hasExactItem(
                            matrix[5],
                            Material.FERMENTED_SPIDER_EYE,
                            32 * crafts
                    )

                            && hasExactItem(
                            matrix[6],
                            Material.DIAMOND,
                            5 * crafts
                    )

                            && hasExactItem(
                            matrix[7],
                            Material.ECHO_SHARD,
                            3 * crafts
                    )

                            && hasExactItem(
                            matrix[8],
                            Material.DIAMOND,
                            5 * crafts
                    );

            if (matches) {
                return crafts;
            }
        }

        /*
         * Hay algo en la mesa,
         * pero no corresponde exactamente
         * a nuestra receta.
         */
        return -1;
    }

    private int countVanillaNetherStars(
            Player player
    ) {

        int amount = 0;

        for (ItemStack item :
                player.getInventory().getStorageContents()) {

            if (item == null) {
                continue;
            }

            if (item.getType() != Material.NETHER_STAR) {
                continue;
            }

            if (corruptedStar.isCorruptedStar(item)) {
                continue;
            }

            amount += item.getAmount();
        }

        return amount;
    }

    private boolean hasMaterials(Player player, int crafts) {

        return countMaterial(player, Material.DIAMOND)
                >= 20 * crafts

                && countMaterial(player, Material.ECHO_SHARD)
                >= 6 * crafts

                && countMaterial(player, Material.RABBIT_FOOT)
                >= 3 * crafts

                && countVanillaNetherStars(player)
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
                countVanillaNetherStars(player);

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

        removeVanillaNetherStars(
                player,
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

    private void removeVanillaNetherStars(
            Player player,
            int amount
    ) {

        PlayerInventory inventory =
                player.getInventory();

        for (int slot = 0;
             slot < inventory.getStorageContents().length;
             slot++) {

            ItemStack item =
                    inventory.getItem(slot);

            if (item == null) {
                continue;
            }

            if (item.getType() != Material.NETHER_STAR) {
                continue;
            }

            // NO tocar Estrellas Corruptas
            if (corruptedStar.isCorruptedStar(item)) {
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