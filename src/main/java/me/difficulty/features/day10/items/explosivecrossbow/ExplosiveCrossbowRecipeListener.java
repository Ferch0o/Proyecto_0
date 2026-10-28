package me.difficulty.features.day10.items.explosivecrossbow;

import me.difficulty.day.DayManager;

import me.difficulty.features.day10.items.corruptedink.CorruptedInk;
import me.difficulty.features.day10.items.minotaurhorn.MinotaurHorn;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import org.bukkit.inventory.meta.ItemMeta;

import org.bukkit.persistence.PersistentDataType;

import org.bukkit.Keyed;

import org.bukkit.plugin.java.JavaPlugin;


public class ExplosiveCrossbowRecipeListener
        implements Listener {


    /*
     * ========================================
     * CANTIDADES POR CASILLA
     * ========================================
     */

    private static final int INK_AMOUNT =
            5;

    private static final int HORN_AMOUNT =
            20;

    private static final int SKULL_AMOUNT =
            2;


    private final NamespacedKey recipeKey;

    private final NamespacedKey customItemIdKey;


    private final CorruptedInk corruptedInk;

    private final MinotaurHorn minotaurHorn;

    private final ExplosiveCrossbow explosiveCrossbow;

    private final DayManager dayManager;


    public ExplosiveCrossbowRecipeListener(
            JavaPlugin plugin,
            NamespacedKey recipeKey,
            CorruptedInk corruptedInk,
            MinotaurHorn minotaurHorn,
            ExplosiveCrossbow explosiveCrossbow,
            DayManager dayManager
    ) {

        this.recipeKey =
                recipeKey;

        this.corruptedInk =
                corruptedInk;

        this.minotaurHorn =
                minotaurHorn;

        this.explosiveCrossbow =
                explosiveCrossbow;

        this.dayManager =
                dayManager;


        this.customItemIdKey =
                new NamespacedKey(
                        plugin,
                        "custom_item_id"
                );
    }


    /*
     * ========================================
     * MOSTRAR RESULTADO
     * ========================================
     */

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onPrepareCraft(
            PrepareItemCraftEvent event
    ) {


        if (!isOurRecipe(
                event.getRecipe()
        )) {

            return;
        }


        /*
         * ========================================
         * SOLO DESDE DÍA 10
         * ========================================
         */

        if (!dayManager.isAtLeast(10)) {

            event.getInventory()
                    .setResult(null);

            return;
        }


        ItemStack[] matrix =
                event.getInventory()
                        .getMatrix();


        /*
         * ========================================
         * VALIDAR INGREDIENTES Y CANTIDADES
         * ========================================
         */

        if (!hasCorrectIngredients(matrix)) {

            event.getInventory()
                    .setResult(null);

            return;
        }


        /*
         * Todo correcto:
         *
         * mostramos la Ballesta Explosiva.
         */

        event.getInventory()
                .setResult(
                        explosiveCrossbow.createItem()
                );
    }


    /*
     * ========================================
     * CRAFTEAR
     * ========================================
     */

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onCraft(
            CraftItemEvent event
    ) {


        if (!isOurRecipe(
                event.getRecipe()
        )) {

            return;
        }


        /*
         * Nosotros controlaremos TODO
         * el consumo.
         *
         * Minecraft no debe consumir
         * automáticamente una unidad
         * de cada casilla.
         */

        event.setCancelled(true);


        if (!dayManager.isAtLeast(10)) {
            return;
        }


        ItemStack[] matrix =
                event.getInventory()
                        .getMatrix();


        if (!hasCorrectIngredients(matrix)) {
            return;
        }


        if (!(event.getWhoClicked()
                instanceof Player player)) {

            return;
        }


        ItemStack result =
                explosiveCrossbow.createItem();


        /*
         * ========================================
         * SHIFT + CLICK
         * ========================================
         *
         * Como una ballesta no se apila,
         * necesitamos al menos un espacio
         * libre en el inventario.
         */

        if (event.isShiftClick()) {


            if (player.getInventory()
                    .firstEmpty() == -1) {

                return;
            }


            consumeIngredients(
                    matrix
            );


            event.getInventory()
                    .setMatrix(
                            matrix
                    );


            player.getInventory()
                    .addItem(
                            result
                    );


            return;
        }


        /*
         * ========================================
         * CLICK NORMAL
         * ========================================
         *
         * El cursor debe estar vacío.
         */

        ItemStack cursor =
                player.getItemOnCursor();


        if (!cursor.getType().isAir()) {
            return;
        }


        consumeIngredients(
                matrix
        );


        event.getInventory()
                .setMatrix(
                        matrix
                );


        player.setItemOnCursor(
                result
        );
    }


    /*
     * ========================================
     * VALIDAR MATRIZ
     * ========================================
     *
     * Slot:
     *
     * 0 1 2
     * 3 4 5
     * 6 7 8
     */

    private boolean hasCorrectIngredients(
            ItemStack[] matrix
    ) {


        if (matrix == null
                || matrix.length < 9) {

            return false;
        }


        return

                /*
                 * Primera fila
                 */

                hasCorruptedInk(
                        matrix[0],
                        INK_AMOUNT
                )

                        &&

                        hasMinotaurHorn(
                                matrix[1],
                                HORN_AMOUNT
                        )

                        &&

                        hasCorruptedInk(
                                matrix[2],
                                INK_AMOUNT
                        )


                        /* Segunda fila */

                        &&

                        hasVanillaItem(
                                matrix[3],
                                Material.WITHER_SKELETON_SKULL,
                                SKULL_AMOUNT
                        )

                        &&

                        isVanillaCrossbow(
                                matrix[4]
                        )

                        &&

                        hasVanillaItem(
                                matrix[5],
                                Material.WITHER_SKELETON_SKULL,
                                SKULL_AMOUNT
                        )


                        /* Tercera fila */

                        &&

                        hasCorruptedInk(
                                matrix[6],
                                INK_AMOUNT
                        )

                        &&

                        hasMinotaurHorn(
                                matrix[7],
                                HORN_AMOUNT
                        )

                        &&

                        hasCorruptedInk(
                                matrix[8],
                                INK_AMOUNT
                        );
    }


    /*
     * ========================================
     * TINTA CORRUPTA
     * ========================================
     */

    private boolean hasCorruptedInk(
            ItemStack item,
            int amount
    ) {


        return item != null

                &&

                corruptedInk.isCorruptedInk(
                        item
                )

                &&

                item.getAmount()
                        >= amount;
    }


    /*
     * ========================================
     * CUERNO DE MINOTAURO
     * ========================================
     */

    private boolean hasMinotaurHorn(
            ItemStack item,
            int amount
    ) {


        return item != null

                &&

                minotaurHorn.isMinotaurHorn(
                        item
                )

                &&

                item.getAmount()
                        >= amount;
    }


    /*
     * ========================================
     * ITEM VANILLA
     * ========================================
     *
     * Además del Material correcto,
     * rechazamos cualquier Custom Item
     * de nuestro plugin.
     */

    private boolean hasVanillaItem(
            ItemStack item,
            Material material,
            int amount
    ) {


        if (item == null) {
            return false;
        }


        if (item.getType() != material) {
            return false;
        }


        if (item.getAmount() < amount) {
            return false;
        }


        return !hasCustomItemId(
                item
        );
    }


    /*
     * ========================================
     * BALLESTA VANILLA
     * ========================================
     */

    private boolean isVanillaCrossbow(
            ItemStack item
    ) {


        if (item == null) {
            return false;
        }


        if (item.getType()
                != Material.CROSSBOW) {

            return false;
        }


        /*
         * No permitimos:
         *
         * Ballesta Explosiva
         * ni ninguna futura ballesta custom.
         */

        return !hasCustomItemId(
                item
        );
    }


    /*
     * ========================================
     * COMPROBAR CUSTOM ITEM ID
     * ========================================
     */

    private boolean hasCustomItemId(
            ItemStack item
    ) {


        if (item == null) {
            return false;
        }


        ItemMeta meta =
                item.getItemMeta();


        if (meta == null) {
            return false;
        }


        return meta.getPersistentDataContainer()
                .has(
                        customItemIdKey,
                        PersistentDataType.STRING
                );
    }


    /*
     * ========================================
     * CONSUMIR INGREDIENTES
     * ========================================
     */

    private void consumeIngredients(
            ItemStack[] matrix
    ) {


        /*
         * Tintas
         */

        consume(
                matrix,
                0,
                INK_AMOUNT
        );

        consume(
                matrix,
                2,
                INK_AMOUNT
        );

        consume(
                matrix,
                6,
                INK_AMOUNT
        );

        consume(
                matrix,
                8,
                INK_AMOUNT
        );


        /*
         * Cuernos
         */

        consume(
                matrix,
                1,
                HORN_AMOUNT
        );

        consume(
                matrix,
                7,
                HORN_AMOUNT
        );


        /*
         * Cabezas
         */

        consume(
                matrix,
                3,
                SKULL_AMOUNT
        );

        consume(
                matrix,
                5,
                SKULL_AMOUNT
        );


        /*
         * Ballesta vanilla
         */

        consume(
                matrix,
                4,
                1
        );
    }


    /*
     * ========================================
     * CONSUMIR UNA CASILLA
     * ========================================
     */

    private void consume(
            ItemStack[] matrix,
            int slot,
            int amount
    ) {


        ItemStack item =
                matrix[slot];


        if (item == null) {
            return;
        }


        int remaining =
                item.getAmount()
                        - amount;


        if (remaining <= 0) {

            matrix[slot] =
                    null;

        } else {

            item.setAmount(
                    remaining
            );
        }
    }


    /*
     * ========================================
     * ¿ES NUESTRA RECETA?
     * ========================================
     */

    private boolean isOurRecipe(
            Recipe recipe
    ) {


        if (!(recipe instanceof Keyed keyed)) {
            return false;
        }


        return recipeKey.equals(
                keyed.getKey()
        );
    }
}