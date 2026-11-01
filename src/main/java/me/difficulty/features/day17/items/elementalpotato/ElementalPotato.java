package me.difficulty.features.day17.items.elementalpotato;

import me.difficulty.core.items.CustomItem;
import me.difficulty.core.items.ItemIds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class ElementalPotato extends CustomItem {
    public ElementalPotato(JavaPlugin plugin) {
        super(plugin, ItemIds.ELEMENTAL_POTATO, Material.POTATO,
                ItemIds.ELEMENTAL_POTATO_MODEL);
    }

    @Override
    public ItemStack createItem() {
        ItemStack item = super.createItem();
        ItemMeta meta = item.getItemMeta();
        FoodComponent food = meta.getFood();
        food.setCanAlwaysEat(true);
        meta.setFood(food);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    protected Component getDisplayName() {
        return Component.text("Patata Elemental", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false);
    }

    @Override
    protected List<Component> getLore() {
        return List.of(
                Component.text("Consume para eliminar la Maldición del Inframundo.", NamedTextColor.GRAY),
                Component.text("Otorga 2 corazones adicionales de vida máxima.", NamedTextColor.YELLOW),
                Component.text("Una sola curación por jugador.", NamedTextColor.DARK_GRAY)
        );
    }

    public boolean isElementalPotato(ItemStack stack) {
        return isItem(stack);
    }
}
