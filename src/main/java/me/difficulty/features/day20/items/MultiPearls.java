
package me.difficulty.features.day20.items;

import me.difficulty.core.items.CustomItem;
import me.difficulty.core.items.ItemIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class MultiPearls extends CustomItem {

    /*
     * ========================================
     * DIA 20 - MULTI PEARLS
     * ========================================
     *
     * Material base: HEART_OF_THE_SEA
     * ID: multi_pearls
     * CustomModelData: 1207
     *
     * Material para futuros crafteos.
     */

    public MultiPearls(JavaPlugin plugin) {

        super(
                plugin,
                ItemIds.MULTI_PEARLS,
                Material.HEART_OF_THE_SEA,
                ItemIds.MULTI_PEARLS_MODEL
        );
    }

    /*
     * ========================================
     * NOMBRE
     * ========================================
     */
    @Override
    protected Component getDisplayName() {

        return Component.text(
                "Multi Pearls",
                NamedTextColor.LIGHT_PURPLE
        ).decoration(
                TextDecoration.ITALIC,
                false
        );
    }

    /*
     * ========================================
     * DESCRIPCION
     * ========================================
     */
    @Override
    protected List<Component> getLore() {

        return List.of(

                Component.text(
                        "La energía de ocho Perlas Elementales.",
                        NamedTextColor.AQUA
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "Concentrada en un único núcleo.",
                        NamedTextColor.GRAY
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "Material para crafteos especiales.",
                        NamedTextColor.GREEN
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                )
        );
    }
}
