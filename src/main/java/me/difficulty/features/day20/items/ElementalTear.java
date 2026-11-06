
package me.difficulty.features.day20.items;

import me.difficulty.core.items.CustomItem;
import me.difficulty.core.items.ItemIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class ElementalTear extends CustomItem {

    public ElementalTear(JavaPlugin plugin) {

        super(
                plugin,
                ItemIds.ELEMENTAL_TEAR,
                Material.GHAST_TEAR,
                ItemIds.ELEMENTAL_TEAR_MODEL
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
                "Lágrima Elemental",
                NamedTextColor.LIGHT_PURPLE
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
                        "Una lágrima cargada de energía del Nether.",
                        NamedTextColor.GRAY
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "Obtenida del Ghast Demoníaco.",
                        NamedTextColor.YELLOW
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