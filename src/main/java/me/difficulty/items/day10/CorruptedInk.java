package me.difficulty.items.day10;

import me.difficulty.items.CustomItem;
import me.difficulty.items.ItemIds;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;


public class CorruptedInk extends CustomItem {


    public CorruptedInk(
            JavaPlugin plugin
    ) {

        super(
                plugin,
                ItemIds.CORRUPTED_INK,
                Material.GLOW_INK_SAC,
                ItemIds.CORRUPTED_INK_MODEL
        );
    }


    @Override
    protected Component getDisplayName() {

        return Component.text(
                        "Tinta Corrupta",
                        NamedTextColor.DARK_AQUA
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
                                "Una tinta alterada por una energía corrupta.",
                                NamedTextColor.GRAY
                        )
                        .decoration(
                                TextDecoration.ITALIC,
                                false
                        ),

                Component.text(
                                "Obtenida de los Glow Squids.",
                                NamedTextColor.DARK_GRAY
                        )
                        .decoration(
                                TextDecoration.ITALIC,
                                false
                        ),

                Component.text(
                                "Disponible desde el Día 10.",
                                NamedTextColor.DARK_GRAY
                        )
                        .decoration(
                                TextDecoration.ITALIC,
                                false
                        )
        );
    }


    public boolean isCorruptedInk(
            ItemStack item
    ) {

        return isItem(item);
    }
}