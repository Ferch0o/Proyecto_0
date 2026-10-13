package me.difficulty.listeners;

import me.difficulty.day.DayManager;
import me.difficulty.items.day7.CorruptedStar;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Map;

public class CorruptedStarRecipeListener implements Listener {

    private final NamespacedKey recipeKey;
    private final CorruptedStar corruptedStar;
    private final DayManager dayManager;

    public CorruptedStarRecipeListener(
            NamespacedKey recipeKey,
            CorruptedStar corruptedStar,
            DayManager dayManager
    ) {
        this.recipeKey = recipeKey;
        this.corruptedStar = corruptedStar;
        this.dayManager = dayManager;
    }

    /*
     * Comprueba constantemente si las cantidades
     * colocadas en la mesa son suficientes.
     */
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {

        if (!isCorruptedStarRecipe(event.getRecipe())) {
            return;
        }

        if (!dayManager.isAtLeast(7)) {
            event.getInventory().setResult(null);
            return;
        }

        ItemStack[] matrix =
                event.getInventory().getMatrix();

        if (!hasRequiredAmounts(matrix)) {

            event.getInventory().setResult(null);
            return;
        }

        event.getInventory().setResult(
                corruptedStar.createItem()
        );
    }

    /*
     * Controlamos manualmente el crafteo.
     */
    @EventHandler
    public void onCraft(CraftItemEvent event) {

        if (!isCorruptedStarRecipe(event.getRecipe())) {
            return;
        }

        if (!dayManager.isAtLeast(7)) {

            event.setCancelled(true);

            if (event.getWhoClicked() instanceof Player player) {
                player.sendMessage(
                        "§cLa Estrella Corrupta no puede fabricarse antes del Día 7."
                );
            }

            return;
        }

        /*
         * Cancelamos el comportamiento normal de Minecraft.
         *
         * Nosotros quitaremos los materiales y entregaremos
         * la Estrella.
         */
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack[] matrix =
                event.getInventory().getMatrix();

        if (!hasRequiredAmounts(matrix)) {
            return;
        }

        /*
         * Shift + Click:
         * fabrica todas las estrellas posibles.
         *
         * Click normal:
         * fabrica una.
         */
        int crafts;

        if (event.isShiftClick()) {
            crafts = calculateMaximumCrafts(matrix);
        } else {
            crafts = 1;
        }

        if (crafts <= 0) {
            return;
        }

        /*
         * Consumimos exactamente las cantidades necesarias.
         */
        consumeIngredients(matrix, crafts);

        event.getInventory().setMatrix(matrix);

        /*
         * Entregamos el resultado.
         */
        if (event.isShiftClick()) {

            giveToInventory(
                    player,
                    crafts
            );

        } else {

            giveToCursor(
                    player
            );
        }

        /*
         * Actualizamos el resultado de la mesa.
         *
         * Si todavía quedan materiales suficientes,
         * seguirá apareciendo otra Estrella.
         */
        if (hasRequiredAmounts(matrix)) {

            event.getInventory().setResult(
                    corruptedStar.createItem()
            );

        } else {

            event.getInventory().setResult(null);
        }
    }

    private boolean isCorruptedStarRecipe(Recipe recipe) {

        if (!(recipe instanceof Keyed keyed)) {
            return false;
        }

        return keyed.getKey().equals(recipeKey);
    }

    /*
     * Cantidades necesarias:
     *
     * 5 Diamantes      3 Fragmentos      5 Diamantes
     *
     * 3 Pata Conejo    1 Nether Star     32 Ojos
     *
     * 5 Diamantes      3 Fragmentos      5 Diamantes
     */
    private boolean hasRequiredAmounts(ItemStack[] matrix) {

        if (matrix.length < 9) {
            return false;
        }

        return hasItem(matrix[0], Material.DIAMOND, 5)
                && hasItem(matrix[1], Material.ECHO_SHARD, 3)
                && hasItem(matrix[2], Material.DIAMOND, 5)

                && hasItem(matrix[3], Material.RABBIT_FOOT, 3)
                && hasItem(matrix[4], Material.NETHER_STAR, 1)
                && hasItem(matrix[5], Material.FERMENTED_SPIDER_EYE, 32)

                && hasItem(matrix[6], Material.DIAMOND, 5)
                && hasItem(matrix[7], Material.ECHO_SHARD, 3)
                && hasItem(matrix[8], Material.DIAMOND, 5);
    }

