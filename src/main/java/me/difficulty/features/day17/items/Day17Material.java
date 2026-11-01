package me.difficulty.features.day17.items;

import me.difficulty.core.items.CustomItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/** Material especial del día 17. Conserva la identificación CustomItem del proyecto. */
public final class Day17Material extends CustomItem {
    private final String name;
    private final String description;

    public Day17Material(JavaPlugin plugin, String id, Material material, int model,
                         String name, String description) {
        super(plugin, id, material, model);
        this.name = name;
        this.description = description;
    }

    @Override
    protected Component getDisplayName() {
        return Component.text(name, NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    protected List<Component> getLore() {
        return List.of(Component.text(description, NamedTextColor.GRAY));
    }

    public boolean matches(ItemStack stack) {
        return isItem(stack);
    }
}
