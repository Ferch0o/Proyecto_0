
package me.difficulty.features.day20.items;

import me.difficulty.core.items.CustomItem;
import me.difficulty.core.items.ItemIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class ElementalGold extends CustomItem {

    public ElementalGold(JavaPlugin plugin) {

        super(
                plugin,
                ItemIds.ELEMENTAL_GOLD,
                Material.GOLD_INGOT,
                ItemIds.ELEMENTAL_GOLD_MODEL
        );
    }

    @Override
    protected Component getDisplayName() {

        return Component.text(
                "Oro Elemental",
                NamedTextColor.GOLD
        ).decoration(
                TextDecoration.ITALIC,
                false
        );
    }

    @Override
    protected List<Component> getLore() {

        return List.of(

                Component.text(
                        "Un lingote impregnado de energía del Nether.",
                        NamedTextColor.GRAY
                ).decoration(
                        TextDecoration.ITALIC,
                        false
                ),

                Component.text(
                        "Obtenido del Rey Piglin.",
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