    private boolean hasItem(
            ItemStack item,
            Material material,
            int amount
    ) {

        return item != null
                && item.getType() == material
                && item.getAmount() >= amount;
    }

    /*
     * Calcula cuántas estrellas pueden fabricarse
     * con las cantidades actuales.
     */
    private int calculateMaximumCrafts(ItemStack[] matrix) {

        int crafts = Integer.MAX_VALUE;

        crafts = Math.min(crafts, matrix[0].getAmount() / 5);
        crafts = Math.min(crafts, matrix[1].getAmount() / 3);
        crafts = Math.min(crafts, matrix[2].getAmount() / 5);

        crafts = Math.min(crafts, matrix[3].getAmount() / 3);
        crafts = Math.min(crafts, matrix[4].getAmount());
        crafts = Math.min(crafts, matrix[5].getAmount() / 32);

        crafts = Math.min(crafts, matrix[6].getAmount() / 5);
        crafts = Math.min(crafts, matrix[7].getAmount() / 3);
        crafts = Math.min(crafts, matrix[8].getAmount() / 5);

        return crafts;
    }

    /*
     * Consume exactamente los materiales de N crafteos.
     */
    private void consumeIngredients(
            ItemStack[] matrix,
            int crafts
    ) {

        consume(matrix, 0, 5 * crafts);
        consume(matrix, 1, 3 * crafts);
        consume(matrix, 2, 5 * crafts);

        consume(matrix, 3, 3 * crafts);
        consume(matrix, 4, crafts);
        consume(matrix, 5, 32 * crafts);

        consume(matrix, 6, 5 * crafts);
        consume(matrix, 7, 3 * crafts);
        consume(matrix, 8, 5 * crafts);
    }

    private void consume(
            ItemStack[] matrix,
            int slot,
            int amount
    ) {

        ItemStack item = matrix[slot];

        if (item == null) {
            return;
        }

        int remaining =
                item.getAmount() - amount;

        if (remaining <= 0) {

            matrix[slot] = null;

        } else {

            item.setAmount(remaining);
        }
    }

    /*
     * Click normal:
     * entrega la estrella al cursor.
     */
    private void giveToCursor(Player player) {

        ItemStack result =
                corruptedStar.createItem();

        ItemStack cursor =
                player.getItemOnCursor();

        if (cursor.getType().isAir()) {

            player.setItemOnCursor(result);
            return;
        }

        /*
         * Si ya tiene estrellas corruptas en el cursor,
         * las apilamos.
         */
        if (corruptedStar.isCorruptedStar(cursor)
                && cursor.getAmount() < cursor.getMaxStackSize()) {

            cursor.setAmount(
                    cursor.getAmount() + 1
            );

            player.setItemOnCursor(cursor);

            return;
        }

        /*
         * Si el cursor está ocupado,
         * la mandamos al inventario.
         */
        giveToInventory(player, 1);
    }

    /*
     * Shift + Click:
     * mete directamente todas las estrellas
     * fabricadas al inventario.
     */
    private void giveToInventory(
            Player player,
            int amount
    ) {

        ItemStack result =
                corruptedStar.createItem();

        result.setAmount(amount);

        Map<Integer, ItemStack> leftover =
                player.getInventory().addItem(result);

        /*
         * Si el inventario está lleno,
         * dejamos caer el sobrante.
         */
        for (ItemStack item : leftover.values()) {

            player.getWorld().dropItemNaturally(
                    player.getLocation(),
                    item
            );
        }
    }
}