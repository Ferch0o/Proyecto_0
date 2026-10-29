package me.difficulty.features.day10.items.explosivecrossbow;

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;

import me.difficulty.core.day.DayManager;

import me.difficulty.features.day10.items.corruptedink.CorruptedInk;
import me.difficulty.features.day10.items.minotaurhorn.MinotaurHorn;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.bukkit.persistence.PersistentDataType;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Predicate;


public class ExplosiveCrossbowRecipeBookListener
        implements Listener {


    /*
     * ========================================
     * CANTIDADES POR CASILLA
     * ========================================
     */

    private static final int INK_PER_SLOT =
            5;

    private static final int HORN_PER_SLOT =
            20;

    private static final int SKULL_PER_SLOT =
            2;


    /*
     * ========================================
     * TOTALES
     * ========================================
     */

    private static final int TOTAL_INK =
            20;

    private static final int TOTAL_HORNS =
            40;

    private static final int TOTAL_SKULLS =
            4;


    private final NamespacedKey recipeKey;

    private final NamespacedKey customItemIdKey;


    private final CorruptedInk corruptedInk;

    private final MinotaurHorn minotaurHorn;

    private final ExplosiveCrossbow explosiveCrossbow;

    private final DayManager dayManager;


    public ExplosiveCrossbowRecipeBookListener(
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
     * CLICK EN EL LIBRO DE RECETAS
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
         * ========================================
         * SOLO NUESTRA RECETA
         * ========================================
         */

        if (!recipeKey.equals(
                event.getRecipe()
        )) {

            return;
        }


        /*
         * Cancelamos el comportamiento vanilla.
         *
         * Si no lo hacemos, Minecraft pondría
         * solamente 1 item por casilla.
         */

        event.setCancelled(true);


        Player player =
                event.getPlayer();


        /*
         * ========================================
         * SOLO DESDE DÍA 10
         * ========================================
         */

        if (!dayManager.isAtLeast(10)) {

            return;
        }


        /*
         * ========================================
         * DEBE ESTAR EN UNA MESA 3x3
         * ========================================
         */

        if (!(player.getOpenInventory()
                .getTopInventory()
                instanceof CraftingInventory craftingInventory)) {

            return;
        }


        ItemStack[] currentMatrix =
                craftingInventory.getMatrix();


        /*
         * La receta necesita 3x3.
         *
         * Esto evita intentar cargarla
         * dentro del crafting 2x2
         * del inventario personal.
         */

        if (currentMatrix.length < 9) {

            player.sendMessage(
                    "§cNecesitas una mesa de crafteo."
            );

            return;
        }


        /*
         * ========================================
         * LA MESA DEBE ESTAR VACÍA
         * ========================================
         */

        if (!isMatrixEmpty(
                currentMatrix
        )) {

            player.sendMessage(
                    "§cVacía la mesa de crafteo antes de cargar esta receta."
            );

            return;
        }


        /*
         * ========================================
         * COMPROBAR INVENTARIO
         * ========================================
         */

        if (!hasRequiredMaterials(
                player
        )) {

            player.sendMessage(
                    "§cNo tienes todos los materiales necesarios."
            );

            return;
        }


        /*
         * ========================================
         * BUSCAR BALLESTA VANILLA
         * ========================================
         *
         * La copiamos antes de retirarla
         * del inventario.
         */

        ItemStack vanillaCrossbow =
                findVanillaCrossbow(
                        player
                );


        if (vanillaCrossbow == null) {

            return;
        }


        vanillaCrossbow =
                vanillaCrossbow.clone();

        vanillaCrossbow.setAmount(
                1
        );


        /*
         * ========================================
         * RETIRAR MATERIALES DEL INVENTARIO
         * ========================================
         */

        removeRequiredMaterials(
                player
        );


        /*
         * ========================================
         * CONSTRUIR LA MATRIZ
         * ========================================
         *
         * 5    20    5
         * 2     1    2
         * 5    20    5
         */

        ItemStack[] matrix =
                new ItemStack[9];


        /*
         * Primera fila
         */

        matrix[0] =
                corruptedInk.createItem();

        matrix[0].setAmount(
                INK_PER_SLOT
        );


        matrix[1] =
                minotaurHorn.createItem();

        matrix[1].setAmount(
                HORN_PER_SLOT
        );


        matrix[2] =
                corruptedInk.createItem();

        matrix[2].setAmount(
                INK_PER_SLOT
        );


        /*
         * Segunda fila
         */

        matrix[3] =
                new ItemStack(
                        Material.WITHER_SKELETON_SKULL,
                        SKULL_PER_SLOT
                );


        matrix[4] =
                vanillaCrossbow;


        matrix[5] =
                new ItemStack(
                        Material.WITHER_SKELETON_SKULL,
                        SKULL_PER_SLOT
                );


        /*
         * Tercera fila
         */

        matrix[6] =
                corruptedInk.createItem();

        matrix[6].setAmount(
                INK_PER_SLOT
        );


        matrix[7] =
                minotaurHorn.createItem();

        matrix[7].setAmount(
                HORN_PER_SLOT
        );


        matrix[8] =
                corruptedInk.createItem();

        matrix[8].setAmount(
                INK_PER_SLOT
        );


        /*
         * Colocamos la receta manualmente.
         */

        craftingInventory.setMatrix(
                matrix
        );


        /*
         * Mostramos nuestro resultado custom.
         */

        craftingInventory.setResult(
                explosiveCrossbow.createItem()
        );


        player.updateInventory();
    }


    /*
     * ========================================
     * ¿MATRIZ VACÍA?
     * ========================================
     */

    private boolean isMatrixEmpty(
            ItemStack[] matrix
    ) {


        for (ItemStack item :
                matrix) {


            if (item != null
                    && !item.getType()
                    .isAir()) {

                return false;
            }
        }


        return true;
    }


    /*
     * ========================================
     * ¿TIENE TODOS LOS MATERIALES?
     * ========================================
     */

    private boolean hasRequiredMaterials(
            Player player
    ) {


        return countCorruptedInk(
                player
        ) >= TOTAL_INK

                &&

                countMinotaurHorn(
                        player
                ) >= TOTAL_HORNS

                &&

                countVanillaMaterial(
                        player,
                        Material.WITHER_SKELETON_SKULL
                ) >= TOTAL_SKULLS

                &&

                countVanillaMaterial(
                        player,
                        Material.CROSSBOW
                ) >= 1;
    }


    /*
     * ========================================
     * CONTAR TINTA CORRUPTA
     * ========================================
     */

    private int countCorruptedInk(
            Player player
    ) {


        int total =
                0;


        for (ItemStack item :
                player.getInventory()
                        .getStorageContents()) {


            if (item == null) {
                continue;
            }


            if (!corruptedInk
                    .isCorruptedInk(item)) {

                continue;
            }


            total +=
                    item.getAmount();
        }


        return total;
    }


    /*
     * ========================================
     * CONTAR CUERNOS
     * ========================================
     */

    private int countMinotaurHorn(
            Player player
    ) {


        int total =
                0;


        for (ItemStack item :
                player.getInventory()
                        .getStorageContents()) {


            if (item == null) {
                continue;
            }


            if (!minotaurHorn
                    .isMinotaurHorn(item)) {

                continue;
            }


            total +=
                    item.getAmount();
        }


        return total;
    }


    /*
     * ========================================
     * CONTAR ITEM VANILLA
     * ========================================
     */

    private int countVanillaMaterial(
            Player player,
            Material material
    ) {


        int total =
                0;


        for (ItemStack item :
                player.getInventory()
                        .getStorageContents()) {


            if (!isVanillaItem(
                    item,
                    material
            )) {

                continue;
            }


            total +=
                    item.getAmount();
        }


        return total;
    }


    /*
     * ========================================
     * BUSCAR BALLESTA VANILLA
     * ========================================
     */

    private ItemStack findVanillaCrossbow(
            Player player
    ) {


        for (ItemStack item :
                player.getInventory()
                        .getStorageContents()) {


            if (isVanillaItem(
                    item,
                    Material.CROSSBOW
            )) {

                return item;
            }
        }


        return null;
    }


    /*
     * ========================================
     * ¿ITEM VANILLA?
     * ========================================
     */

    private boolean isVanillaItem(
            ItemStack item,
            Material material
    ) {


        if (item == null
                || item.getType()
                != material) {

            return false;
        }


        ItemMeta meta =
                item.getItemMeta();


        if (meta == null) {

            return true;
        }


        /*
         * Si tiene custom_item_id
         * pertenece a nuestro plugin,
         * por lo tanto NO es vanilla.
         */

        return !meta.getPersistentDataContainer()
                .has(
                        customItemIdKey,
                        PersistentDataType.STRING
                );
    }


    /*
     * ========================================
     * RETIRAR MATERIALES
     * ========================================
     */

    private void removeRequiredMaterials(
            Player player
    ) {


        ItemStack[] contents =
                player.getInventory()
                        .getStorageContents();


        /*
         * 20 Tintas Corruptas
         */

        removeItems(
                contents,
                corruptedInk::isCorruptedInk,
                TOTAL_INK
        );


        /*
         * 40 Cuernos
         */

        removeItems(
                contents,
                minotaurHorn::isMinotaurHorn,
                TOTAL_HORNS
        );


        /*
         * 4 Cabezas de Wither Skeleton
         */

        removeItems(
                contents,
                item -> isVanillaItem(
                        item,
                        Material.WITHER_SKELETON_SKULL
                ),
                TOTAL_SKULLS
        );


        /*
         * 1 Ballesta vanilla
         */

        removeItems(
                contents,
                item -> isVanillaItem(
                        item,
                        Material.CROSSBOW
                ),
                1
        );


        player.getInventory()
                .setStorageContents(
                        contents
                );
    }


    /*
     * ========================================
     * RETIRAR CANTIDAD
     * ========================================
     */

    private void removeItems(
            ItemStack[] contents,
            Predicate<ItemStack> condition,
            int amount
    ) {


        int remaining =
                amount;


        for (int i = 0;
             i < contents.length;
             i++) {


            if (remaining <= 0) {
                break;
            }


            ItemStack item =
                    contents[i];


            if (item == null) {
                continue;
            }


            if (!condition.test(
                    item
            )) {

                continue;
            }


            int remove =
                    Math.min(
                            item.getAmount(),
                            remaining
                    );


            int newAmount =
                    item.getAmount()
                            - remove;


            remaining -=
                    remove;


            if (newAmount <= 0) {

                contents[i] =
                        null;

            } else {

                item.setAmount(
                        newAmount
                );
            }
        }
    }
}