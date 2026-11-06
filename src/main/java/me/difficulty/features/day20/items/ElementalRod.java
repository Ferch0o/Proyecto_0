
package me.difficulty.features.day20.items;

import me.difficulty.core.items.CustomItem;
import me.difficulty.core.items.ItemIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class ElementalRod extends CustomItem {

    /*
     * ========================================
     * CONSTRUCTOR
     * ========================================
     */
    public ElementalRod(JavaPlugin plugin) {

        super(
                plugin,
                ItemIds.ELEMENTAL_ROD,
                Material.BLAZE_ROD,
                ItemIds.ELEMENTAL_ROD_MODEL
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
                        "Vara Elemental",
                        NamedTextColor.GOLD
                )
                .decoration(
                        TextDecoration.ITALIC,
                        false
                );
    }

    /*
     * ========================================
     * DESCRIPCIÓN
     * ========================================
     */
    @Override
    protected List<Component> getLore() {

        return List.of(

                Component.text(
                                "Una vara cargada de energía elemental.",
                                NamedTextColor.GRAY
                        )
                        .decoration(
                                TextDecoration.ITALIC,
                                false
                        ),

                Component.text(
                                "Obtenida al derrotar al Rey Blaze.",
                                NamedTextColor.YELLOW
                        )
                        .decoration(
                                TextDecoration.ITALIC,
                                false
                        ),

                Component.text(
                                "Material especial del Día 20.",
                                NamedTextColor.DARK_GRAY
                        )
                        .decoration(
                                TextDecoration.ITALIC,
                                false
                        )
        );
    }
}