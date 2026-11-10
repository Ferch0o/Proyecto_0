
package me.difficulty.features.day20.items;

import me.difficulty.core.items.CustomItem;
import me.difficulty.core.items.ItemIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class ElementalTemplate extends CustomItem {

    public ElementalTemplate(JavaPlugin plugin) {
        super(
                plugin,
                ItemIds.ELEMENTAL_TEMPLATE,
                Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
                ItemIds.ELEMENTAL_TEMPLATE_MODEL
        );
    }

    @Override
    protected Component getDisplayName() {
        return Component.text(
                "Plantilla Elemental",
                NamedTextColor.LIGHT_PURPLE
        ).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    protected List<Component> getLore() {
        return List.of(
                Component.text(
                        "Fusiona los elementos más inestables del Nether.",
                        NamedTextColor.GRAY
                ).decoration(TextDecoration.ITALIC, false),

                Component.text(
                        "Necesaria para la Armadura Elemental.",
                        NamedTextColor.AQUA
                ).decoration(TextDecoration.ITALIC, false),

                Component.text(
                        "Objeto especial del Día 20.",
                        NamedTextColor.DARK_GRAY
                ).decoration(TextDecoration.ITALIC, false)
        );
    }
}
