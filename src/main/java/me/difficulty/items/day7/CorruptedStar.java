package me.difficulty.items.day7;

import me.difficulty.items.CustomItem;
import me.difficulty.items.ItemIds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CorruptedStar extends CustomItem {

    public CorruptedStar(JavaPlugin plugin) {

        super(
                plugin,
                ItemIds.CORRUPTED_STAR,
                Material.NETHER_STAR,
                ItemIds.CORRUPTED_STAR_MODEL
        );
    }

    @Override
    protected Component getDisplayName() {

        return Component.text(
                        "Estrella Corrupta",
                        NamedTextColor.DARK_PURPLE
                )
                .decoration(
                        TextDecoration.ITALIC,
                        false
                );
    }

    @Override
    protected List<Component> getLore() {

        return List.of(

                Component.text(
                                "Un objeto contaminado por una energía desconocida.",
                                NamedTextColor.GRAY
                        )
                        .decoration(
                                TextDecoration.ITALIC,
                                false
                        ),

                Component.text(
                                "Disponible desde el Día 7.",
                                NamedTextColor.DARK_GRAY
                        )
                        .decoration(
                                TextDecoration.ITALIC,
                                false
                        )
        );
    }

    /*
     * Dejamos este método para no tener que modificar
     * todavía todos nuestros listeners.
     */
    public boolean isCorruptedStar(
            ItemStack item
    ) {

        return isItem(item);
    }
}