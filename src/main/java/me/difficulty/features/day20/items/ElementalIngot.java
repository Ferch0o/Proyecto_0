
package me.difficulty.features.day20.items;

import me.difficulty.core.items.CustomItem;
import me.difficulty.core.items.ItemIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class ElementalIngot extends CustomItem {

    public ElementalIngot(JavaPlugin plugin) {

        super(
                plugin,
                ItemIds.ELEMENTAL_INGOT,
                Material.NETHERITE_INGOT,
                ItemIds.ELEMENTAL_INGOT_MODEL
        );
    }

    @Override
    protected Component getDisplayName() {

        return Component.text(
                "Lingote Elemental",
                NamedTextColor.LIGHT_PURPLE
        ).decoration(
                TextDecoration.ITALIC,
                false
        );
    }

    @Override
    protected List<Component> getLore() {

        return List.of(

                Component.text(
                        "Un lingote que fusiona diferentes minerales.",
                        NamedTextColor.GRAY
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "Material para la futura Armadura Elemental.",
                        NamedTextColor.AQUA
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "Objeto especial del Día 20.",
                        NamedTextColor.DARK_GRAY
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                )
        );
    }
}