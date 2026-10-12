package me.difficulty.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CorruptedStar {

    private final NamespacedKey itemKey;

    public CorruptedStar(JavaPlugin plugin) {
        this.itemKey = new NamespacedKey(plugin, "corrupted_star");
    }

    public ItemStack createItem() {

        ItemStack item = new ItemStack(Material.NETHER_STAR);

        ItemMeta meta = item.getItemMeta();

        meta.displayName(
                Component.text("Estrella Corrupta", NamedTextColor.DARK_PURPLE)
                        .decoration(TextDecoration.ITALIC, false)
        );

        meta.lore(List.of(
                Component.text("Un objeto contaminado por una energía desconocida.",
                                NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),

                Component.text("Disponible desde el Día 7.",
                                NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));

        meta.getPersistentDataContainer().set(
                itemKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);

        return item;
    }

    public boolean isCorruptedStar(ItemStack item) {

        if (item == null || item.getType() != Material.NETHER_STAR) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return false;
        }

        return meta.getPersistentDataContainer().has(
                itemKey,
                PersistentDataType.BYTE
        );
    }
}