package me.difficulty.features.day10.items.explosivecrossbow;

import me.difficulty.items.CustomItem;
import me.difficulty.items.ItemIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;


public class ExplosiveCrossbow extends CustomItem {


    public ExplosiveCrossbow(
            JavaPlugin plugin
    ) {

        super(
                plugin,
                ItemIds.EXPLOSIVE_CROSSBOW,
                Material.CROSSBOW,
                ItemIds.EXPLOSIVE_CROSSBOW_MODEL
        );
    }


    /*
     * ========================================
     * CREAR BALLESTA EXPLOSIVA
     * ========================================
     */

    @Override
    public ItemStack createItem() {


        /*
         * CustomItem ya coloca:
         *
         * - Material.CROSSBOW
         * - custom_item_id
         * - CustomModelData
         * - nombre
         * - lore
         */

        ItemStack item =
                super.createItem();


        ItemMeta meta =
                item.getItemMeta();


        /*
         * ========================================
         * IRROMPIBLE
         * ========================================
         */

        meta.setUnbreakable(
                true
        );

        item.setItemMeta(
                meta
        );


        return item;
    }


    /*
     * ========================================
     * NOMBRE
     * ========================================
     */

    @Override
    protected Component getDisplayName() {

        return Component.text(
                        "Ballesta Explosiva",
                        NamedTextColor.DARK_RED
                )
                .decoration(
                        TextDecoration.ITALIC,
                        false
                );
    }


    /*
     * ========================================
     * LORE
     * ========================================
     */

    @Override
    protected List<Component> getLore() {

        return List.of(

                Component.text(
                                "Una ballesta modificada con materiales corruptos.",
                                NamedTextColor.GRAY
                        )
                        .decoration(
                                TextDecoration.ITALIC,
                                false
                        ),

                Component.text(
                                "Sus proyectiles liberan una carga explosiva.",
                                NamedTextColor.RED
                        )
                        .decoration(
                                TextDecoration.ITALIC,
                                false
                        ),

                Component.text(
                                "Disponible desde el Día 10.",
                                NamedTextColor.DARK_GRAY
                        )
                        .decoration(
                                TextDecoration.ITALIC,
                                false
                        )
        );
    }


    /*
     * ========================================
     * IDENTIFICAR BALLESTA EXPLOSIVA
     * ========================================
     */

    public boolean isExplosiveCrossbow(
            ItemStack item
    ) {

        return isItem(item);
    }
}