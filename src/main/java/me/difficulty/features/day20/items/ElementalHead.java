
package me.difficulty.features.day20.items;

import me.difficulty.core.items.CustomItem;
import me.difficulty.core.items.ItemIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class ElementalHead extends CustomItem {

    public ElementalHead(JavaPlugin plugin) {

        super(
                plugin,
                ItemIds.ELEMENTAL_HEAD,
                Material.WITHER_SKELETON_SKULL,
                ItemIds.ELEMENTAL_HEAD_MODEL
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
                "Cabeza Elemental",
                NamedTextColor.DARK_PURPLE
        ).decoration(
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
                        "Obtenida del Guardián de la Fortaleza.",
                        NamedTextColor.GRAY
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "Material especial del Día 20.",
                        NamedTextColor.DARK_GRAY
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                )
        );
    }
}
