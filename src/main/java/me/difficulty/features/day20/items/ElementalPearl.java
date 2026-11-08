
package me.difficulty.features.day20.items;

import me.difficulty.core.items.CustomItem;
import me.difficulty.core.items.ItemIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class ElementalPearl extends CustomItem {

    public ElementalPearl(JavaPlugin plugin) {

        super(
                plugin,
                ItemIds.ELEMENTAL_PEARL,
                Material.ENDER_PEARL,
                ItemIds.ELEMENTAL_PEARL_MODEL
        );
    }

    @Override
    protected Component getDisplayName() {

        return Component.text(
                "Perla Elemental",
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
                        "No falla durante la Tormenta.",
                        NamedTextColor.AQUA
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "80% de obtener Velocidad II por 5 segundos.",
                        NamedTextColor.GREEN
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "Obtenida del Enderman Ancestral.",
                        NamedTextColor.GRAY
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                )
        );
    }
}